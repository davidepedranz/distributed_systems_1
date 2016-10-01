package lab3.actors;

import akka.actor.ActorRef;
import lab3.messages.ChatMessage;
import lab3.messages.StopMessage;
import lab3.vectorclock.VectorClockActor;

import java.util.List;

/**
 * This actor will only listen.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Listener extends VectorClockActor {

	/**
	 * This actor will listen for all messages.
	 *
	 * @param id             This actor identifier (used for the vector clock)
	 * @param numberOfActors Number of actors in the system (used for the vector clock)
	 * @param actors         Actors in the system (used for the vector clock)
	 */
	public Listener(int id, int numberOfActors, List<ActorRef> actors) {

		// call super constructor
		super(id, numberOfActors, actors);
	}

	@Override
	protected final void onChatMessage(ChatMessage message) {
		// do nothing
	}

	@Override
	protected void onStopMessage(StopMessage message) {
		// do nothing
	}

}
