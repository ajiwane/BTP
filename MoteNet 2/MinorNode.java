/**
 * This structure is used to store information of a minor node
 * 
 *
 */
public class MinorNode {
	private String ipAddress;
	private String username;
	private String password;
	private String homedir;
	public MinorNode(){
		
	}
	/**
	 * 
	 * @param ipAddress : ipaddress of the machine
	 * @param username : usernameof the machine
	 * @param password : password of the machine
	 * @param homedir : where is the motenet home directory on that machine
	 */
	public MinorNode(String ipAddress, String username, String password, String homedir) {
		super();
		this.ipAddress = ipAddress;
		this.username = username;
		this.password = password;
		this.homedir = homedir;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getHomedir() {
		return homedir;
	}
	public void setHomedir(String homedir) {
		this.homedir = homedir;
	}
	
}
