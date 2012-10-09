import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * This class uploads motestat program on all motes
 * 
 */
public class UploadDefaultOnAll extends Thread{
	/**
	 * This vector has all motes connected to the current Minor Node.
	 */
	private Vector motes;
	/**
	 * dbConnection
	 */
	public Connection dbConn;
	
	
	public void initialize(){
		try{
		dbConn = DBConnection.getConnection();
		}catch(Exception e){
			stamp("error getting datbase connection in classUpload Default job");
			e.getMessage();
			e.printStackTrace();
		}
	}
	/**
	 * set intially all motes condition to zero
	 *
	 */
	public  void setAllMotesConditionToZero(){
		try {
		PreparedStatement query = dbConn.prepareStatement("UPDATE motes SET condition = 0  " +
		" WHERE ipaddress = ? ");
		query.setString(1, Trigger.IPADDRESS);
		query.execute();
		}catch(SQLException e){
			stamp("error while changing condition of all motes shutting down the minor node check datbase connection and restart\n");
			MoteNet.STOP = true;
			stamp("shutting down all threads by system.exit");
			System.exit(0);
		
		}
		
	}
	public synchronized void run() {
		initialize();
		getAllMotes();
		//setAllMotesConditionToZero();
		installOnAllMotes();
	}
	/**
	 * function which install default job on all motes
	 */
	public void installDefault(){
		initialize();
		getAllMotes();
		installOnAllMotes();
	}
	/**
	 * gets all mote connected to this comp adn store in motes vector
	 *
	 */
	public void getAllMotes() {
		try{
			motes = new Vector();
			PreparedStatement query = dbConn.prepareStatement("SELECT moteid, moteport, condition FROM motes" +
			" WHERE ipaddress = ? ORDER BY moteid");
			query.setString(1,Trigger.IPADDRESS);
			ResultSet s = query.executeQuery();
			while(s.next()){
				if(s.getInt("condition")!=2)
				{
					MoteJob mj = new MoteJob();
				mj.setMoteId(s.getInt("moteid"));
				mj.setComPort(s.getString("moteport"));
				mj.setMainFile(Trigger.DEFAULTJOBFILE);
				mj.setCondition(s.getInt("condition"));
				motes.addElement(mj);
				}
			}
			System.out.println("number of motes connected are "+motes.size()+"\n and mote details  are");
			for(int i=0;i<motes.size();i++)
			System.out.println((MoteJob)motes.elementAt(i));
		}catch(SQLException e){
			stamp("getAllMotes SQL Exception occured check datbase connection ");
			e.printStackTrace();
		}
	}
	/**
	 *installs default program on all motes connected to minor node 
	 *Exceptions:
	 * if IOException occurs assumes it due to some file not found aill stop all motenet program by notifying other threads
	 * 
	 */
	public void installOnAllMotes() {
		System.out.println("now installing on all motes default motestat program ");
		for(int i =0; i<motes.size();++i){
			MoteJob mj = (MoteJob)motes.elementAt(i);
			try{
			Upload.upload(mj);//upload each motejob
			}catch(IOException e) {
				stamp("error on uploading mote(IOException) "+ mj);
				e.getMessage();
				e.printStackTrace();
				MoteNet.STOP = true;
				stamp("shutting down all threads by system.exit");
				//System.exit(0);
			}
			catch(InterruptedException e ){
				stamp("error on uploading mote(interrupted Exception) "+ mj);
				e.getMessage();
				e.printStackTrace();
				//System.exit(0);
			}
			catch(Exception e){
				stamp("error on uploading mote(general Exception) "+ mj);
				e.getMessage();
				e.printStackTrace();
				//System.exit(0);
			}
		}
	}
	
	public void stamp(String s) {
		System.out.println(" In class UploadDefaultOnAll in function "+s);
	}
}
