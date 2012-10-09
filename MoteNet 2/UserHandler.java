import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
/**
 * This class starts interface for the user, providing login interface
 * It provides login/register options and implements them
 * 
 */
public class UserHandler extends Thread{
	/**
	 * path of the motenet directory on central server
	 */
	public static String HOMEDIR;
	/**
	 * ipaddress of the central server
	 */
	public static String IPADDRESS;
	/**
	 * username of the central server machine
	 */
	public static String MYUNAME;
	/**
	 * password of the central server machine
	 */
	public static String MYPASSWORD;
	/**
	 * path of the executable of the motestat-default job in central server
	 */
	public static String DEFAULTJOBFILE ;
	/**
	 * path of the motestat-default job folder in central server
	 */
	public static String DEFAULTJOBFOLDER;
	/**
	 * motestat-default job class filename
	 */
	public static String DEFAULTJOBCLASSNAME;
	/**
	 * slot size in minutes
	 */
	public static int SLOTSIZE = 5;
	public void clear(){
		try{
			Thread.sleep(500);
			String command = "clear";
			Process child = Runtime.getRuntime().exec(command);
		}catch(Exception e){
			stamp("clear");
			e.printStackTrace();
		}
	}

	public Connection dbConn;
	public void initialize() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("minorNodeDetails"));
			HOMEDIR=br.readLine();
			IPADDRESS = br.readLine();
			MYUNAME = br.readLine();
			MYPASSWORD= br.readLine();
			DEFAULTJOBFILE =  UserHandler.HOMEDIR+"motestat/main.exe";
			DEFAULTJOBFOLDER = HOMEDIR+"motestat/";
			DEFAULTJOBCLASSNAME = "StatusMessage";
		}catch(IOException e){
			System.out.println(" Error in reading file minornode details in class Trigger");
			MoteNet.STOP = true;
			stamp("shutting down all threads by system.exit");
			System.exit(0);
		}

	}
	/**
	 * this function starts the interface providing login/register options
	 */
	UserHandler () {
		try{
			initialize();
			new MoteConnectivity().start();
			new SetMCMap().start();
			while(true){
			boolean loop = true;
			String uname = "";
			String passwd = "";
			while(loop){
				System.out.println("Enter 1 to login and 2 to register.");

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String opt =br.readLine();
				if(opt.equals("1")){
					System.out.print("Enter your userid: ");
					br = new BufferedReader(new InputStreamReader(System.in));
					uname =br.readLine();

					System.out.print("Enter password: ");
					passwd =br.readLine();
					checkLogin(uname,passwd);
					loop = false;
				}
				else if(opt.equals("2")){
					register();
					loop = false;
				}
			}
		}

		}catch(Exception e){
			stamp("Login");
			e.printStackTrace();
		}

	}
	/**
	 * this function registers the new user to the lab updating user's info into the database and creating user's directory on all minornodes
	 *
	 */
	public void register(){
		try{
			boolean loop = true;
			String uname = "";
			String passwd = "";
			while(loop){
			System.out.print("Enter new userid: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			uname =br.readLine();

			System.out.print("Enter new password: ");
			passwd =br.readLine();
			
			if(!checkUser(uname)){
				DBConnection.setId("userid", dbConn);

				int uid= DBConnection.getId("userid", dbConn);
				while (uid== -1) {
					uid= DBConnection.getId("userid", dbConn);
					stamp("checkuser while loop");
				}
				
				dbConn = DBConnection.getConnection();
				PreparedStatement query = dbConn.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)");
				query.setInt(1, uid);
				query.setString(2,uname);
				query.setString(3,passwd);
				query.setString(4,uname);
				
				query.execute();
				String jobdir = HOMEDIR+uname;
				String[] commands = new String[]{"mkdir",jobdir};
				try{
					Process child = Runtime.getRuntime().exec(commands);
					Thread.currentThread().sleep(1000);
				}catch(Exception e){
					stamp("dir");
					e.printStackTrace();
				}
				
				Vector mns = getMinorNodes();
				try{
				for(int i=0;i<mns.size();++i) {
					MinorNode m = (MinorNode)mns.get(i);

					CommandExecuter ce = new CommandExecuter(-1,m);

					Vector v = new Vector();
					v.add("-r");
					v.add(MYUNAME+"@"+IPADDRESS+":"+HOMEDIR+uname);
					v.add(m.getHomedir()+uname+"\n");
					v.add(MYPASSWORD+"\n");
					ce.executeCommand("scp", v);

				}
				}catch(Exception e){
					stamp("error while copying files "+ e);
				}
				loop = false;
			}
			}
		}catch(Exception e){
			stamp("Error while registering new user");
		}
	}
	/**
	 * returns all the minornodes of the motenet lab
	 * @return
	 */
	public Vector getMinorNodes() {
		Vector moteJ = new Vector();
		try{

			PreparedStatement query = dbConn.prepareStatement("SELECT distinct ipaddress, username, password, homedir" +
			" FROM minornodes");
			ResultSet s = query.executeQuery();
			while(s.next()){
				MinorNode mn;

				mn = new MinorNode();
				mn.setIpAddress(s.getString("ipaddress"));
				mn.setUsername(s.getString("username"));
				mn.setPassword(s.getString("password"));
				mn.setHomedir(s.getString("homedir"));
				moteJ.add(mn);	

			}
		}catch(Exception e){
			stamp("setmoteJVector");
			e.printStackTrace();
		}
		return moteJ;
	}
	public void stamp(String s) {
		System.out.println("Login "+s);
	}

	/**
	 * checks if the username already exits
	 * @param uname
	 * @return true if exists else returns flase
	 */	
	public boolean checkUser(String uname){
		boolean ret = false;
		try{
			dbConn = DBConnection.getConnection();
			PreparedStatement query = dbConn.prepareStatement("SELECT passwd, userid FROM users WHERE username = '"+uname+"'");
			ResultSet rs = query.executeQuery();
			if(rs.next()) {
				ret = true;
			}
			else{
				ret = false;
			}
		}catch(Exception e){
			stamp("checkUser");
			e.printStackTrace();
		}
		return ret;
	}
	/**
	 * this authenticates the user in the motenet lab
	 */
	public void checkLogin(String uname, String pwd) {
		try{
			dbConn = DBConnection.getConnection();
			PreparedStatement query = dbConn.prepareStatement("SELECT passwd, userid FROM users WHERE username = '"+uname+"'");
			ResultSet rs = query.executeQuery();
			if(rs.next()) {
				String passwdfromdb = rs.getString("passwd");
				if(pwd.equals(passwdfromdb)){
					int userid = rs.getInt("userid");
					//clear();
					System.out.println("Welcome to the mote lab. "+ uname+ "  "+userid);
					//continue .user logins go to general options
					User u = new User(uname,userid);
					u.handleOptions();
				}
				else {
					System.out.println("Wrong Password. Try Again");
					new UserHandler().start();
					this.destroy();

				}

			}
			else {
				System.out.println("Userid does not exist. Bye");
				new UserHandler().start();
				this.destroy();

			}
			dbConn.close();

		}catch(Exception e){
			stamp("checkLogin");
			e.printStackTrace();
		}
	}
	public void run() {

	}

}