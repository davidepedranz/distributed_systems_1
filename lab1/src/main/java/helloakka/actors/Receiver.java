package helloakka.actors;

import akka.actor.UntypedActor;
import helloakka.messagges.HelloMessage;

// The Receiver actor class
public class Receiver extends UntypedActor {

	public void onReceive(Object message) {        // This function is called on every message received by the actor.
		if (message instanceof HelloMessage) {            // Like this you can distinguish the types of received messages
			HelloMessage h = (HelloMessage) message;
			System.out.println("[" +
				getSelf().path().name() +    // the name of the current actor
				"] received a message from " +
				getSender().path().name() + // the name of the sender actor
				": " + h.msg                    // finally the message contents
			);
		} else {
			unhandled(message);                // for messages we don't know what to do with
		}
	}
}
