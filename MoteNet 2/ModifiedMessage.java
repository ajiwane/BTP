import net.tinyos.message.*;
/**
 * Messages received from the mote, are stored in this data structure beforef writing it to a file, along with other parameters that are stored. 
 * 
 *
 */
public class ModifiedMessage {
	private Message origMessage;
	private long time;
	private String stamp;
	private int mId;
	public ModifiedMessage(Message origMessage, long time, int mId) {
		super();
		this.origMessage = origMessage;
		this.time = time;
		this.mId = mId;
	}

	public ModifiedMessage(Message origMessage, String time, int mId) {
		super();
		this.origMessage = origMessage;
		this.stamp = time;
		this.mId = mId;
	}
	public Message getOrigMessage() {
		return origMessage;
	}
	public void setOrigMessage(Message origMessage) {
		this.origMessage = origMessage;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public String getStamp() {
		return stamp;
	}

	public void setStamp(String stamp) {
		this.stamp = stamp;
	}
	

}