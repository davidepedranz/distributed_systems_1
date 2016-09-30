package lab3.messages;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This is a message exchanged by Chatter actors.
 *
 * @author Davide Pedranz <davide.pedranz@gmail.com>
 * @see lab3.actors.Chatter
 */
public class ChatMessage implements Serializable, Comparable {

	private final String topic;
	private final int replies;
	private final int senderID;
	private final int[] vc;

	/**
	 * Construct a new Chat Message.
	 *
	 * @param topic    Topic of the message.
	 * @param replies  Number of this message for this topic.
	 * @param senderID The ID of the actor who created this message.
	 * @param vc       Vector Clock (deep copied).
	 */
	public ChatMessage(String topic, int replies, int senderID, int[] vc) {
		this.topic = topic;
		this.replies = replies;
		this.senderID = senderID;
		this.vc = Arrays.copyOf(vc, vc.length);
	}

	public String topic() {
		return topic;
	}

	public int replies() {
		return replies;
	}

	public int sender() {
		return senderID;
	}

	public int[] vc() {
		return vc;
	}

	@Override
	public int compareTo(Object o) {
		final ChatMessage other = (ChatMessage) o;
		final int compareTopic = this.topic.compareTo(other.topic);
		return compareTopic != 0 ? compareTopic : (this.replies - other.replies);
	}

	@Override
	public String toString() {
		return topic + replies + " ";
	}

}
