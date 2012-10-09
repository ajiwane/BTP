import java.util.Vector;

import com.jscape.inet.ssh.SshConnectedEvent;
import com.jscape.inet.ssh.SshDataReceivedEvent;
import com.jscape.inet.ssh.SshDisconnectedEvent;
import com.jscape.inet.ssh.SshListener;
import com.jscape.inet.ssh.SshSession;

/**
 * Command executer is for execution of commands on the remote machine. It maintains an sshsession with the remote computer
 * 
 *
 */
public class CommandExecuter implements SshListener{

	private boolean connected = false;
	private SshSession sess = null;
	int jobId;
	MinorNode m;
	/**
	 * This creates a command executer for the remote computer m
	 * @param jobId 
	 * @param m : this includes details of computer on which command are to be executed.
	 */
	public CommandExecuter(int jobId, MinorNode m) {
		this.jobId = jobId;
		this.m = m;
		initialize();
	}
	
	/**
	 * this executes the given command
	 * @param command
	 * @param args : arguments for the command
	 * @throws Exception
	 */
	public void executeCommand (String command, Vector args) throws Exception {
		try{
		String fc=command;
		if(command.equals("mkdir")) {
			fc = fc+" "+ (String)args.elementAt(0);
			sess.send(fc);
		}
		else if(command.equals("scp")){
			fc = fc + " "+args.elementAt(0)+ " "+args.elementAt(1)+" "+args.elementAt(2);
			sess.sendRaw(fc);
			
			//System.out.println(args.elementAt(3));
			fc = (String)args.elementAt(3);
			Thread.sleep(4000);
			sess.send(fc);
//			System.out.println("succesfully executed");
		}
		
		
		
		}catch( Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * Closes sshSession with the computer.
	 *
	 */
	public void close(){
		try{
			sess.disconnect();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * returns true if it is connected to the remote computer.
	 */
	public void connected(SshConnectedEvent ev) {
	     //System.out.println("Connected: " + ev.getHost());
	     connected = true;
	   }
	/**
	 * This function is called when ever there is some data received from the 
	 */
	public void dataReceived(SshDataReceivedEvent ev) {
	     // send data received to console
	    //System.out.print(ev.getData());
		
	   }
	
	
	
	public void disconnected(SshDisconnectedEvent ev) {
	     System.out.println("Disconnected: " + ev.getHost() + ". Press Enter to exit");
	     connected = false;
	   }
	/**
	 * initializes ssh session and it is called from constructor class
	 *
	 */
	public void initialize() {
		try{
			sess = new SshSession(m.getIpAddress(),m.getUsername(),m.getPassword());
			
			sess.addSshListener(this);
			
			sess.connect();
			
			
			}catch(Exception e){
				
				e.printStackTrace();
			}
//			finally {
//				
//			       try {
//			           if(connected) {
//			        	   sess.disconnect();
//			           }
//			         } catch(Exception e) {
//			        	 
//			 			e.printStackTrace();
//			         }      
//			       }
	}
}
