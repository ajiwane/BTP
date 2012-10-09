import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
/**
 * This class copies daa files and Error log files to the minor node. When the scheduled time of job has reached is end, first data Loggers are stopped and this thread is started.  
 * 
 *
 */

public class DataCombiner extends Thread {
	
	// Vector of path of files to be copied Assumes that last element is error file 
	
	Vector Paths;
	MinorNode mn;
	String DFolder;
	int JobId;
	BufferedWriter bw;
	/**
	 * Creates a new DataCombiner 
	 * @param Paths : vector of paths of files to be copied
	 * @param mn  : majornode details to which files are to be copied. 
	 * @param DFolder : destination folder on the major node
	 * @param jobId : id of the job
	 */
	public DataCombiner(Vector Paths, MinorNode mn,String DFolder, int jobId){
		
		this.Paths =Paths;
		this.mn = mn;
		this.DFolder = DFolder;
		this.JobId = jobId;
		try{
			if(Paths!= null&& Paths.size()>0)
			bw = new BufferedWriter(new FileWriter(new File((String)Paths.elementAt(Paths.size()-1)),true));
		}catch(IOException e ){
			stamp("Error while opening buffered writer ");
		}
	}
	
	public void run() {
		CommandExecuter ce = new CommandExecuter(this.JobId, this.mn);
		
		for(int i=0;i<Paths.size();i++) {
			if(i == Paths.size()-1) 
				try{bw.close(); bw =null;}catch(IOException e){
					stamp("Error copying Error log file to main comp");
				}
			Vector args = new Vector();
			args.add("-r");
			args.add(Trigger.MYUNAME+"@"+Trigger.IPADDRESS+":"+(String)Paths.elementAt(i));
			args.add(this.DFolder+"\n");
			args.add(Trigger.MYPASSWORD+"\n");
			try{
				System.out.println(args.elementAt(2));
			System.out.println("copying file  "+Paths.elementAt(i));
			ce.executeCommand("scp", args);
			System.out.println("copied file  "+Paths.elementAt(i));
			}catch(Exception e){
				
				stamp("Error in copying data file");
				try{
					if(bw!= null) {
					bw.write("couldn not copy data file "+(String)Paths.elementAt(i)+" to major node\n");	
					}
				}catch(Exception ee){
					stamp("Error in logging failure of the data file");
				}
				
			}
			
		}
		stamp("Wrriten succesfully");
		
	}
	public void stamp(String s) {
		System.out.println("Data Combiner  "+s);
	}
	
	public static void main(String args[]) throws Exception
	{
//		Vector Path = new Vector();
//		Trigger.MYUNAME = "naresh";
//		Trigger.IPADDRESS = "10.129.112.142";
//		Trigger.MYPASSWORD = "naresh123";
//		Path.add("/home/ashwin/motenet/ashwin/job9/data_mote_1");
//		Path.add("/home/ashwin/motenet/ashwin/job9/data_mote_2");
//		Path.add("/home/ashwin/motenet/ashwin/job9/ErrorLog");
//		MinorNode mn = new MinorNode("10.129.50.149","ashwin","ashwin123");
//		DataCombiner dc = new DataCombiner(Path,mn,"/home/ashwin/motenet/ashwin/job9/data",9);
//		dc.start();
//		while(true){
//			
//		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/ashwin/motenet/ashwin/job9/ErrorLog"),true));
		bw.write("line one\n");
		bw.close();
		bw = new BufferedWriter(new FileWriter(new File("/home/ashwin/motenet/ashwin/job9/ErrorLog"),true));
		bw.write("line two");
		bw.close();
	}
	
}
