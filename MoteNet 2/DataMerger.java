import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * 
 * 
 *
 */
public class DataMerger extends Thread {
//	Connection dbConn;
//	int SLOTSIZE = 5;
//	public int getNextJob() {
//		int jobid =-1;
//		PreparedStatement query;
//		try{
//			java.util.Date cd = new java.util.Date();
//			int modMins = cd.getMinutes();
//			modMins = modMins - (modMins%SLOTSIZE);
//			modMins = modMins - 5;
//			cd.setMinutes(modMins);
//			cd.setSeconds(0);
//
//
//			query = dbConn.prepareStatement("SELECT jobid FROM slots" +
//			" WHERE sdate = ? AND stime = ?");
//			query.setDate(1, new java.sql.Date(cd.getTime()));
//			query.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
//
//			ResultSet s = query.executeQuery();
//			int lastid = -1;
//			while(s.next()){
//				lastid = s.getInt("jobid");
//			}
//
//
//			modMins = modMins + 5;
//			cd.setMinutes(modMins);
//			cd.setSeconds(0);
//
//
//			query = dbConn.prepareStatement("SELECT jobid FROM slots" +
//			" WHERE sdate = ? AND stime = ?");
//			query.setDate(1, new java.sql.Date(cd.getTime()));
//			query.setTime(2, new java.sql.Time(cd.getHours(),cd.getMinutes(),cd.getSeconds()));
//
//			s = query.executeQuery();
//			int curid = -1;
//			while(s.next()){
//				curid = s.getInt("jobid");
//			}
//			if(curid!=lastid && lastid != -1)
//				jobid = lastid;
//
//		}catch(Exception e){
//
//		}
//		return jobid;
//	}
//	public void stamp(String s) {
//		System.out.println("DATAMERGER: "+s);
//	}
//	public DataMerger() {
//		try{
//			dbConn =DBConnection.getConnection();
//		}catch(Exception e) {
//			stamp("could not get database connection check if your database is running or not");
//			System.exit(0);
//		}
//	}
//	public void makeTar(int id) {
//		String uname = null;
//		try{
//		PreparedStatement ps = dbConn.prepareStatement("SELECT username FROM users,jobowners WHERE  users.userid = jobowners.userid AND jobowners.jobid = ?");
//		ps.setInt(1,id);
//		ResultSet rs = ps.executeQuery();
//		rs.next();
//		rs.getString("username");
//		}catch(SQLException e){
//			stamp("error in executing query");
//		}
//		if(uname ==null) return;
//		String[] commands = new String[]{"tar","-zcvf",UserHandler.HOMEDIR+uname+"/job"+id+"/data.tgz",
//				UserHandler.HOMEDIR+uname+"/job"+id+"/data"};
//		try{
//			Process child = Runtime.getRuntime().exec(commands);
//			int ret = child.waitFor();
//		}catch(Exception e){
//			System.out.println(" Error while trying to make tar ball file to ihex.out file");
//			
//		}	
//	}
//	public void run() {
//		while(true) {
//			int id = getNextJob();
//			if(id != -1) {
//				try{
//				this.sleep(120000);
//				}catch(Exception e){
//					
//				}
//				makeTar(id);
//			}
//			try{
//			this.sleep(60000);
//			}catch(Exception e){
//				
//			}
//		}
//	}

}