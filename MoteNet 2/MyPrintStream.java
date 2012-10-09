import java.util.Vector;

import net.tinyos.util.Messenger;


public class MyPrintStream implements Messenger {
	
	private int moteId;
	private Vector BadPacket;
	public MyPrintStream() {
	BadPacket = new Vector();
		
		
	}
	
	public MyPrintStream(int moteId) {
	this.moteId =moteId;
	this.BadPacket = new Vector();
		
	}
	public void message(String s) {
		long rTime = System.nanoTime();
		
		if(!(s.contains("received Packet") || s.contains("read packet"))) {
			BadPacket.add(new MyMessagePacket(s,rTime));
		}

	}

	public Vector getBadPacket() {
		return BadPacket;
	}

	public void setBadPacket(Vector badPacket) {
		BadPacket = badPacket;
	}

	public int getMoteId() {
		return moteId;
	}

	public void setMoteId(int moteId) {
		this.moteId = moteId;
	}

}
