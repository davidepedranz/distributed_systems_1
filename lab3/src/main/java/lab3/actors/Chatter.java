package lab3.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.Ordering;
import lab3.messages.ChatMessage;
import lab3.messages.StartMessage;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This actor will reply on a given topic.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Chatter extends UntypedActor {

	// initialization parameters
	private final int id;
	private final List<ActorRef> actors;
	private final int maxMessages;
	private final String topic;
	private final boolean startDiscussion;

	// internal state
	private final int[] vc;
	private final List<ChatMessage> buffer;

	// delivered messages -> for debug
	private final List<ChatMessage> delivered;

	// make delivery randomly
	private final SecureRandom random;

	// stop condition
	private int got = 0;
	private int sent = 0;

	/**
	 * Create a new Chatter actor.
	 *
	 * @param id     This actor identifier (used for the vector clock)
	 * @param actors Actors in the system (used for the vector clock)
	 * @param topic  Topic to which to reply to
	 */
	public Chatter(int id, int numberOfActors, List<ActorRef> actors, int maxMessages, String topic, boolean startDiscussion) {

		// initialize actor parameters
		this.id = id;
		this.actors = Collections.unmodifiableList(actors);
		this.maxMessages = maxMessages;
		this.topic = topic;
		this.startDiscussion = startDiscussion;

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
	public void preStart() throws Exception {
		super.preStart();
		//System.out.println("Actor " + id + " -> Prestart -> " + actors);
		//startChat();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof StartMessage) {
			startChat();
		} else if (message instanceof ChatMessage) {
			handleChatMessage((ChatMessage) message);
		} else {
			unhandled(message);
		}
	}

	private void startChat() {
		System.out.println("Actor" + id + " -> group " + actors);
		if (this.startDiscussion) {
			//System.out.println("Actor" + id + " -> start conversation " + topic);
			sendChatMessage(this.topic, 0);
		}
	}

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
	 * Handle a new ChatMessage.
	 *
	 * @param newMessage Message to handle.
	 */
	private void handleChatMessage(ChatMessage newMessage) {

		// add the message to the buffer
		buffer.add(newMessage);

		// check if I have a message to deliver
		final Optional<ChatMessage> toDeliver = nextMessage();
		toDeliver.ifPresent(this::deliverChatMessage);
	}

	/**
	 * Simulate the message delivery to the application (printing it to the console).
	 *
	 * @param message Message to deliver.
	 */
	private void deliverChatMessage(ChatMessage message) {

		// update received messages
		this.got++;

		// update vc -> merge
		// NB: no update my counter!
		for (int i = 0; i < actors.size(); i++) {
			if (i != this.id) {
				this.vc[i] = Math.max(this.vc[i], message.vc()[i]);
			}
		}

		// deliver the message -> store it in a queue
		this.delivered.add(message);

		// stop condition
		if (this.got == this.maxMessages * this.actors.size()) {
//			System.out.println("Actor " + id + " -> " + delivered);

			// print topic "a"
			final List<ChatMessage> topicA = delivered.stream()
				.filter(m -> m.topic().contains("a"))
				.collect(Collectors.toList());
			System.out.println("Actor " + id + " -> topic A: " + topicA + " --> " + Ordering.natural().isOrdered(topicA));

			// print topic "b"
			final List<ChatMessage> topicB = delivered.stream()
				.filter(m -> m.topic().contains("b"))
				.collect(Collectors.toList());
			System.out.println("Actor " + id + " -> topic B: " + topicB + " --> " + Ordering.natural().isOrdered(topicB));

			System.out.println("Actor " + id + " -> NOT DELIVERED: " + buffer);

//			System.out.println("Try to stop... " + id);
//			getContext().stop(getSelf());
			return;
		}

		// if not mine message, reply to it
		if (this.topic.equals(message.topic()) && message.sender() != id) {

			// reply to the received message with an incremented value and the same topic
			sendChatMessage(message.topic(), message.replies());
		}
	}

	private void sendChatMessage(String topic, int replies) {

		// update sent messages -> only for stop condition
		this.sent++;

		// update my vector clock
		this.vc[this.id]++;

		// message
		final ChatMessage message = new ChatMessage(topic, replies + 1, this.id, this.vc);

		// System.out.println("Actor " + id + " send message " + message);

		// do not send message to myself... deliver it now
		delivered.add(message);

		// send message in multicast
		actors.stream()
			.unordered()
			.filter(actor -> actor != getSelf())
			.forEach(actor -> {
				try {
					Thread.sleep(random.nextInt(50));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				// System.out.println("Actor " + id + " send message " + message + " --- to " + actor.path().name());
				actor.tell(message, getSelf());
			});
	}
}
