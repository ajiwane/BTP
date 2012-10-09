import java.sql.*;

/**
 * In order to query database, we need to get a connection. This class stores iformation of the data base and gives connection when ever required.
 * 
 *
 */
public class DBConnection {
	private static final String JDBC_DRIVER = "org.postgresql.Driver";
	private static final String URL = "jdbc:postgresql://10.129.50.149:5432/MoteNet";
	private static final String USERNAME = "postgres";
	private static final String PASSWORD = "postgres";
	/*
	 * list of prepared statement
	 */
	  
	/**
	 * for debugging
	 */
	public static void stamp(String s) {
		System.out.println("DBConnection "+s);
	}
	/**
	 * returns connection with database mentioned
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection() throws Exception  
	   {
	      Connection dbConn = null;
	      
	      // Explicitly load the JDBC driver
	      try{Class.forName(JDBC_DRIVER);
	      }
	      catch (ClassNotFoundException e){
	    	  stamp(" DBConnection class :Error class JDBC_Driver not found");
	    	  throw e;
	    	  
	    	  }
	      // Create a connection to the database
	      try
	      {
	         dbConn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
	         
	      }
	      catch (SQLException e)
	      {
	    	 stamp("Unable to connect to database");
	    	 throw e;
	         
	      }
	      return dbConn;
	   } // main
	
	/**
	 * closes the connection with the object
	 * @param dbConn
	 */
	public static void closeConnection(Connection dbConn) {
		try{
			dbConn.close();
		}catch(Exception e){
			stamp("closeConnection");
			e.printStackTrace();
		}
	}
	
	/*
	 * s = moteid ot jobid or userid
	 */
	/**
	 * Inorder to assign and id to mote or job max number used till now is stored in the database. This is function return that number which is mentioned in the string.  
	 */
	public static int getId(String s,Connection dbConn) {
		int i;
		try{
			PreparedStatement query = dbConn.prepareStatement("SELECT "+ s+" FROM curid");
			ResultSet rs = query.executeQuery();
			rs.next();
			i = Integer.parseInt(rs.getString(s));
			return i;			
		}catch(Exception e){
			stamp("getId");
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Inorder to assign and id to mote or job max number used till now is stored in the database. This is function sets the variable mentioned in string to the given value..
	 * @param s
	 * @param dbConn
	 */
	public static void setId(String s,Connection dbConn) {
		int i;
		try{
			//transaction
			
			PreparedStatement query = dbConn.prepareStatement("SELECT "+s+" FROM curid");
			ResultSet rs = query.executeQuery();
			if(rs.next()){
				i = rs.getInt(s);
				dbConn.setAutoCommit(false);
				query = dbConn.prepareStatement("UPDATE curid SET "+s+" = ?");
				i = i+1;
				query.setInt(1, i);
				query.execute();
				dbConn.commit();
	            dbConn.setAutoCommit(true);	
			}
			else{
				System.out.println("Some error with getting id in setId function.");
			}
		}catch(Exception e){
			stamp("setId");
			e.printStackTrace();
		}
		
	}
   
}