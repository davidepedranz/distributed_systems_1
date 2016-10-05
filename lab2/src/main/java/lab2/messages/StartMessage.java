package lab2.messages;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Start message that informs every chat participant about its peers
 */
public class StartMessage implements Serializable {

	private final List<ActorRef> group;        // an array of group members (actor references)

	public StartMessage(List<ActorRef> group) {
		this.group = Collections.unmodifiableList(group);
	}

	public List<ActorRef> getGroup() {
		return group;
	}
}
