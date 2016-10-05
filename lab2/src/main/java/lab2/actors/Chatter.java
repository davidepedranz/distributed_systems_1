package lab2.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import lab2.Main;
import lab2.messages.ChatMessage;
import lab2.messages.StartMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chatter extends UntypedActor {

	// actor properties
	private final int id;                   // My ID number
	private final String myTopic;           // The topic I am interested in, null if no topic
	private final boolean initiator;        // Should I start the conversation?

	// internal state
	private List<ActorRef> group;                            // the list of peers (the multicast group)
	private StringBuffer chatHistory = new StringBuffer();    // all the chat messages received are stored here
	private int recvCount = 0;    // number of received messages
	private int sendCount = 0;    // number of sent messages

	// constructor
	public Chatter(int id, String topic, boolean initiator) {
		this.id = id;
		this.myTopic = topic;
		this.initiator = initiator;
	}

	@Override
	public void onReceive(Object msg) {

		// received the start message
		if (msg instanceof StartMessage) {
			setGroup((StartMessage) msg);

			System.out.println(getSelf().path().name() + ": starting with total " + this.group.size() + " peer(s)");

			// initiator starts the conversation on his topic
			if (initiator) {
				sendChatMessage(myTopic, 0);        // with a message 0
			}
		}

		// received a chat message
		else if (msg instanceof ChatMessage) {
			// deliver the message
			deliver((ChatMessage) msg);
		}

		// unknown message
		else {
			unhandled(msg);
		}
	}

	private void setGroup(StartMessage sm) {
		this.group = new ArrayList<>(sm.getGroup());
	}

	private void deliver(ChatMessage m) {

		// Our "chat application" appends all the received messages to the chatHistory
		// and replies if the topic of the message is interesting
		chatHistory.append(m.topic());
		chatHistory.append(m.n());
		chatHistory.append(" ");
		recvCount++;

		// I have a topic to discuss and the message is on this topic
		// I still have something to tell
		// I don't want to reply to my own message
		if (myTopic != null && myTopic.equals(m.topic()) && sendCount < Main.N_MESSAGES && m.senderId() != id) {

			// reply to the received message with an incremented value and the same topic
			sendChatMessage(m.topic(), m.n() + 1);
		}

		// print the chat history in the end
		if (recvCount == Main.N_MESSAGES * Main.N_CHATTERS) {
			System.out.println(getSelf().path().name() + ": " + chatHistory);
		}
	}

	private void sendChatMessage(String topic, int n) {
		sendCount++;
		multicast(new ChatMessage(topic, n, id));
	}

	// implement the multicast (just send m to everybody in the group using the tell() function)
	private void multicast(Serializable message) {
		assert group != null;
		group.stream()
			.unordered()
			.forEach(actor -> actor.tell(message, getSelf()));
	}
}
