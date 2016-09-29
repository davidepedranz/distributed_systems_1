package helloakka.messagges;

import java.io.Serializable;

// Hello Message
public class HelloMessage implements Serializable {
	public final String msg;

	public HelloMessage(String msg) {
		this.msg = msg;
	}
}
