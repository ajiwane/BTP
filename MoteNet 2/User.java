
import java.sql.*;
import java.io.*;
import java.util.*;

import com.jscape.inet.ssh.SshSession;

public class User {
	/**
	 * total number of motes connected to the motenet lab
	 */
	int NUMOFMOTES ;
	final int NUMOFSLOTS = 10;
	/**
	 * username of the central server machine
	 */
	final String SERVERUNAME= "ashwin";
	/**
	 * password of the central server machine
	 */
	final String SERVERPASSWD = "ashwin123";
	/**
	 * ipaddress of the central server machine
	 */
	final String SERVERIP = "10.129.50.149";
	/**
	 * path of the motenet directory
	 */
	final String HOMEDIR = "/home/ashwin/motenet/";
	Vector currSlotsToDisplay = null;
	Vector currAvailableSlots = null;
	/**
	 * vector containing moteconnectivity map of the lab
	 */
	static Vector moteconnMap = new Vector();
	/**
	 * username of the logged in user 
	 */
	public String uname;
	/**
	 * userid of the logged in user
	 */
	public int userid;
	public Connection dbConn;
	public boolean allmotes = false;
	public User(String uname,int userid){
		this.uname = uname;
		this.userid =userid;
		//don't connect before the job gets created. connect whenever we need to acces database
		try{
			dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in class USER");
			e.getMessage();
			e.printStackTrace();
		}
		//	handleOptions();
	}

	public void stamp(String s) {
		System.out.println("User "+s);
	}
/**
 * displays options to the user on console interface
 * @param s
 */
	public void displayOptions(String s){
		//G for general
		if(s.equals("G")) {
			System.out.println("\nOptions:");
			System.out.println("To create a job type 1 followed by enter");
			System.out.println("To edit a job type 2 followed by enter");
			System.out.println("To schedule a job type 3 followed by enter");
			System.out.println("To logout type 4 followed by enter");
			System.out.println("To see status of motes type 5 followed by enter");
			System.out.println("To view the data of jobs type 6 followed by enter");
			System.out.println("To view the connectivity map of motes type 7 followed by enter");
		}
		else if(s.equals("C")) {
			System.out.println("\nCreate Job:");
			System.out.println(
					"write description of the job in a file in following format\n"+
					"job name job description followed by\n"+
					"number of main files and number of class files in first and second lines follwed by path of the each file in different lines\n"+
					"type path of the file followed by enter"
			);
		}
		else if(s.equals("JD")) {
			System.out.println("\nJob Deatils:");
			System.out.println("write deatils of the job in a file in following format\n" +
					"path of a main file in one line and the moteids (separated by a single space) on which you want to run this main program in the next line\n"+
					"follow the above format for all the main files which you want to upload on motes\n"+
					"if you want to run the main program on all the motes then instead of writing all the moteids you can write \"allmotes\" \n"+
			"type path of the file followed by enter");
		}
	}

/**
 * reads files uploaded while creating the job and stores the information in vectors
 *
 */
	public void createJob() {
		try{
			displayOptions("C");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s =br.readLine();
			Job j = new Job();
			BufferedReader bf = new BufferedReader(new FileReader(new File(s)));

			j.setName(bf.readLine());
			j.setDescription(bf.readLine());

			Vector mainf = new Vector();
			Vector classf = new Vector();

			int i= Integer.parseInt(bf.readLine());
			int k = Integer.parseInt(bf.readLine());

			for(;i>0;i--){
				mainf.add(bf.readLine());
			}
			for(;k>0;k--){
				classf.add(bf.readLine());
			}

			j.setClassFiles(classf);
			j.setMainFiles(mainf);

			displayOptions("JD");
			br = new BufferedReader(new InputStreamReader(System.in));
			s =br.readLine();
			bf = new BufferedReader(new FileReader(new File(s)));

			Vector mainmotes = new Vector();
			for(int p=0;p<mainf.size();++p) {
				Vector tmp = null;
				mainmotes.add(tmp);
			}

			String line = null;
			while((line = bf.readLine()) != null) {
				int index = mainf.indexOf(line);
				Vector tmp = new Vector();
				line = bf.readLine();
				if(line == null) {
					System.out.println("Error in job details file, please check it.Bye");
					System.exit(0);

				}
				else if(line.equals("allmotes")) {
					allmotes = true;
					for(int p=1;p<=NUMOFMOTES;++p) {
						tmp.add(new Integer(p));
					}
				}
				else {
					StringTokenizer st = new StringTokenizer(line);
					while (st.hasMoreTokens()) {
						String id = st.nextToken();
						int p = Integer.parseInt(id);
						if(p>=1 && p<=NUMOFMOTES)
							tmp.add(new Integer(p));
						else {
							System.out.println("Error in job details file, moteid "+p+" does not exist.Bye");
							System.exit(0);
						}
					}
				}
				mainmotes.setElementAt(tmp, index);
			}

			j.setMainUploadMoteIds(mainmotes);

			commitJob(j);
			handleOptions();
		}catch(Exception e){
			stamp("createJob");
			e.printStackTrace();
		}
	}

	/**
	 * commits the job. Stores all the inforamtion of the job in the database
	 * @param j
	 */
	public void commitJob(Job j){
		try{
			
			DBConnection.setId("jobid", dbConn);

			int id= DBConnection.getId("jobid", dbConn);
			while (id== -1) {
				id= DBConnection.getId("jobid", dbConn);
				stamp("commitJob while loop");
			}

			j.setJobId(id);
			//store files in dir structure
			j = storeFiles(j);
			System.out.println("directory created");
			//store all the job details in db


			//jobs table
			dbConn.setAutoCommit(false);
			PreparedStatement query = dbConn.prepareStatement("INSERT INTO jobs VALUES (?, ?, ?)");
			query.setInt(1,j.getJobId());
			query.setString(2,j.getName());
			query.setString(3,j.getDescription());
			query.execute();

			//classfiles table
			Vector classes = j.getClassFiles();
			for(int i=0;i<classes.size();++i) {
				query = dbConn.prepareStatement("INSERT INTO classfiles VALUES ("+j.getJobId()+", '"+(String)classes.elementAt(i)+"')");
				query.execute();
			}

			//mainfiles table
			Vector mainfiles = j.getMainFiles();
			for(int i=0;i<mainfiles.size();++i) {
				query = dbConn.prepareStatement("INSERT INTO mainfiles VALUES ("+j.getJobId()+", '"+(String)mainfiles.elementAt(i)+"')");
				query.execute();
			}

			//schedules table
			Vector uploadids = j.getMainUploadMoteIds();
			for(int i=0;i<mainfiles.size();++i) {
				Vector tmp = (Vector)uploadids.elementAt(i);
				String mainfile = (String)mainfiles.elementAt(i);
				if(tmp!=null) {
					for(int ii=0;ii<tmp.size();++ii) {
						int moteid = ((Integer)tmp.elementAt(ii)).intValue();
						query = dbConn.prepareStatement("INSERT INTO schedules VALUES("+moteid+", "+j.getJobId()+", '"+mainfile+"')");
						query.execute();
					}
				}
			}

//			jobowners table
			query = dbConn.prepareStatement("INSERT INTO jobowners VALUES (?, ?)");
			query.setInt(1,userid);
			query.setInt(2, j.getJobId());
			query.execute();

			dbConn.commit();
			dbConn.setAutoCommit(true);
			System.out.println("\nJob "+j.getJobId()+" created successfully.");
			//DBConnection.closeConnection(dbConn);
			copyFiles(j.getJobId());


		}catch(Exception e){
			stamp("commitJob");
			e.printStackTrace();
		}
	}

	/**
	 * copies all files of the created job to all specified local machines using ssh
	 * @param jobId
	 */
	public void copyFiles(int jobId){
		Vector mns = getMinorNodes(jobId);
		try{
		for(int i=0;i<mns.size();++i) {
			MinorNode m = (MinorNode)mns.get(i);

			CommandExecuter ce = new CommandExecuter(jobId,m);

			Vector v = new Vector();
			v.add("-r");
			v.add(SERVERUNAME+"@"+SERVERIP+":"+HOMEDIR+this.uname+"/job"+jobId);
			v.add(m.getHomedir()+this.uname+"\n");
			v.add(SERVERPASSWD+"\n");
			ce.executeCommand("scp", v);

		}
		}catch(Exception e){
			stamp("error while copying files "+ e);
		}

	}
/**
 * returns all the minordes of the lab
 * @param jobId
 * @return
 */
	public Vector getMinorNodes(int jobId) {
		Vector moteJ = new Vector();
		try{

			PreparedStatement query = dbConn.prepareStatement("SELECT distinct minornodes.ipaddress, username, password, homedir" +
			" FROM minornodes, schedules, motes WHERE schedules.moteid = motes.moteid AND schedules.jobid = ? AND motes.ipaddress = minornodes.ipaddress ");
			query.setInt(1,jobId);
			ResultSet s = query.executeQuery();
			while(s.next()){
				MinorNode mn;

				mn = new MinorNode();
				mn.setIpAddress(s.getString("ipaddress"));
				mn.setUsername(s.getString("username"));
				mn.setPassword(s.getString("password"));
				mn.setHomedir(s.getString("homedir"));
				moteJ.add(mn);	


				//ErrorHandler.handleJobMoteError(moteE, this.userId, this.nextJobid);
			}
		}catch(Exception e){
			stamp("setmoteJVector");
			e.printStackTrace();
		}
		return moteJ;
	}
/**
 * stores files of the created job in the central server machine under proper directory structure
 * @param j
 * @return
 */
	public Job storeFiles(Job j){

		String jobdir = "/home/ashwin/motenet/"+this.uname+"/job"+j.getJobId()+"/";
		String[] commands = new String[]{"mkdir",jobdir};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			Thread.currentThread().sleep(1000);
		}catch(Exception e){
			stamp("storeFiles");
			e.printStackTrace();
		}
		String dataDir = jobdir+"data/";
		commands = new String[]{"mkdir",dataDir};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			Thread.currentThread().sleep(1000);
		}catch(Exception e){
			stamp("creating data directory");
			e.printStackTrace();
		}
		Vector mainfiles = j.getMainFiles();
		for(int i = 0; i<mainfiles.size();++i){
			commands = new String[]{"cp",(String)mainfiles.elementAt(i),jobdir};
			try{
				Process child = Runtime.getRuntime().exec(commands);
				Thread.currentThread().sleep(1000);
			}catch(Exception e){
				stamp("storeFiles");
				e.printStackTrace();
			}
		}
		Vector tmpmainfiles = new Vector();
		for(int i = 0; i<mainfiles.size();++i){
			String mf = (String)mainfiles.elementAt(i);
			int index = mf.lastIndexOf("/");
			if(index!=-1) {
				mf = mf.substring(index+1);
			}
			mf = jobdir+mf;
			tmpmainfiles.add(mf);
		}

		j.setMainFiles(tmpmainfiles);

		Vector classfiles = j.getClassFiles();
		for(int i = 0; i<classfiles.size();++i){
			commands = new String[]{"cp",(String)classfiles.elementAt(i),jobdir};
			try{
				Process child = Runtime.getRuntime().exec(commands);
				Thread.currentThread().sleep(1000);
				String cf = (String)classfiles.elementAt(i);
				int index = cf.lastIndexOf("/");
				if(index!=-1) {
					cf = cf.substring(index+1);
				}
				index = cf.lastIndexOf(".");
				cf = cf.substring(0, index);
				cf = jobdir+cf;
				commands = new String[]{"cp",(String)classfiles.elementAt(i),cf};
				child = Runtime.getRuntime().exec(commands);
				Thread.currentThread().sleep(1000);
			}catch(Exception e){
				stamp("storeFiles");
				e.printStackTrace();
			}
		}

		Vector tmpclassfiles = new Vector();
		for(int i = 0; i<classfiles.size();++i){
			String mf = (String)classfiles.elementAt(i);
			int index = mf.lastIndexOf("/");
			if(index!=-1) {
				mf = mf.substring(index+1);
			}
			mf = jobdir+mf;
			tmpclassfiles.add(mf);
		}

		j.setClassFiles(tmpclassfiles);

		return j;
	}

/**
 * in all schedules the job
 *
 */
	public void scheduleJob() {
		try{
			Vector currjobs = displayCurrentJobs();
			System.out.println("Type jobid you want to schedule followed by enters");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int jid = Integer.parseInt(br.readLine());
			if(currjobs.contains(new Integer(jid))) {
				handleSlots(jid);
				handleOptions();
			}
			else {
				System.out.println("Jobid "+jid+" does not exist. Try Again");
				scheduleJob();
			}
		}catch(Exception e){
			stamp("scheduleJob");
			e.printStackTrace();
		}
	}
/**
 * returns the vector containing all the jobs created by the user
 * @return
 */
	public Vector displayCurrentJobs() {
		try {
			Vector ret = new Vector();
			PreparedStatement query = dbConn.prepareStatement("SELECT jobs.jobid,jobname,jobdescription FROM jobowners,jobs WHERE " +
			"jobowners.jobid = jobs.jobid AND jobowners.userid = ?");
			query.setInt(1,userid);
			ResultSet r = query.executeQuery();
			System.out.println("\njobid \t jobname \t jobdescription");
			while(r.next()){
				int jobid = r.getInt("jobid");
				System.out.println(jobid+"\t"+r.getString("jobname")+"\t"+r.getString("jobdescription"));
				ret.addElement(new Integer(jobid));
			}
			return ret;

		}catch(Exception e){
			stamp("displaycurrentJobs");
			e.printStackTrace();
		}
		return null;
	}


	public void handleSlots(int jid) {
		try{
			java.util.Date date1 = new java.util.Date();
			String s = date1.toString();
			int min = date1.getMinutes();
			int rem = min%Slot.SLOTSIZEMIN;
			min = min + Slot.SLOTSIZEMIN - rem;
			date1.setMinutes(min);
			date1.setSeconds(0);
			java.util.Date startdate = new java.util.Date(date1.getTime()+Slot.AFTER30MINS);
			handleSlotsHelper(jid, startdate);	
		}catch(Exception e){
			stamp("handleSlots");
			e.printStackTrace();
		}
	}
/**
 * displays 10 slots starting 10 mins from the current time on the interface
 * and asks user to select a slot
 * @param jid
 * @param startdate
 */
	public void handleSlotsHelper(int jid, java.util.Date startdate) {

		try{
			currSlotsToDisplay = new Vector();
			for(int i=0;i<NUMOFSLOTS;++i) {
				java.util.Date tmpdate = new java.util.Date(startdate.getTime()+(i*Slot.SLOTSIZEMILLISEC));
				Slot tmp = new Slot(tmpdate);
				currSlotsToDisplay.addElement(tmp);
			}
			Slot sslot = (Slot)currSlotsToDisplay.elementAt(0);
			Slot eslot =  (Slot)currSlotsToDisplay.elementAt(currSlotsToDisplay.size()-1);

			displaySlots(sslot.getSDate(),sslot.getStartTime(),eslot.getSDate(),eslot.getStartTime());

			System.out.println("Type slotid on which you want to schedule the job followed by a enter");
			System.out.println("Or type \"moreslots\" to show next available slots");
			System.out.println("NOTE: Continuous slots will be considered as a one single major slot");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String ans = br.readLine();

			if(ans.equals("moreslots")) {
				startdate = new java.util.Date(eslot.getSDate().getTime()+Slot.SLOTSIZEMILLISEC);
				handleSlotsHelper(jid,startdate);
			}
			else {
				int slotselectedid = Integer.parseInt(ans);

				if (currAvailableSlots.contains(new Integer(slotselectedid))) {
					Slot selected = (Slot)currSlotsToDisplay.elementAt(slotselectedid);
					selected.setScheduledJodID(jid);
					commitSlot(selected);
				}
				else {
					System.out.println("Slot with slotid "+slotselectedid+" either does not exist or is already occupied.");
					System.out.println("Please select another slot.");
					Thread.currentThread().sleep(500);
					handleSlots(jid);
				}
			}
		} catch(Exception e){
			stamp("handleSlotsHelper");
			e.printStackTrace();
		}
	}
/**
 * this function schedules the job, stores all the information about the job schedule in the database
 * @param s
 */
	public void commitSlot(Slot s) {
		try{
			
			dbConn.setAutoCommit(false);
			PreparedStatement query = dbConn.prepareStatement("INSERT INTO slots VALUES (?, ?, ?)");
			query.setInt(1,s.getScheduledJodID());
			query.setDate(2,new java.sql.Date(s.getSDate().getTime()));
			query.setTime(3,s.getStartTime());
			query.execute();

			dbConn.commit();
			dbConn.setAutoCommit(true);
			String dir= (new java.sql.Date(s.getSDate().getTime())).toString()+"_"+s.getStartTime();
			String jobdir = UserHandler.HOMEDIR+this.uname+"/job"+s.getScheduledJodID()+"/data/"+dir+"/";
			String[] commands = new String[]{"mkdir",jobdir};
			try{
				Process child = Runtime.getRuntime().exec(commands);
				Thread.currentThread().sleep(1000);
			}catch(Exception e){
				stamp("storeFiles");
				e.printStackTrace();
			}
			//DBConnection.closeConnection(dbConn);
			System.out.println("\nJod "+s.getScheduledJodID()+" scheduled successfully.");

		}catch(Exception e){
			stamp("commitSlot");
			e.printStackTrace();
		}
	}
/**
 * display slots from time d1,t1 till time d2,t2
 * @param d1
 * @param t1
 * @param d2
 * @param t2
 */
	public void displaySlots(java.util.Date d1,Time t1,java.util.Date d2,Time t2) {
		try{			
			PreparedStatement query = dbConn.prepareStatement("SELECT * FROM slots WHERE ((sdate=? AND stime >=?) OR sdate> ?) AND ((sdate=? AND stime <=?) OR sdate< ?) " +
			"ORDER BY sdate ASC,stime ASC");
			query.setDate(1, new java.sql.Date(d1.getTime()));
			query.setTime(2, t1);
			query.setDate(3, new java.sql.Date(d1.getTime()));
			query.setDate(4, new java.sql.Date(d2.getTime()));
			query.setTime(5, t2);
			query.setDate(6, new java.sql.Date(d2.getTime()));
			ResultSet r = query.executeQuery();

			boolean over = false;
			currAvailableSlots = new Vector();
			System.out.println("\nslotid \t jobid \t slotdate \t slottime");
			int i = 0;

			Slot tmp = (Slot)currSlotsToDisplay.elementAt(i);
			java.sql.Date tmpdate = new java.sql.Date(tmp.getSDate().getTime());
			Time tmptime = tmp.getStartTime();

			while(r.next()) {
				java.sql.Date slotdate = r.getDate("sdate");
				Time slottime = r.getTime("stime");
				while((!((tmpdate.getDate()==slotdate.getDate()) 
						&& (tmpdate.getMonth()== slotdate.getMonth())
						&& (tmpdate.getYear()==slotdate.getYear()))) 
						|| (!tmptime.equals(slottime))) {

					currAvailableSlots.addElement(new Integer(i));
					System.out.println(i+"\t -- \t "+tmpdate.toString()+"\t"+ tmptime);
					++i;
					if(i<NUMOFSLOTS) {
						tmp = (Slot)currSlotsToDisplay.elementAt(i);
						tmpdate = new java.sql.Date(tmp.getSDate().getTime());
						tmptime = tmp.getStartTime();
					}
					else {
						over = true;
						break;
					}
				}

				if(over)
					break;

				System.out.println(i+" \t "+r.getInt("jobid")+" \t "+slotdate.toString()+" \t "+ slottime);
				++i;
				if(i<NUMOFSLOTS) {
					tmp = (Slot)currSlotsToDisplay.elementAt(i);
					tmpdate = new java.sql.Date(tmp.getSDate().getTime());
					tmptime = tmp.getStartTime();
				}
				else {
					break;
				}
			}

			currAvailableSlots.addElement(new Integer(i));
			System.out.println(i+"\t -- \t "+tmpdate.toString()+"\t"+ tmptime);
			++i;
			while(i<NUMOFSLOTS) {
				tmp = (Slot)currSlotsToDisplay.elementAt(i);
				tmpdate = new java.sql.Date(tmp.getSDate().getTime());
				tmptime = tmp.getStartTime();
				currAvailableSlots.addElement(new Integer(i));
				System.out.println(i+"\t -- \t "+tmpdate.toString()+"\t"+ tmptime);
				++i;
			}

			//long numslots = d2.getTime()+t2.getTime()

		}catch(Exception e){
			stamp("displaySlots");
			e.printStackTrace();
		}
	}

	public void logout(){
		try{
			dbConn.close();
			System.out.println("\nBye");
			//System.exit(0);

			//this.destroy();
			new UserHandler().start();

		}catch(Exception e){
			stamp("logout");
			e.printStackTrace();
		}
	}

	public void setNumofMotes() {
		try {
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid FROM curid");
			ResultSet r = query.executeQuery();
			while(r.next()){
				NUMOFMOTES = r.getInt("moteid");
			}

		}catch(Exception e){
			stamp("setNumofMotes");
			e.printStackTrace();
		}
	}
/**
 * displays the motes status of the lab
 *
 */
	public void showMoteStatus() {
		Vector motes = new Vector();
		try {
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid, moteport, condition, ipaddress FROM motes");
			ResultSet r = query.executeQuery();
			while(r.next()){
				MoteJob mj = new MoteJob();
				mj.setMoteId(r.getInt("moteid"));
				mj.setComPort(r.getString("moteport"));
				mj.setCondition(r.getInt("condition"));
				mj.setIpAddress(r.getString("ipaddress"));
				motes.addElement(mj);
			}

			System.out.println("MoteId \t Moteport \t Ipaddress \t Status \n");
			for(int i =0;i<motes.size();++i){
				MoteJob mj = (MoteJob)motes.elementAt(i);
				int condition = mj.getCondition();
				String status ="";
				if(condition == 0){
					status = "DISABLED";
				}
				else if(condition == 1){
					status = "OK";
				}
				else if(condition == 2){
					status = "REMOVED";
				}
				System.out.println(mj.getMoteId()+" \t "+mj.getComPort()+" \t "+mj.getIpAddress()+" \t "+status);
			}

			handleOptions();
		}catch(Exception e){
			stamp("showMoteStatus");
			e.printStackTrace();
		}
	}
	/**
	 * displays the path of the folders in which completed job data is stored
	 *
	 */
	public void showJobData() {
		try{
			java.util.Date cd = new java.util.Date();
			int modMins = cd.getMinutes();
			modMins = modMins - (modMins%UserHandler.SLOTSIZE);
			modMins = modMins - 5;
			cd.setMinutes(modMins);
			cd.setSeconds(0);
			
		PreparedStatement ps = dbConn.prepareStatement("SELECT DISTINCT jobowners.jobid FROM jobowners , slots WHERE jobowners.userid = ? AND jobowners.jobid = slots.jobid AND slots.sdate <= ? AND slots.stime <= ?");
		ps.setInt(1, this.userid);
		ps.setDate(2, new java.sql.Date(cd.getTime()));
		ps.setTime(3, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
		
		ResultSet rs = ps.executeQuery();
		ps = dbConn.prepareStatement("SELECT DISTINCT jobid FROM slots WHERE sdate = ? AND stime = ?  ");
		modMins = modMins + 5;
		cd.setMinutes(modMins);
		cd.setSeconds(0);
		ps.setDate(1, new java.sql.Date(cd.getTime()));
		ps.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
		ResultSet rs2 = ps.executeQuery();
		int curId=-1;
		if(rs2.next()){
			curId = rs2.getInt("jobid");
		}
		System.out.println("!!!!!!!!11JobData Details!!!!!!!!");
		while(rs.next()){
			int id = rs.getInt("jobid");
			if(id != curId)
			System.out.println("Job data is available for job id "+id +" in folder "+UserHandler.HOMEDIR+this.uname+"/job"+id+"/");
		}
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println(" Error in database querying try again");
		}
		handleOptions();
	}
	
	/**
	 * displays moteconnectivity of the lab which is stored in the moteconnMap vector
	 *
	 */
	public void showMotesConnectivity(){
		try{
			if(moteconnMap.size()!=0){
				for(int i=0;i<moteconnMap.size();++i){
					System.out.println((String)moteconnMap.elementAt(i));
				}
			}
			else{
				System.out.println("None");
			}
			handleOptions();
		}catch(Exception e){
			stamp("showMotesConnectivity");
			e.printStackTrace();
		}
	}
	public void handleOptions() {
		try{
			//new SetMCMap().start();
			setNumofMotes();
			displayOptions("G");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s =br.readLine();
			if(s.equals("1")) {
				createJob();
			}
			else if (s.equals("3")) {
				scheduleJob();
			}
			else if (s.equals("4")) {
				logout();
			}
			else if (s.equals("5")) {
				showMoteStatus();
			}
			else if (s.equals("6")) {
				showJobData();
			}
			else if (s.equals("7")) {
				showMotesConnectivity();
			}
		}catch(Exception e){
			stamp("handleOptions");
			e.printStackTrace();

		}
	}

}