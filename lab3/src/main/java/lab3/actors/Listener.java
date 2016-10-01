package lab3.actors;

import akka.actor.ActorRef;
import lab3.messages.ChatMessage;

import java.util.List;

/**
 * This actor will reply on a given topic.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Listener extends AbstractActor {

	public Listener(int id, int numberOfActors, List<ActorRef> actors) {

		// call super constructor
		super(id, numberOfActors, actors);
	}

	@Override
	protected final void onChatMessage(ChatMessage message) {
		// do nothing
	}


}
