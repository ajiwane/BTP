import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.*;
import java.util.Date;

import sun.net.ftp.FtpClient;
import net.tinyos.message.*;
import net.tinyos.packet.Serial;
/**
 * this will run the scheduled jobs
 * @author naresh,ashwin
 *
 */
public class RunScheduledJob extends Thread{
/**
 * this has  job details to be run next
 */
	Vector v ;
	/**
	 * vector of dataloggers which will be closed later
	 */
	private Vector DataL = new Vector();
	/**
	 * Vector of serial forwarders which will be closed later
	 */
	private Vector SerialF = new Vector();
	/**
	 * vector of motes on which current job is to be run 
	 */
	private Vector moteJ;
	/**
	 * vector of motes on which previous job is to be run
	 */
	private Vector prevmoteJ;
	/**
	 * next job id
	 */
	private int nextJobid;
	/**
	 * current job
	 */
	private Job nextJob;
	/**
	 * previous username
	 */
	private String prevUName;
	/**
	 * previous job id
	 */
	private int prevJobid;
	/**
	 * dbConnection 
	 */
	public Connection dbConn;
	/**
	 * user Id of the job 
	 */
	private int userId;
	/**
	 * username of the job owner
	 */
	private String uname ;
	/**
	 * -1 implies next job to be run is motestat job
	 */
	private final int DEFAULTID = -1;
	/**
	 * contains info all bad motes for current running job
	 */
	Vector moteE ;
	/**
	 * error state after uploading job on motes 
	 */
	private boolean UPLOADSTATE;
	/**
	 * current Job Folder
	 */
	private String currentJF;
	/**
	 * directory to which data is to copied not full path only directory name
	 */
	private String dataDirectory;
	public synchronized void run() {
		try{
			dbConn = DBConnection.getConnection();
		}catch(Exception e) {
			stamp("error getting datbase connection in class Runcheckscheduled job");
			e.getMessage();
			e.printStackTrace();
			System.exit(0);
		}
		try{
			//uploading jobs 
			while(true){
				v = new Vector();
				for(int i=0;i<Trigger.jobsToBeRun.size();++i)
					v.addElement(Trigger.jobsToBeRun.elementAt(i));
				//checks after every one minute for next job		
				while(v.size()==0 || v==null){
					//checking for the next job
					this.sleep(60000);
					for(int i=0;i<Trigger.jobsToBeRun.size();++i)
						v.addElement(Trigger.jobsToBeRun.elementAt(i));				
				}
				nextJob = (Job)v.elementAt(0);
				nextJobid = nextJob.getJobId();
				if(nextJobid != DEFAULTID){
					setUname();
					setmoteJVector();
				}
				else{
					uname = "Admin";
				}
			
				stamp("nextJobId in runscheduledJob is "+nextJobid +" belogns to user "+uname);
				if(nextJobid != DEFAULTID)currentJF = Trigger.HOMEDIR+uname+"/job"+nextJobid+"/";
				else currentJF = Trigger.DEFAULTJOBFOLDER;
					
				java.util.Date sDate= nextJob.getSDate();
				Time sTime = nextJob.getStartTime();
				java.util.Date d= new java.util.Date();
				d.setSeconds(0);
				//wait for the scheduled time
				while(sDate.getYear()!= d.getYear()
						|| sDate.getMonth() != d.getMonth()
						|| sDate.getDate()!=d.getDate() 
						|| sTime.getHours()!=d.getHours()
						|| sTime.getMinutes()!= d.getMinutes()
				) {

					this.sleep(60000);
					d= new java.util.Date();
					d.setSeconds(0);
				}

				if(prevmoteJ != null) System.out.println("previous job id is "+ prevJobid);
				if(prevmoteJ != null && prevJobid != DEFAULTID) {
					System.out.println(" stopping dataloggers of previous job");
					stopDl();
					Thread.sleep(2000);
				}
				dataDirectory = (new java.sql.Date(sDate.getTime())).toString()+"_"+sTime.toString();
				System.out.println("dat directory is  "+dataDirectory);
				System.out.flush();
				UploadAllExecutables();
				if(!UPLOADSTATE) {
					stamp(" job counot be uploaded on motes probably datbase is not available for checking condiotion");
					MoteNet.CURRENTJOBID = DEFAULTID;
					UploadDefaultOnAll ud = new UploadDefaultOnAll();
					ud.installDefault();
					setPrevJob(DEFAULTID);
					removeJob();
					prevJobid = DEFAULTID;
					prevUName = "Admin";
					continue;
				}
				
				MoteNet.CURRENTJOBID = nextJobid;
				if(MoteNet.CURRENTJOBID!= DEFAULTID) {
					logError();
				}
				uploadDefault();
				setPrevJob(0);
				removeJob();
				prevJobid = nextJobid;
				prevUName = this.uname;
			}
		}catch(Exception e){
			stamp("run");
			e.printStackTrace();
		}

	}
	/**
	 * logs the errors occured during installation of programs on mote
	 */
	public void logError() {
		try{
			
			File f = new File(currentJF+"ErrorLog_"+Trigger.IPADDRESS);
			System.out.println(currentJF+"ErrorLog");
			boolean b =f.createNewFile();
			BufferedWriter br = new BufferedWriter(new FileWriter(f));
			
			if(!UPLOADSTATE) {
				br.write("job is not  installed and run on this minor node");
			}
			System.out.println("logging Error  size of moteE is "+moteE.size());
			if(moteE != null){
				for(int i=0;i<moteE.size();i++){
					MoteJob mj =(MoteJob)moteE.elementAt(i); 
					if((mj).getInstallComments() != null || mj.getInstallComments().length() != 0){
						stamp("writing comments about mote "+mj.getMoteId());
						br.write(mj.toString());
						br.write(mj.getInstallComments()+"\n");
					}
				}
			}
			br.close();
		}catch(IOException e){
			stamp("error while logging the deatils of errors while uploading data" );
			e.printStackTrace();
		}
	}
	/**
	 *this will change the job folder to defaultjob folder on those motes on which job ran previously 
	 *
	 */
	public void setDefOnCMotes() {
		if(moteJ== null) return;
		for(int i=0;i<moteJ.size();i++) {
			MoteJob mj = (MoteJob)moteJ.elementAt(i);
			}
		
	}
	public void setUname() {
		try{
		PreparedStatement query = dbConn.prepareStatement("SELECT username" +
		" FROM users, jobowners WHERE jobowners.jobid = ? AND jobowners.userid = users.userid");
		query.setInt(1,this.nextJobid);
		ResultSet s = query.executeQuery();
		while(s.next()){
			uname = s.getString("username");
		}
		}catch(Exception e){
			stamp("setUname");
			e.printStackTrace();
		}
	}
	
	public void setmoteJVector() {
		try{
			
			moteJ = new Vector();
		PreparedStatement query = dbConn.prepareStatement("SELECT jobid, schedules.moteid, mainfile, moteport, condition" +
		" FROM schedules, motes WHERE schedules.moteid = motes.moteid AND schedules.jobid = ? AND motes.ipaddress = ?");
		query.setInt(1,this.nextJobid);
		query.setString(2,Trigger.IPADDRESS );
		ResultSet s = query.executeQuery();
		while(s.next()){
			MoteJob j;
			
			j = new MoteJob(s.getInt("jobid"),s.getInt("moteid"),s.getString("mainfile"),Trigger.IPADDRESS,s.getString("moteport"));
			j.setCondition(s.getInt("condition"));
			String mainfilename = j.getMainFileName();
			
			String mainfile = Trigger.HOMEDIR+uname+"/job"+nextJobid+"/"+mainfilename+".exe";
			j.setMainFile(mainfile);
	
			moteJ.add(j);	
		
		}
		}catch(Exception e){
			stamp("setmoteJVector");
			e.printStackTrace();
		}
	}
	
	public static String getClassName(String s){
		String z="";
		z=s.substring(s.lastIndexOf("/")+1,s.length());
			
		z=z.substring(0,z.lastIndexOf("."));
		return z;
	}
	public Vector getClassFiles(MoteJob mj) {
		Vector v = new Vector();
		if(mj.getJobId() == DEFAULTID) {
			try{
				stamp("installing default on mote "+mj.getMoteId()+"  "+mj.getComPort());
				File f = new File(mj.getMainFolder());
				URL[] u = new URL[]{f.toURL()};
				ClassLoader cl = new URLClassLoader(u);
				Class c = Class.forName(Trigger.DEFAULTJOBCLASSNAME,false,cl);
				v.add((Message)c.newInstance());
			}catch(Exception e) {
				stamp("error in loading the class");
				return v;
			}
			return v;
		}
		else{
			try{
				PreparedStatement query = dbConn.prepareStatement("SELECT classfile From classfiles Where jobid = ?");
				query.setInt(1, mj.getJobId());
				ResultSet rs = query.executeQuery();
				File f = new File(mj.getMainFolder());
				URL[] u = new URL[]{f.toURL()};
				ClassLoader cl = new URLClassLoader(u);
				while(rs.next()){
					String s = getClassName(rs.getString("classfile"));
					System.out.println("searching in  "+mj.getMainFolder()+" for class  "+ s );

					Class c = Class.forName(s,false,cl);
					v.add((Message)c.newInstance());
				}
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("error in database");
			}
			return v;
		}
	}
	/**
	 * this function uploads all executables on all motes accordingly
	 */
	public void UploadAllExecutables() {
		
		//vector of motes whose condiotion is bad and job wont b  scheduled
		UPLOADSTATE = false;
		moteE = new Vector();
		for(int i=0;i<moteJ.size();++i){
			MoteJob mj = (MoteJob)moteJ.elementAt(i);
			//this has current job class files
			Vector classfiles = getClassFiles(mj);
			PreparedStatement query = null;
			try{
				query = dbConn.prepareStatement("SELECT condition, moteport FROM motes WHERE moteid = ?");
			}catch(SQLException e){
				stamp("SQL exception in getting condition of the mote");
				UPLOADSTATE =false;
				e.getMessage();
				e.printStackTrace();
				return;
			}
			ResultSet s = null;
			try{
				query.setInt(1,mj.getMoteId());
				s = query.executeQuery();
			}catch(SQLException e){
				stamp("SQL exception(in executing query) in getting condition of the mote");
				e.getMessage();
				e.printStackTrace();
				UPLOADSTATE = false;
				return;
			}catch(NullPointerException e){
				stamp("query is not initalized");
				e.getMessage();
				e.printStackTrace();
				UPLOADSTATE = false;
				return;
				
			}
			try{
				while(s.next()) {

					int condition =  s.getInt("condition");
					Mote mote = new Mote(mj.getMoteId(),s.getString("moteport"),mj.getMainFolder()+mj.getMoteId()+"_status");
					if(condition == 0 ){
						stamp("condiotion of mote "+ mj.toString()+" is bad");
						mj.setInstallComments("condiotion of mote "+ mj.toString()+" is bad in database "+"\n");
						moteE.add(mj);
					}
					else if (condition == 2){
						stamp("mote "+ mj.toString()+" is removed");
						mj.setInstallComments("mote "+ mj.toString()+" is removed  it is not connected"+"\n");
						moteE.add(mj);
					}
					else{
						System.out.println("uploading...");
						//need to take care of error
						MoteData md = new MoteData("sf@localhost:"+mj.getSFPort(),classfiles,mj.getMoteId(),mj.getMainFolder()+Trigger.IPADDRESS+"_data_mote_"+mj.getMoteId());
						try{
							Upload.upload(mj);
						}catch(IOException e) {
							stamp("error on uploading mote(IOException) "+ mj);
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("IO error while installing on the mote"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							
							moteE.add(mj);
							continue;
						}
						catch(InterruptedException e ){
							stamp("error on uploading mote(interrupted Exception) "+ mj);
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("Interrupted error  while installing on the mote"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							moteE.add(mj);
							continue;
						}
						catch(Exception e){
							stamp("error on uploading mote(general Exception) "+ mj);
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("Interrupted error  while installing on the mote"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							moteE.add(mj);
							continue;
						}
						if(nextJobid != DEFAULTID ) {
						MySerialForwarder sf = new MySerialForwarder(mj.getSFPort(),mj.getComPort());
						try {
							sf.start();
						}catch(InterruptedException e){
							stamp("InterruptedExceptio while starting serial forwader on "+ mj.getComPort()+" for job "+mj.getJobId());
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("Could not start serial forwarder due to InterruptedException"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							moteE.add(mj);
							continue;
						}catch(IOException e){
							stamp("IOException while starting serial forwader on "+ mj.getComPort()+" for job "+mj.getJobId());
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("Could not start serial forwarder due to IOException"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							moteE.add(mj);
							continue;
						}catch(Exception e){
							stamp("Exception while starting serial forwader on "+ mj.getComPort()+" for job "+mj.getJobId());
							e.getMessage();
							e.printStackTrace();
							mj.setInstallComments("Could not start serial forwarder due to Exception"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
							moteE.add(mj);
							continue;
						}
						DataLogger dl = null;
						if(sf.check()) {
							SerialF.add(sf);
							try{
								dl = new DataLogger(md,mote);
							}catch(IOException e){
								stamp("Error while opening packet file for the datalogger of  "+ mj.getComPort());
								mj.setInstallComments("probably data although collected will not be written to the file"+"\n"+"Error is "+"\n"+e.getMessage()+"\n");
								e.getMessage();
								e.printStackTrace();
								moteE.add(mj);
								continue;
							}catch(Exception e){
								stamp("Error while creating data logger" );
								mj.setInstallComments("Error : "+ e.getMessage()+ "while starting data logger");
								e.getMessage();
								e.printStackTrace();
								moteE.add(mj);
								continue;

							}
							//dl.start();
							if(!dl.getStatus()) {
								stamp("Error while opening the datalogger of  "+ mj.getComPort());
								mj.setInstallComments("data logger is not opened properly data may not be written correctly");
								moteE.add(mj);
								
							}
							DataL.add(dl);
							if(mj.getInstallComments()==null || mj.getInstallComments().length() ==0)
							{
							mj.setInstallComments("Job is sucessfully ran on "+ Trigger.IPADDRESS+"  "+mj.getComPort());
							System.out.println("mote add to moteE Vector");
									}
							System.out.println("installation comments "+mj.getInstallComments());
							moteE.add(mj);
							stamp("datlogger on mote "+ mj.getComPort()+" started :)");
						}
						}
					}
				}
			}catch(SQLException e){
				stamp("sql exceptio while getting the information of the mjotes" );
				e.printStackTrace();
				e.getMessage();
				UPLOADSTATE = false;
				return;
			}catch(NullPointerException e){
				stamp("result Set is not initialized ");
				e.printStackTrace();
				e.getMessage();
				UPLOADSTATE = false;
				return;
			}catch(Exception e){
				stamp("Error while executing query to get the conditions of motes ");
				e.printStackTrace();
				e.getMessage();
				UPLOADSTATE = false;
				return;
			}
		}
		UPLOADSTATE = true;
		//ErrorHandler.handleJobMoteError(moteE, this.userId, this.nextJobid);
	}
	/**
	 * This program uploads default program on those motes which are uncommon to the current running job 
	 */
	public void uploadDefault() {
		if(prevmoteJ==null) return;
		
			Vector tmpmoteJ = new Vector();
			for(int j=0;j<prevmoteJ.size();++j){
				tmpmoteJ.add(prevmoteJ.elementAt(j));
			}
			
			for(int i=0;i<moteJ.size();++i){
				MoteJob mj = (MoteJob)moteJ.elementAt(i);
				int moteid = mj.getMoteId();
				for(int j=0;j<tmpmoteJ.size();++j){
					MoteJob prevmj = (MoteJob)tmpmoteJ.elementAt(j);
					if(prevmj.getMoteId() == moteid) {
						tmpmoteJ.removeElementAt(j);
						break;
					}
				}
							
			}
			
			
			for(int i=0;i<tmpmoteJ.size();++i){
				MoteJob mj = (MoteJob)tmpmoteJ.elementAt(i);
				mj.setMainFile(Trigger.DEFAULTJOBFILE);
				try{
				Upload.upload(mj); //check how it uploads
				}catch(IOException e) {
					stamp("error while  uploading default program on mote(IOException) "+ mj);
					e.getMessage();
					e.printStackTrace();
				
				}
				catch(InterruptedException e ){
					stamp("error while uploadingdefault program on  mote(interrupted Exception) "+ mj);
					e.getMessage();
					e.printStackTrace();
				}
				catch(Exception e){
					stamp("error while uploading default program on  mote(general Exception) "+ mj);
					e.getMessage();
					e.printStackTrace();
				
				}
			}
		
	}
	/**
	 * sets previous Job to current job
	 */
	public void setPrevJob(int id) {
		prevmoteJ = new Vector();
		if(id == DEFAULTID){
			for(int i=0;i<moteJ.size();i++){
				MoteJob j = (MoteJob)moteJ.elementAt(i);
				j.setJobId(-1);
				j.setMainFile(Trigger.DEFAULTJOBFILE);
				prevmoteJ.add(j);
				
			}
		}
		else{
			
			for(int i=0;i<moteJ.size();++i){
				MoteJob j =	(MoteJob)moteJ.elementAt(i);
				j.setMainFile(Trigger.DEFAULTJOBFILE);
				j.setJobId(DEFAULTID);
				prevmoteJ.add(j);
			}
		}			

	}
	/**
	 * this function stops currently running Dataloggers 
	 * @throws Exception
	 */
	public void stopDl() throws Exception {
		for(int i=0;i<DataL.size();i++) {
			DataLogger dl =(DataLogger)DataL.elementAt(i);
			dl.stop();
		}
		for(int i=0;i<SerialF.size();i++) {
			MySerialForwarder msf = (MySerialForwarder)SerialF.elementAt(i);
			msf.stop();
		}
		//
		Vector Paths = new Vector();
		for(int i=0;i<DataL.size();i++){
			DataLogger dl = (DataLogger)DataL.elementAt(i);
			MoteData md = dl.getMd();
			Paths.add(md.getDataFile());			
		}
		Paths.add(Trigger.HOMEDIR+this.prevUName+"/job"+this.prevJobid+"/ErrorLog_"+Trigger.IPADDRESS);
		DataCombiner dc = new DataCombiner(Paths,getMN(),Trigger.MNHOMEFOLDER+this.prevUName+"/job"+this.prevJobid+"/data"+"/"+dataDirectory,this.prevJobid);
		dc.start();
		//
	
	
		DataL = new Vector();
		SerialF = new Vector();
	
	}
	public MinorNode getMN() {
		MinorNode mn = new MinorNode(Trigger.MNIPADRESS,Trigger.MNUNAME,Trigger.MNPASSWORD,Trigger.HOMEDIR);
		return mn;
	}
	/**
	 * remove the job from the vector 
	 * @throws Exception
	 */
	public synchronized void removeJob() {
		
			Trigger.jobsToBeRun.removeElementAt(0);
	}
	public static void main(String[] args) {
		System.out.println(getClassName("/home/ashwin/motenet/ashwin/job8/BlinkToRadioMsg.class"));
		Date ud = new java.util.Date();
		System.out.println(ud);
		java.sql.Date sd = new java.sql.Date(ud.getTime());
		java.sql.Time sd2 = new java.sql.Time(ud.getTime());
		String s = sd.toString()+"_"+sd2.toString();
		System.out.println(s);
		System.out.println(sd2);
	}
	public void stamp(String s) {
		System.out.println("RunScheduledJob "+s);
	}

}