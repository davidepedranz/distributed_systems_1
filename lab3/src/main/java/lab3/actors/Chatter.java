package lab3.actors;

import akka.actor.ActorRef;
import lab3.messages.ChatMessage;
import lab3.messages.StopMessage;

import java.util.List;

/**
 * This actor will reply on a given topic.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Chatter extends AbstractActor {

	// initialization parameters
	private final String topic;
	private final boolean startDiscussion;

	private boolean stop = false;

	/**
	 * Create a new Chatter actor.
	 *
	 * @param id     This actor identifier (used for the vector clock)
	 * @param actors Actors in the system (used for the vector clock)
	 * @param topic  Topic to which to reply to
	 */
	public Chatter(int id, int numberOfActors, List<ActorRef> actors, String topic, boolean startDiscussion) {

		// call super constructor
		super(id, numberOfActors, actors);

		// initialize actor parameters
		this.topic = topic;
		this.startDiscussion = startDiscussion;
	}

	@Override
	public void preStart() {
		super.preStart();
		if (startDiscussion) {
			sendChatMessage(topic, 0);
		}
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof StopMessage) {
			stop = true;
			System.out.println("Stopping actor... " + id);
		}
		super.onReceive(message);
	}

	@Override
	protected final void onChatMessage(ChatMessage message) {

		// if not mine message, reply to it
		if (!stop && this.topic.equals(message.topic()) && message.sender() != id) {

			// reply to the received message with an incremented value and the same topic
			sendChatMessage(message.topic(), message.replies());
		}
	}


}
