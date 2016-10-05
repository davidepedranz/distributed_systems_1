package lab2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lab2.actors.Chatter;
import lab2.messages.StartMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

	// system constants
	public final static int N_CHATTERS = 4;          // number of chatting actors
	public final static int N_MESSAGES = 5;          // number of chat messages to send (per chatter)
	private final static int N_LISTENERS = 10;        // number of listening actors

	// entry point
	public static void main(String[] args) {

		// Create the 'lab2' actor system
		final ActorSystem system = ActorSystem.create("lab2");

		List<ActorRef> group = new ArrayList<>();

		// the first four peers will be talkative chatters

		// this one will start the "discussion" on topic "a"
		group.add(system.actorOf(Props.create(Chatter.class, 0, "a", true), "chatter_0"));

		// this one will catch up the topic "a"
		group.add(system.actorOf(Props.create(Chatter.class, 1, "a", false), "chatter_1"));

		// add two more chatters that should talk on another topic
		// Important: use IDs 2 and 3 for them
		group.add(system.actorOf(Props.create(Chatter.class, 2, "b", true), "chatter_2"));
		group.add(system.actorOf(Props.create(Chatter.class, 3, "b", false), "chatter_3"));

		// the rest are silent listeners: they don't have topics to discuss
		for (int i = 0; i < N_LISTENERS; i++) {
			group.add(system.actorOf(Props.create(Chatter.class, i + N_CHATTERS, null, false), "listener" + i));
		}

		// "forgetting" the reference to the group ensuring that no one can modify the group
		group = Collections.unmodifiableList(group);

		// we send the start message to the talkative chatters to inform them of the whole group of participants (peers)
		final StartMessage start = new StartMessage(group);
		for (ActorRef peer : group) {
			peer.tell(start, null);
		}
	}

}
