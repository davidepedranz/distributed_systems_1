package lab3.vectorclock;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.Ordering;
import lab3.messages.ChatMessage;
import lab3.messages.StopMessage;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract actor that implements Vector Clock to deliver ChatMessages in a topic based order.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public abstract class VectorClockActor extends UntypedActor {

	// initialization parameters
	private final int id;
	private final int numberOfActors;
	private final List<ActorRef> actors;

	// internal state
	private final int[] vc;
	private final List<ChatMessage> buffer;

	// delivered messages -> for debug
	private final List<ChatMessage> delivered;

	// make delivery randomly
	private final SecureRandom random;

	/**
	 * This actor use a Vector Clock to deliver messages preserving the order by topic.
	 *
	 * @param id             This actor identifier (used for the vector clock)
	 * @param numberOfActors Number of actors in the system (used for the vector clock)
	 * @param actors         Actors in the system (used for the vector clock)
	 */
	public VectorClockActor(int id, int numberOfActors, List<ActorRef> actors) {

		// initialize actor parameters
		this.id = id;
		this.numberOfActors = numberOfActors;
		this.actors = Collections.unmodifiableList(actors);

		// initialize the vector clock
		this.vc = new int[numberOfActors];

		// create a buffer to temporally store message not ready to be delivered
		this.buffer = new LinkedList<>();

		// store the delivered messages
		this.delivered = new LinkedList<>();

		// deliver the message in a random order
		this.random = new SecureRandom();
	}

	@Override
	public void preStart() {
		assert this.numberOfActors == this.actors.size();
	}

	@Override
	public void postStop() {

		// partition messages
		final Map<String, List<ChatMessage>> partition = delivered
			.stream()
			.collect(Collectors.groupingBy(ChatMessage::topic));

		// build summary
		final StringBuilder builder = new StringBuilder("Actor " + id + "\n");
		partition.forEach((topic, list) -> {
			final boolean ordered = Ordering.natural().isOrdered(list);
			builder
				.append(" -> topic ")
				.append(topic)
				.append(" [")
				.append(ordered ? "OK" : "KO")
				.append("]: ")
				.append(list)
				.append("\n");
		});
		builder
			.append(" -> NOT DELIVERED: ")
			.append(buffer);

		// print summary
		System.out.println(builder.toString());
	}

	@Override
	public final void onReceive(Object message) throws Exception {
		if (message instanceof ChatMessage) {
			handleChatMessage((ChatMessage) message);
		} else if (message instanceof StopMessage) {
			onStopMessage((StopMessage) message);
		} else {
			unhandled(message);
		}
	}

	/**
	 * Handle a new ChatMessage.
	 * Queue the message and deliver all the deliverable ones.
	 *
	 * @param newMessage Message to handle.
	 */
	private void handleChatMessage(ChatMessage newMessage) {

		// add the message to the buffer
		buffer.add(newMessage);

		// check if I have a message to deliver
		Optional<ChatMessage> toDeliver;
		do {
			toDeliver = nextMessage();
			toDeliver.ifPresent(this::deliverChatMessage);
		} while (toDeliver.isPresent());
	}

	/**
	 * Extract the next message ready to be delivered.
	 *
	 * @return Next message to deliver.
	 */
	private Optional<ChatMessage> nextMessage() {

		// iterate over the buffer to check messages ready to be delivered
		final Iterator<ChatMessage> iterator = buffer.iterator();
		while (iterator.hasNext()) {
			final ChatMessage message = iterator.next();
			boolean canDeliver = canDeliverMessage(message);
			if (canDeliver) {
				iterator.remove();
				return Optional.of(message);
			}
		}

		// no message to deliver
		return Optional.empty();
	}

	/**
	 * Check if a message is ready to be delivered.
	 *
	 * @param message ChatMessage
	 * @return True if safe to deliver, false otherwise.
	 */
	private boolean canDeliverMessage(ChatMessage message) {

		// extract the sender of the message
		final int sender = message.sender();

		// Vj[j] = Vk[j]+1
		boolean condition1 = message.vc()[sender] == this.vc[sender] + 1;

		// Vj[i] ≤ Vk[i] for all i not equal to j
		boolean condition2 = true;
		for (int i = 0; i < this.vc.length; i++) {
			if (i != message.sender()) {
				condition2 = condition2 && message.vc()[i] <= this.vc[i];
			}
		}

		// both conditions must be true
		return condition1 && condition2;
	}


	/**
	 * Simulate the message delivery to the application (printing it to the console).
	 *
	 * @param message Message to deliver.
	 */
	private void deliverChatMessage(ChatMessage message) {

		// update vc -> merge
		// NB: no update my counter!
		for (int i = 0; i < actors.size(); i++) {
			if (i != this.id) {
				this.vc[i] = Math.max(this.vc[i], message.vc()[i]);
			}
		}

		// deliver the message -> store it in a queue
		this.delivered.add(message);

		// do next elaboration
		onChatMessage(message);
	}

	/**
	 * Send a new ChatMessage with the given topic and number of replies to all the other actors in the system.
	 *
	 * @param topic   Topic of the ChatMessage.
	 * @param replies Current number of replies of the ChatMessage.
	 */
	protected final void sendChatMessage(String topic, int replies) {

		// update my vector clock
		this.vc[this.id]++;

		// message
		final ChatMessage message = new ChatMessage(topic, replies + 1, this.id, this.vc);

		// do not send message to myself... deliver it now
		delivered.add(message);

		// send message in multicast
		actors.stream()
			.unordered()
			.filter(actor -> actor != getSelf())
			.forEach(actor -> {
				try {
					Thread.sleep(random.nextInt(20));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				actor.tell(message, getSelf());
			});
	}

	/**
	 * Return the ID of this actor.
	 *
	 * @return My ID.
	 */
	protected final int id() {
		return id;
	}

	/**
	 * A new ChatMessage is ready to be delivered.
	 *
	 * @param message ChatMessage.
	 */
	protected abstract void onChatMessage(ChatMessage message);

	/**
	 * A StopMessage has just arrived.
	 *
	 * @param message StopMessage
	 */
	protected abstract void onStopMessage(StopMessage message);
}
