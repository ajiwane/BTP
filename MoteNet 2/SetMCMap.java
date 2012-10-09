import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.Vector;

/**
 * this class reads data collected from running moteconnectivity job and parses the messages to obtain mote connectivity map of the motenet lab
 * 
 */
public class SetMCMap extends Thread{
	java.util.Date cd;
	public Connection dbConn;
	int mcJobid = 1;
	int SLOTSIZE = 5;
	public void run(){
		try{
			dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in class check and set new jobs");
			e.getMessage();
			e.printStackTrace();
		}

		while(true){
			cd= new java.util.Date();
			while(!isMCinNextSlot()){
				try{
					Thread.sleep(SLOTSIZE*60*1000);
				}catch(Exception e){}
				cd= new java.util.Date();
			}
			setMap();
		}
	}
/**
 * 3 minutes after completion of the moteconnectivity job, this function reads data files from collected data and extracts the mote connectivity map of the lab 
 *
 */
	public void setMap(){
		try{

			java.util.Date d= new java.util.Date();
			d.setSeconds(0);
			//wait for the scheduled time
			while(cd.getYear()!= d.getYear()
					|| cd.getMonth() != d.getMonth()
					|| cd.getDate()!=d.getDate() 
					|| cd.getHours()!=d.getHours()
					|| cd.getMinutes()!= d.getMinutes()
			){
				this.sleep(60000);
				d= new java.util.Date();
				d.setSeconds(0);
			}
			this.sleep((SLOTSIZE+3)*60*1000);

			Vector v = new Vector();
			String foldername = UserHandler.HOMEDIR+"admin/job1/data/"+(new java.sql.Date(cd.getTime())).toString()+
			"_"+new Time(cd.getTime()).toString();//folder according to time
			File folder = new File(foldername);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++)
			{
				//get file status
				String fName=listOfFiles[i].getName();
				if(fName.contains("data_mote")){
					int in = fName.indexOf("mote_");
					String nodeid = fName.substring(in+5);
					BufferedReader br=null;
					br = new  BufferedReader(new FileReader(new File(foldername+"/"+fName)));
					String line = "";
					line=br.readLine();
					while(line!=null){

						if(line.contains("nodeid")){
							line = line.substring(line.indexOf("0x")+2);
							line = line.substring(0, line.indexOf("]"));
							int decid = Integer.parseInt(line,16);
							String map = decid+" is connected to "+nodeid; 
							if(!v.contains(map)){
								v.addElement(map);
							}
						}
						line=br.readLine();
					}
				}
			}
			User.moteconnMap = new Vector();
			for(int i =0;i<v.size();++i){
				User.moteconnMap.addElement(v.elementAt(i));
			}
		}catch(Exception e){
			System.err.println(e);
		}
	}
/**
 * this function checks if moteconnectivity is scheduled in the next slot
 * @return true if it is scheduled else returns false
 */
	public boolean isMCinNextSlot(){
		boolean ret = false;

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
				int id = s.getInt("jobid");
				if(id==mcJobid){
					ret = true;
				}
			}

		}catch(Exception e){
			System.err.println(e);
		}
		return ret;
	}

	public void stamp(String s) {
		System.out.println("SetMCMap "+s);
	}
}