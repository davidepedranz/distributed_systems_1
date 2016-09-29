package helloakka.actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import helloakka.messagges.HelloMessage;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

// The Sender actor class
public class Sender extends UntypedActor {
	private ActorRef receiver;

	public Sender(ActorRef receiver) {
		this.receiver = receiver;    // this actor will be the destination of our messages
	}

	public void preStart() {
		// Create a timer that will periodically send a message to the receiver actor
		Cancellable timer = getContext().system().scheduler().schedule(
			Duration.create(1, TimeUnit.SECONDS),                // when to start generating messages
			Duration.create(1, TimeUnit.SECONDS),                // how frequently generate them
			receiver,                                            // destination actor reference
			new HelloMessage("HelloMessage from " + getSelf().path().name()), // the message to send
			getContext().system().dispatcher(), getSelf()        // source of the message (myself)
		);
	}

	public void onReceive(Object message) {
		unhandled(message);        // this actor does not handle any incoming messages
	}
}
