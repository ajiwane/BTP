import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This will check for the next job scheduled after 10 mins from the current time and it will store in the vector.
 * It will do this periodicaly after the SLOT time
 *  
 * 
 *
 */
public class CheckAndSetNewjobs extends Thread{

	public final int SLOTSIZE = 5;
	/**
	 * time for sleep before checking for next job to be run
	 */
	public final int SLEEP = 5*60*1000;
	public Connection dbConn;
	int nextid = -1;
	int previd = -1;
	/**
	 * current Date
	 */
	java.util.Date cd;


	public synchronized void run() {
		try{
		dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in class check and set new jobs");
			e.getMessage();
			e.printStackTrace();
		}
		set();

	}
/**
 * get the next job id to be run and store the job in the vector periodicall for Sleep secs(default 5min)
 *
 */
	public void set() {
		try{
			while(true) {
				cd= new java.util.Date();
				nextid = getNextJobid();
				//if(nextid == -1) {}
				if(nextid == previd){}
				else {
					addNextJob(nextid);
				}
				this.sleep(SLEEP);
				//wait for 5  more mins
				previd = nextid;
			}
		}catch(InterruptedException e){
			stamp(" interrupted message while trying to sleep in function set in class CheckandSetNewJob");
			e.getMessage();
			e.printStackTrace();
		}
	}
/**
 * this will add jod details to the shared vector
 * @param id
 */
	public synchronized void addNextJob(int id) {
		Job j = new Job();
		j.setJobId(id);
		j.setSDate(new java.util.Date(cd.getTime()));
		j.setStartTime(new java.sql.Time(cd.getTime()));
		Trigger.jobsToBeRun.addElement(j);
		System.out.println("next job to be run is job: "+ id);
	}
/**
 * returns id of the job scheduled 10mins from now or -1 if no job is scheduled
 */
	
	public int getNextJobid() {
		int id =-1;
		try{
			int modMins = cd.getMinutes();
			modMins = modMins - (modMins%SLOTSIZE);
			modMins = modMins + 5;
			cd.setMinutes(modMins);
			cd.setSeconds(0);

			PreparedStatement query = dbConn.prepareStatement("SELECT jobid FROM slots" +
			" WHERE sdate = ? AND stime = ?");
					
			query.setDate(1, new java.sql.Date(cd.getTime()));
			query.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
			
			ResultSet s = query.executeQuery();
			while(s.next()){
				id = s.getInt("jobid");
			}
		}catch(SQLException e){
			stamp("SQL Exception caught while trying to get next job details");
			stamp("check datbase connection");
			e.getMessage();
			e.printStackTrace();
			stamp("!!!!!WARNING!!!!! : if there is some job scheduled on this machine after Sleep minuntes from");
		}
		return id;
	}

	public void stamp(String s) {
		System.out.println("CheckAndSetNewjobs "+s);
	}
}