import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a helper class to data logger. In order to keep track of motes while a job is running on them. If a mote is removed, when a job is running its status is written to a file.
 * This get information of the motes connected to the PC and check if this mte is connected to it or not 
 * 
 *
 */
public class DLHelper extends Thread {
private String fName;
private String motePort;
private int moteId;
private boolean alive;
/**
 * Returns a dlh helper, which checks status of the mote, on the mentioned mote port 
 * @param moteId : id of the mote to be monitored
 * @param fname : filename to which status has to be written
 * @param motePort : usbport to which mote is connected
 */
public DLHelper(int moteId, String fname, String motePort) {
	
	this.fName = fname;
	this.motePort = motePort;
	this.moteId = moteId;
	alive = true;
}
/**
 * this kills this thread
 *
 */
public void kill() {
	alive = false;
	this.destroy();
}
public void run() {
	BufferedWriter bw=null;
	while(alive){
	if(MoteTracker.ConnectedMotes != null){
		System.out.println("connected motes size is "+MoteTracker.ConnectedMotes.size() );
		int i=0;
		for( i=0;i<MoteTracker.ConnectedMotes.size();i++) {
			if(((String)MoteTracker.ConnectedMotes.elementAt(i)).equals(this.motePort)) break;
		}
		if(i<MoteTracker.ConnectedMotes.size())  {
			try{
			bw = new  BufferedWriter(new FileWriter(new File(fName)));
			bw.write("\n"+"mote "+moteId+" id is no more connected to the computer" );
			}catch(Exception e)
			{
				
			}
		}
//		if(!MoteTracker.ConnectedMotes.contains(this.motePort)) {
//			try{
//				bw = new  BufferedWriter(new FileWriter(new File(fName)));
//				bw.write("\n"+"mote "+moteId+" id is no more connected to the computer" );
//			}catch(IOException e){
//				
//			}
		}
	}
	try{
		if(bw!=null ) bw.close();
	Thread.sleep(60000);
	}catch(Exception e){
		
	}
	}
}



