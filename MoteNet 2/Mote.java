/**
 * This class contains various  variable to store information of a mote. Various variable are used include moteport, moteid etc. 
 *  
 * 
 *
 */
public class Mote {
/**
 * 
 */
	private int moteId;
	private String motePort;
	private String fName;
	public Mote(int moteId, String motePort, String fname) {
		this.moteId = moteId;
		this.motePort = motePort;
		this.fName = fname;
	}
	public String getFName() {
		return fName;
	}
	public void setFName(String name) {
		fName = name;
	}
	public int getMoteId() {
		return moteId;
	}
	public void setMoteId(int moteId) {
		this.moteId = moteId;
	}
	public String getMotePort() {
		return motePort;
	}
	public void setMotePort(String motePort) {
		this.motePort = motePort;
	}
	
}
