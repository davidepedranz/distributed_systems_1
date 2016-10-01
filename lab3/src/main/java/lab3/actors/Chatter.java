package lab3.actors;

import akka.actor.ActorRef;
import lab3.messages.ChatMessage;
import lab3.messages.StopMessage;
import lab3.vectorclock.VectorClockActor;

import java.util.List;

/**
 * This actor will reply on a given topic.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Chatter extends VectorClockActor {


	// initialization parameters
	private final String topic;
	private final boolean startDiscussion;

	// stop when I get a "StopMessage"
	private boolean stop;

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

		// current state
		this.stop = false;
	}

	@Override
	public void preStart() {
		super.preStart();
		if (startDiscussion) {
			sendChatMessage(topic, 0);
		}
	}

	@Override
	protected final void onChatMessage(ChatMessage message) {

		// if not mine message, reply to it
		if (!stop && this.topic.equals(message.topic()) && message.sender() != id()) {

			// reply to the received message with an incremented value and the same topic
			sendChatMessage(message.topic(), message.replies());
		}
	}

	@Override
	protected void onStopMessage(StopMessage message) {
		stop = true;
		System.out.println("Stopping chatter " + id() + "...");
	}

}
