import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
/**
 * This class will schedule moteconnectivity job every half n hour, under the user admin 
 * 
 *
 */

public class MoteConnectivity extends Thread{

	int SLEEP = 30*60*1000;
	int SLOTSIZE = 5;
	public Connection dbConn;
	String mcUsername = "admin";
	int mcUserid = 1;
	/**
	 * mote connectivity job id
	 */
	int mcJobid = 1;
	/**
	 * current Date
	 */
	java.util.Date cd;
	public void run() {
		try{
			dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in class check and set new jobs");
			e.getMessage();
			e.printStackTrace();
		}
		while (true){
			try{
			cd= new java.util.Date();
			Slot mcSlot = getNextFreeSlot();
			new User(mcUsername,mcUserid).commitSlot(mcSlot);
			checkForNewMotes();
			this.sleep(SLEEP);
			}catch(Exception e){
				System.err.println(e);
			}
		}
	}
	/**
	 * add new motes to the job if any added before last scheduled time.
	 * @throws Exception
	 */
	public void checkForNewMotes() throws Exception{
		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid FROM schedules where jobid = ?");
			query.setInt(1, mcJobid);
			ResultSet s = query.executeQuery();
			Vector prevmotes = new Vector();
			while(s.next()){
				int id = s.getInt("moteid");
				prevmotes.addElement(id);
			}
			
			query = dbConn.prepareStatement("SELECT mainfile FROM mainfiles where jobid = ?");
			query.setInt(1, mcJobid);
			s = query.executeQuery();
			String mcMainfile = "";
			while(s.next()){
				mcMainfile = s.getString("mainfile");
			}
				
			query = dbConn.prepareStatement("SELECT moteid FROM motes");
			s = query.executeQuery();
			while(s.next()){
				int id = s.getInt("moteid");
				if(!prevmotes.contains(id)){
					PreparedStatement ps = dbConn.prepareStatement("INSERT INTO schedules values (?, ?, ?)");
					ps.setInt(1, id);
					ps.setInt(2, mcJobid);
					ps.setString(3, mcMainfile);
					ps.execute();
				}
			}
			}catch(Exception e){
				throw e;
			}
	}
	/**
	 * This will return  the next free slot which is available for scheduling mote connectivity program. 
	 * @return
	 */
	public Slot getNextFreeSlot() {
		Slot sl = null;
		try{
			int modMins = cd.getMinutes();
			modMins = modMins - (modMins%SLOTSIZE);
			modMins = modMins + 10;
			cd.setMinutes(modMins);
			cd.setSeconds(0);

			PreparedStatement query = dbConn.prepareStatement("SELECT jobid FROM slots" +
			" WHERE sdate = ? AND stime = ?");

			query.setDate(1, new java.sql.Date(cd.getTime()));
			query.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));

			ResultSet s = query.executeQuery();
			if(s.next()){
				try{
				this.sleep(SLOTSIZE*60*1000);
				} catch (Exception e) {}
				cd = new java.util.Date();
				return getNextFreeSlot();
			}
			else{
				sl = new Slot(cd);
				sl.setScheduledJodID(mcJobid);
			}
		}catch(SQLException e){
			stamp("SQL Exception caught while trying to get next job details");
			stamp("check datbase connection");
			e.getMessage();
			e.printStackTrace();
			stamp("!!!!!WARNING!!!!! : if there is some job scheduled on this machine after Sleep minuntes from");
		}
		return sl;
	}
	public void stamp(String s) {
		System.out.println("MoteConnectivity "+s);
	}
}
