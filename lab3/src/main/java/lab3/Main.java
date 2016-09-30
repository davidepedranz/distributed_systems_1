package lab3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lab3.actors.Chatter;
import lab3.messages.StartMessage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {

	// actors
	private final static int N_CHATTERS = 4;                             // number of chatting actors
	private final static int N_LISTENERS = 0;                            // number of listening actors
	private final static int N_ACTORS = N_CHATTERS + N_LISTENERS;        // total actors in the system

	// simulation
	private final static int N_MESSAGES = 5;                             // number of chat messages to send (per chatter)

	// entry point
	public static void main(String[] args) throws InterruptedException {

		// create the 'lab3' actor system
		final ActorSystem system = ActorSystem.create("lab3");

		// create actors
		final List<ActorRef> actors = new LinkedList<>();
		final ActorRef actor0 = system.actorOf(Props.create(Chatter.class, 0, N_ACTORS, actors, N_MESSAGES, "a", true), "chatter_0");
		final ActorRef actor1 = system.actorOf(Props.create(Chatter.class, 1, N_ACTORS, actors, N_MESSAGES, "a", false), "chatter_1");
		final ActorRef actor2 = system.actorOf(Props.create(Chatter.class, 2, N_ACTORS, actors, N_MESSAGES, "b", false), "chatter_2");
		final ActorRef actor3 = system.actorOf(Props.create(Chatter.class, 3, N_ACTORS, actors, N_MESSAGES, "b", false), "chatter_3");
		actors.addAll(Arrays.asList(actor0, actor1, actor2, actor3));

		// start the system
		final StartMessage start = new StartMessage();
		for (ActorRef peer : actors) {
			peer.tell(start, null);
		}

		// wait 3 seconds
		Thread.sleep(5000);
		system.terminate();
	}

}
