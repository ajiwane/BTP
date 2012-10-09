/**
 * This class stores details of the job and mote on which it is to be installed
 * 
 *
 */
public class MoteJob {

	private int jobId;
	private int moteId;
	private String mainFile;
	private String ipAddress;
	private String comPort;
	private String ihexFile;
	private String username;
	private String password;
	private int condition;
	private String SFPort;
	private String InstallComments;
	public String getInstallComments() {
		return InstallComments;
	}
	public static MoteJob getDefaultJob(){
		MoteJob mj = new MoteJob();
		mj.setMoteId(-1);
		mj.setMainFile(Trigger.DEFAULTJOBFILE);
		mj.setIpAddress(Trigger.IPADDRESS);
		return mj;
	}
	
	public void setInstallComments(String installComments) {
		InstallComments = installComments;
	}

	public String getIhexFile() {
		return ihexFile;
	}

	public void setIhexFile(String ihexFile) {
		this.ihexFile = ihexFile;
	}

	public String getSFPort() {
		return SFPort;
	}

	public void setSFPort() {
		int i = 9001+this.moteId;
		SFPort = ""+i;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIhexName() {
		return this.getMainFileName()+".ihex" ;
	}
	
	public String getMainFolder(){
		return mainFile.substring(0, mainFile.lastIndexOf("/")+1);
	}
	
	
	public String getMainFileName(){
		if(mainFile.lastIndexOf(".exe") != -1)
			return mainFile.substring(mainFile.lastIndexOf("/")+1,mainFile.lastIndexOf("."));	
		else 
			return mainFile.substring(mainFile.lastIndexOf("/")+1);
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getMainFile() {
		return mainFile;
	}
	public void setMainFile(String mainFile) {
		this.mainFile = mainFile;
	}
	public int getMoteId() {
		return moteId;
	}
	public void setMoteId(int moteId) {
		this.moteId = moteId;
	}
	public String getComPort() {
		return comPort;
	}
	public void setComPort(String comPort) {
		this.comPort = comPort;
	}
	public String toString(){
		return "mote id is "+ this.moteId+"\t mote port is "+this.comPort+"\n";
	}
	public MoteJob(int jobId, int moteId, String mainFile, String ipAddress, String comPort) {
		this.jobId = jobId;
		this.moteId = moteId;
		this.mainFile = mainFile;
		this.ipAddress = ipAddress;
		this.comPort = comPort;
		this.setSFPort();
	}
	public MoteJob(){}
	
	public static void main(String args[]){
		MoteJob mj = new MoteJob(1,2,"home/kite","21212","effsds");
		System.out.println(mj.getMainFolder());
	}
}