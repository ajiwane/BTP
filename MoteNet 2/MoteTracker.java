import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import com.jscape.inet.ssh.transport.Message;
import com.sun.corba.se.impl.encoding.CodeSetConversion.CTBConverter;

import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import net.tinyos.tools.MsgReader;
import net.tinyos.util.PrintStreamMessenger;

/**
 * This class keeps tracks of the mote attached to the minor node and update their status in the database.
 * 
 * 
 *
 */
public class MoteTracker  extends Thread implements net.tinyos.message.MessageListener

{

	private int SLEEPTIME = 60000;
	/**
	 * moteIf for listening to the motes
	 */
	private MoteIF moteIF;
	/**
	 * Phoenix source of the MOteIf
	 */
	private PhoenixSource PS;
	/**
	 * contains all motes connected to this computer
	 */
	private Vector allmotes;
	/**
	 * all motes on which job is not running now 
	 */
	private Vector notrmotes;

	public Connection dbConn;
	public final int SLOTSIZE = 5;
	/**
	 * This is vector of al messages tracked by this 
	 */
	private Vector Messages;
	/**
	 * the list of current trackers
	 */
	private Vector  CMoteTrackers;
	/**
	 * mote which this is tracking
	 */
	private MoteJob Mote;
	/**
	 * parameter in minutes for getting free motes.
	 */
	private int paraMin = 2;
	/**
	 *current connected motes to the minor node 
	 * 
	 */
	public static Vector ConnectedMotes;
	public Vector getCMoteTrackers() {
		return CMoteTrackers;
	}
	public void setCMoteTrackers(Vector moteTrackers) {
		CMoteTrackers = moteTrackers;
	}
	public Vector getMessages() {
		return Messages;
	}
	public void setMessages(Vector messages) {
		Messages = messages;
	}
	public MoteJob getMote() {
		return Mote;
	}
	public void setMote(MoteJob mote) {
		Mote = mote;
	}
	public void initialize(){
		try{
			dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in classUpload Default job");
			e.getMessage();
			e.printStackTrace();
		}
	}
	public void stamp(String s){
		System.out.println("MoteTracker:  "+s);
	}
	public MoteTracker(){
		PS =null;
		moteIF=null;
	}
	public void close() {
		if(PS != null) {
			PS.shutdown();
			moteIF = null;
		}
	}

	public void startPS() {
		if(PS == null) return;
		else PS.start();
	}
	/**
	 * Starts mote tracker thread, which starts parsing motelist command and collecting packets at the mote.
	 * @param source
	 * @throws Exception
	 */
	public MoteTracker(String source) throws Exception {
		if (source != null) {
			moteIF = new MoteIF(BuildSource.makePhoenix(source, PrintStreamMessenger.err));
		}
		else {
			moteIF = new MoteIF(BuildSource.makePhoenix(PrintStreamMessenger.err));
		}
		Messages = new Vector();
		File f = new File(Trigger.DEFAULTJOBFOLDER);
		URL[] u = new URL[]{f.toURL()};
		ClassLoader cl = new URLClassLoader(u);// ClassLoader(u);
		Class cc = Class.forName("StatusMessage",false,cl);
		Object p = cc.newInstance();
		moteIF.registerListener((net.tinyos.message.Message)p,this);
	}
	/**
	 * returns status of motetracker
	 * 
	 */
	public boolean getStatus() {
		boolean b =true;
		if(PS ==null) b=false;
		//if(PS.)
		return b;
	}
	/**
	 * rertuns mote tracker for the motejob given by mj
	 * @param mj
	 * @throws Exception
	 */
	public MoteTracker(MoteJob mj) throws Exception {
		String source = mj.getComPort();
		Mote = mj;
		if (source!= null) {
			source = "serial@"+mj.getComPort()+":telosb";
			PS = BuildSource.makePhoenix(source, null);

			if(PS != null){
//				PS.awaitStartup();
				moteIF = new MoteIF(PS);
			}
			else {
				moteIF = null;
			}
		}
		else {
			PS = null;
			moteIF = new MoteIF(BuildSource.makePhoenix(PrintStreamMessenger.err));
		}
		Messages = new Vector();
		File f = new File(Trigger.DEFAULTJOBFOLDER);
		URL[] u = new URL[]{f.toURL()};
		ClassLoader cl = new URLClassLoader(u);// ClassLoader(u);
		Class cc = Class.forName("StatusMessage",false,cl);
		Object p = cc.newInstance();
		moteIF.registerListener((net.tinyos.message.Message)p,this);

	}
/**
 * stops the current trackers 
 *
 */
	public void stopCMote() {
		if(CMoteTrackers == null) return;
		for(int i=0;i<CMoteTrackers.size();i++){
			MoteTracker mt = (MoteTracker)CMoteTrackers.elementAt(i);
			mt.close();
		}
	}
	public void run() {
		initialize();
		while(true){
			stopCMote();
			try{
				updateMoteConnection();
			}catch(Exception e){}
			update();
			try{
				Thread.sleep(45000);
			}catch(Exception e){}

			setUpdate();
			try{
				Thread.sleep(SLEEPTIME);
			}catch(Exception e){}
		}

	}
	/**
	 * This will update the condition of all the motes
	 *
	 */
	public void setUpdate() {
		Vector v = new Vector();
		for(int i =0;i<CMoteTrackers.size();i++) {
			MoteTracker mt = (MoteTracker)CMoteTrackers.elementAt(i);
			if(!mt.getStatus()){
				stamp("Couldnot read packets  on mote " +mt.getMote());
				MoteJob mj =mt.getMote();
				mj.setCondition(0);
			}
			if(mt.getMessages().size()>0){
				stamp("status messages receveid on mote " +mt.getMote());
				MoteJob mj =mt.getMote();
				mj.setCondition(1);
				v.add(mj);
			}
			else{
				MoteJob mj = mt.getMote();
				System.out.println("No status messages receveid on mote " +mt.getMote());
				mj.setCondition(0);
				v.add(mj);
			}
			mt.close();
		}
		for(int i=0;i<v.size();i++) {
			MoteJob mj = (MoteJob)v.elementAt(i);
			try{
				PreparedStatement query = dbConn.prepareStatement("UPDATE  motes SET condition = ?" +
				" WHERE moteid = ?");
				System.out.println("setting condtion of mote \t"+ mj);
				System.out.println("condtion set to"+ mj.getCondition());

				query.setInt(1,mj.getCondition() );
				query.setInt(2, mj.getMoteId());
				query.execute();

			}catch(SQLException e){
				stamp("Exception occured while changing condition of the motes ");
				e.printStackTrace();
			}
		}

	}
	
	public void update() {
		getAllMotes();
		//getNotRunningJobMotes();
		getFreeMotes();
		CMoteTrackers = null;
		CMoteTrackers = new Vector();
		for(int i=0;i<notrmotes.size();i++) {
			try{
				MoteTracker mt =new MoteTracker((MoteJob)notrmotes.elementAt(i));
//				mt.startPS();
				CMoteTrackers.add(mt);
			}catch(Exception e) {

			}
		}

	}

	public void getFreeMotes() {
		notrmotes = new Vector();
		int currentid = -1;
		java.util.Date cd,pd,fd;
		cd= new java.util.Date();
		pd = new java.util.Date(cd.getTime());
		fd = new java.util.Date(cd.getTime());
		int modMins = cd.getMinutes();
		pd.setMinutes(modMins-paraMin);
		pd.setSeconds(0);
		fd.setMinutes(modMins+paraMin);
		fd.setSeconds(0);

		modMins = pd.getMinutes();
		modMins = modMins - (modMins%SLOTSIZE);
		pd.setMinutes(modMins);
		pd.setSeconds(0);

		modMins = fd.getMinutes();
		modMins = modMins - (modMins%SLOTSIZE);
		fd.setMinutes(modMins);
		fd.setSeconds(0);


		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT jobid FROM slots" +
			" WHERE sdate = ? AND stime = ?");

			query.setDate(1, new java.sql.Date(pd.getTime()));
			query.setTime(2, new java.sql.Time(pd.getHours(),pd.getMinutes(),pd.getSeconds()));
			ResultSet s = query.executeQuery();
			while(s.next()){
				currentid = s.getInt("jobid");
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}

		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT schedules.moteid " +
			" FROM schedules, motes WHERE schedules.moteid = motes.moteid AND schedules.jobid = ? AND motes.ipaddress = ?");
			query.setInt(1,currentid);
			query.setString(2,Trigger.IPADDRESS );
			ResultSet s = query.executeQuery();
			for(int i =0;i<allmotes.size();++i){
				MoteJob mj = (MoteJob)allmotes.elementAt(i);
				notrmotes.addElement(mj);
			}
			while(s.next()){
				int check = 0;
				int id = s.getInt("moteid");
				for(int i =0;i<notrmotes.size();++i){
					MoteJob mj = (MoteJob)notrmotes.elementAt(i);
					if(mj.getMoteId()==id){
						notrmotes.removeElementAt(i);
						break;	
					}	
				}
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}

		currentid = -1;
		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT jobid FROM slots" +
			" WHERE sdate = ? AND stime = ?");

			query.setDate(1, new java.sql.Date(fd.getTime()));
			query.setTime(2, new java.sql.Time(fd.getHours(),fd.getMinutes(),fd.getSeconds()));
			ResultSet s = query.executeQuery();
			while(s.next()){
				currentid = s.getInt("jobid");
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}

		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT schedules.moteid " +
			" FROM schedules, motes WHERE schedules.moteid = motes.moteid AND schedules.jobid = ? AND motes.ipaddress = ?");
			query.setInt(1,currentid);
			query.setString(2,Trigger.IPADDRESS );
			ResultSet s = query.executeQuery();

			while(s.next()){
				int check = 0;
				int id = s.getInt("moteid");
				for(int i =0;i<notrmotes.size();++i){
					MoteJob mj = (MoteJob)notrmotes.elementAt(i);
					if(mj.getMoteId()==id){
						notrmotes.removeElementAt(i);
						break;	
					}	
				}
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}


	}
	public void getNotRunningJobMotes() {
		notrmotes = new Vector();
		int currentid = -1;
		java.util.Date cd;
		cd= new java.util.Date();
		int modMins = cd.getMinutes();
		modMins = modMins - (modMins%SLOTSIZE);
		cd.setMinutes(modMins);
		cd.setSeconds(0);
		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT jobid FROM slots" +
			" WHERE sdate = ? AND stime = ?");

			query.setDate(1, new java.sql.Date(cd.getTime()));
			query.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
			ResultSet s = query.executeQuery();
			while(s.next()){
				currentid = s.getInt("jobid");
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}

		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT schedules.moteid " +
			" FROM schedules, motes WHERE schedules.moteid = motes.moteid AND schedules.jobid = ? AND motes.ipaddress = ?");
			query.setInt(1,currentid);
			query.setString(2,Trigger.IPADDRESS );
			ResultSet s = query.executeQuery();
			for(int i =0;i<allmotes.size();++i){
				MoteJob mj = (MoteJob)allmotes.elementAt(i);
				notrmotes.addElement(mj);
			}
			while(s.next()){
				int check = 0;
				int id = s.getInt("moteid");
				for(int i =0;i<notrmotes.size();++i){
					MoteJob mj = (MoteJob)notrmotes.elementAt(i);
					if(mj.getMoteId()==id){
						notrmotes.removeElementAt(i);
						break;	
					}	
				}
			}

		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}
	}

	public void getAllMotes() {
		try{
			allmotes = new Vector();
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid, moteport, condition FROM motes" +
			" WHERE ipaddress = ? ORDER BY moteid");
			query.setString(1,Trigger.IPADDRESS);
			ResultSet s = query.executeQuery();
			while(s.next()){
				if(s.getInt("condition")!=2){
				MoteJob mj = new MoteJob();
				mj.setMoteId(s.getInt("moteid"));
				mj.setComPort(s.getString("moteport"));
				mj.setMainFile(Trigger.DEFAULTJOBFILE);
				mj.setCondition(s.getInt("condition"));
				allmotes.addElement(mj);
				}
			}
			System.out.println("number of motes connected are "+allmotes.size()+"/n and mote details  are");
			for(int i=0;i<allmotes.size();i++)
				System.out.println((MoteJob)allmotes.elementAt(i));
		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}
	}
	public void messageReceived(int to, net.tinyos.message.Message message) {
		Messages.add(message);
//		System.out.println(" status mesaage received on and Message size is "+ Messages.size() );
	}


	private static void usage() {
		System.err.println("usage: MsgReader [-comm <source>] message-class [message-class ...]");
	}

	private void addMsgType(net.tinyos.message.Message msg) {
		moteIF.registerListener(msg,this);
	}

	public static void main(String[] args) throws Exception {
		String source = null;
		Vector v = new Vector();
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-comm")) {
					source = args[++i];
				}
				else {
					String className = args[i];
					try {
						Class c = Class.forName(className);
						Object packet = c.newInstance();
						Message msg = (Message)packet;
						v.addElement(msg);
					}
					catch (Exception e) {
						System.err.println(e);
					}
				}
			}
		}
		else if (args.length != 0) {
			usage();
			System.exit(1);
		}


	}
	public MoteIF getMoteIF() {
		return moteIF;
	}
	public void setMoteIF(MoteIF moteIF) {
		this.moteIF = moteIF;
	}
	public PhoenixSource getPS() {
		return PS;
	}
	public void setPS(PhoenixSource ps) {
		PS = ps;
	}

	public void updateMoteConnection() throws Exception{
		String []commands = new String[]{"motelist"};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			int ret = child.waitFor();
			InputStream in = child.getInputStream();
			String output = Upload.getString(in);
			in.close();
			if(output.equals("No devices found.\n")) {
				try {
					PreparedStatement query = dbConn.prepareStatement("UPDATE motes SET condition = 2 WHERE ipaddress = ?");
					query.setString(1,Trigger.IPADDRESS);
					query.execute();

				}catch(Exception e){
					stamp("updateMoteConnection: error while updating mote conditions");
					throw e;
				}
			}
			else {
				int ind = output.indexOf("/dev/");
				Vector moteports = new Vector();
				while(ind!=-1){
					output = output.substring(ind);
					ind = output.indexOf(" ");
					String port = output.substring(0, ind);
					moteports.add(port);
					output = output.substring(ind+1);
					ind = output.indexOf("/dev/");
				}
				ConnectedMotes = moteports;
				try {
					PreparedStatement query = dbConn.prepareStatement("SELECT moteport, condition FROM motes " +
					"WHERE ipaddress = ?");
					query.setString(1,Trigger.IPADDRESS);
					ResultSet r = query.executeQuery();
					while(r.next()){
						String port = r.getString("moteport");
						if(moteports.contains(port)){
							if(r.getInt("condition")==2){
								query = dbConn.prepareStatement("UPDATE motes SET condition = 1 WHERE ipaddress = ? AND moteport= ?");
								query.setString(1,Trigger.IPADDRESS);
								query.setString(2,port);
								query.execute();
							}
							int index = moteports.indexOf(port);
							moteports.remove(index);
						}
						else{
							try {
								query = dbConn.prepareStatement("UPDATE motes SET condition = 2 WHERE ipaddress = ? AND moteport= ?");
								query.setString(1,Trigger.IPADDRESS);
								query.setString(2,port);
								query.execute();

							}catch(Exception e){
								stamp("updateMoteConnection: error while updating mote conditions");
								throw e;
							}
						}
					}

					for(int i=0;i<moteports.size();++i){
						String port = (String)moteports.elementAt(i);
						//dbConn.setAutoCommit(false);
						DBConnection.setId("moteid", dbConn);

						int mid= DBConnection.getId("moteid", dbConn);
						while (mid== -1) {
							mid= DBConnection.getId("moteid", dbConn);
							stamp("mote while loop");
						}
						query = dbConn.prepareStatement("INSERT INTO motes VALUES (?, ?, ?, ?)");
						query.setInt(1,mid);
						query.setString(2,port);
						query.setInt(3, 1);
						query.setString(4, Trigger.IPADDRESS);
						query.execute();

						//dbConn.commit();
						//dbConn.setAutoCommit(true);
					}
				}catch(Exception e){
					stamp("updateMoteConnection: error while updating mote conditions");
					throw e;
				}
			}
		}
		catch (Exception e) {
			throw e;
		}
	}
/*
	public int getNewMoteId() throws Exception{
		int id = -1;
		int tmpid = 1;
		try {
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid FROM motes ORDER BY moteid");
			ResultSet r = query.executeQuery();
			while(r.next()){
				id = r.getInt("moteid");
				if(id!=tmpid)
					break;
				++tmpid;
			}
			query = dbConn.prepareStatement("UPDATE curid SET moteid = ?");
			query.setInt(1,id);
			query.execute();
		}catch(Exception e){
			stamp("getNewMoteId: error while getting new moteid");
			throw e;
		}
		return tmpid;
	}*/
}
