package lab3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lab3.actors.Chatter;
import lab3.actors.Listener;
import lab3.messages.StopMessage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulate a system which preserves the order of related messages (message and reply).
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 */
public class Main {

	// entry point
	public static void main(String[] args) throws InterruptedException {

		// create the 'lab3' actor system
		final ActorSystem system = ActorSystem.create("lab3");

		// number of chatting actors
		final int actorsNumber = 10;

		// actors group
		final List<ActorRef> actors = new LinkedList<>();

		// create actors
		final ActorRef actor9 = system.actorOf(Props.create(Listener.class, 9, actorsNumber, actors), "listener_9");
		final ActorRef actor3 = system.actorOf(Props.create(Chatter.class, 3, actorsNumber, actors, "b", false), "chatter_3");
		final ActorRef actor8 = system.actorOf(Props.create(Listener.class, 8, actorsNumber, actors), "listener_8");
		final ActorRef actor4 = system.actorOf(Props.create(Chatter.class, 4, actorsNumber, actors, "c", true), "chatter_4");
		final ActorRef actor5 = system.actorOf(Props.create(Chatter.class, 5, actorsNumber, actors, "c", false), "chatter_5");
		final ActorRef actor6 = system.actorOf(Props.create(Listener.class, 6, actorsNumber, actors), "listener_6");
		final ActorRef actor1 = system.actorOf(Props.create(Chatter.class, 1, actorsNumber, actors, "b", true), "chatter_1");
		final ActorRef actor7 = system.actorOf(Props.create(Listener.class, 7, actorsNumber, actors), "listener_7");
		final ActorRef actor2 = system.actorOf(Props.create(Chatter.class, 2, actorsNumber, actors, "a", false), "chatter_2");
		final ActorRef actor0 = system.actorOf(Props.create(Chatter.class, 0, actorsNumber, actors, "a", true), "chatter_0");

		// add all actors to the group
		actors.addAll(Arrays.asList(actor0, actor1, actor2, actor3, actor4, actor5, actor6, actor7, actor8, actor9));
		assert actors.size() == actorsNumber;

		// simulate for 1 second
		Thread.sleep(1000);

		// ask everybody not to send messages anymore
		final StopMessage stop = new StopMessage();
		actors.forEach(actor -> actor.tell(stop, null));

		// wait for all actors to stop & terminate the system
		Thread.sleep(1000);
		system.terminate();
	}

}
