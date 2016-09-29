package helloakka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import helloakka.actors.Receiver;
import helloakka.actors.Sender;

public class Main {

	// constants
	private final static int N_SENDERS = 5;

	// entry point
	public static void main(String[] args) {

		// Create the 'helloakka' actor system
		final ActorSystem system = ActorSystem.create("helloakka");

		// Create a single receiver actor
		final ActorRef receiver = system.actorOf(Props.create(Receiver.class), "receiver");

		// Create multiple sender actors
		for (int i = 0; i < N_SENDERS; i++) {

			// create the actor system
			system.actorOf(

				// actor properties
				Props.create(
					Sender.class,   // actor class
					receiver        // the constructor parameters are passed here (the receiver in our case)
				),
				"sender" + i
			);
		}
	}
}
