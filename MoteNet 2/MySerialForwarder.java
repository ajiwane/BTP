import java.io.IOException;

import net.tinyos.sf.SerialForwarder;


public class MySerialForwarder {

	private String port;
	private String comPort;
	private SerialForwarder sf;
	private boolean STATE;
	public MySerialForwarder(String port,String comPort){
		this.port =port;
		this.comPort = comPort;
		sf = null;
	}
	public void stamp(String s) {
		System.out.println("MySerialForwarder :  "+s);
	}
	public void start() throws Exception{
		try{
			STATE =false;
		String[] arg = new String[5];
		  arg[0] = "-no-output";
		  arg[1] = "-comm";
		  arg[2] = "serial@"+this.comPort+":telosb";
		  arg[3] = "-port";
		  arg[4] = this.port;
	      sf = new SerialForwarder(arg);
	      //sf.startListenServer();
	      STATE = true;
	     Thread.sleep(1000);
		}catch(Exception e){
			stamp("Exception occured while starting serial forwarder");
			throw e;
		}
		
	}
	public void stop() {
		sf.stopListenServer();
		//sf.
		sf  = null;
	}
	public boolean check() {
	if(sf== null) return false;
	return STATE;
	
	}
}