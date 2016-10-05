package lab2.messages;

import java.io.Serializable;

/**
 * Chat Message.
 */
public class ChatMessage implements Serializable {

	// message fields
	private final String topic;         // "topic" of the conversation
	private final int n;                // the number of the reply in the current topic
	private final int senderId;         // my id

	public ChatMessage(String topic, int n, int senderId) {
		this.topic = topic;
		this.n = n;
		this.senderId = senderId;
	}

	public String topic() {
		return topic;
	}

	public int n() {
		return n;
	}

	public int senderId() {
		return senderId;
	}
}
