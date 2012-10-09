import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * this class has all triggers
 * 
 */
public class Trigger extends Thread{
	/**
	 * minornode motestat directory path
	 */
	public static String HOMEDIR;
	/**
	 *minornode ipaddress 
	 */
	public static String IPADDRESS;
	/**
	 * path of the default main file in the minornode
	 */
	public static String DEFAULTJOBFILE ;
	/**
	 * path of the defaultjob folder in the minornode
	 */
	public static String DEFAULTJOBFOLDER;
	/**
	 * name of the defaultjob class file
	 */
	public static String DEFAULTJOBCLASSNAME;
	/**
	 * username of the minornode
	 */
	public static String MYUNAME;
	/**
	 * password of the minornode
	 */
	public static String MYPASSWORD;
	/**
	 * username of the central server
	 */
	public static String MNUNAME;
	/**
	 * password of the central server
	 */
	public static String MNPASSWORD;
	/**
	 * password of the central server
	 */
	public static String MNIPADRESS;
	/**
	 * path of the motestat folder in the central server
	 */
	public static String MNHOMEFOLDER;
	public Connection dbConn;
	/**
	 * This is a vector used to store all jobs scheduled  next 
	 */
	public static Vector jobsToBeRun = new Vector();
	/**
	 * intializes the global names
	 *
	 */
	public void initialize() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("minorNodeDetails"));
			HOMEDIR=br.readLine();
			IPADDRESS = br.readLine();
			MYUNAME = br.readLine();
			MYPASSWORD = br.readLine();
			MNIPADRESS = br.readLine();
			MNUNAME = br.readLine();
			MNPASSWORD = br.readLine();
			MNHOMEFOLDER = br.readLine();
			DEFAULTJOBFILE =  Trigger.HOMEDIR+"motestat/main.exe";
			DEFAULTJOBFOLDER = HOMEDIR+"motestat/";
			DEFAULTJOBCLASSNAME = "StatusMessage";
			try{
				dbConn = DBConnection.getConnection();
			}catch(Exception e) {
				stamp("error getting datbase connection in class Runcheckscheduled job");
			}
		}catch(IOException e){
			System.out.println(" Error in reading file minornode details in class Trigger");
			MoteNet.STOP = true;
			stamp("shutting down all threads by system.exit");
			System.exit(0);
		}
				
	}
	public void stamp(String s) {
		System.out.println(s);
	}
	/**
	 * checks if the minornode is new to the motelab, if its new then it enters the minornode information in the database
	 */
	public void updateMinorNode(){
		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT distinct ipaddress FROM minornodes");
			ResultSet s = query.executeQuery();
			while(s.next()){
				String ip = s.getString("ipaddress");
				if(ip.equals(IPADDRESS))
					return;
			}
			query = dbConn.prepareStatement("INSERT INTO minornodes values (?, ?, ?, ?)");
			query.setString(1, MYUNAME);
			query.setString(2, MYPASSWORD);
			query.setString(3, HOMEDIR);
			query.setString(4, IPADDRESS);
		}catch(Exception e){
			System.err.println(e);
		}
	}
	public void run(){
		initialize();
		updateMinorNode();
		UploadDefaultOnAll ud =new UploadDefaultOnAll();
		ud.installDefault();
		new MoteTracker().start();
		new CheckAndSetNewjobs().start();
		new RunScheduledJob().start();
	}
}
