import java.sql.Time;
import java.util.*;
/**
 * This is a structure to store details of the job which include jobid,job name,mainfiles, classfiles, moteid...etc
 * 
 *
 */
public class Job {
	private int jobId;
	private String description;
	private String name;
	private Vector mainFiles;
	private Vector classFiles;
	private Vector mainUploadMoteIds;
	private java.util.Date sDate;
	private Time startTime;
	private java.util.Date eDate;
	private Time endTime;
	
	public String toString() {
		return "job id is "+jobId+" job name "+name+" job description  "+ description;
	}
	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public java.util.Date getEDate() {
		return eDate;
	}

	public void setEDate(java.util.Date date) {
		eDate = date;
	}

	public java.util.Date getSDate() {
		return sDate;
	}

	public void setSDate(java.util.Date date) {
		sDate = date;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Job(){
		this.mainFiles = new Vector();
		this.classFiles = new Vector();
	}

	public Vector getClassFiles() {
		return classFiles;
	}

	public void setClassFiles(Vector classFiles) {
		this.classFiles = classFiles;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Vector getMainFiles() {
		return mainFiles;
	}

	public void setMainFiles(Vector mainFiles) {
		this.mainFiles = mainFiles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public Vector getMainUploadMoteIds() {
		return mainUploadMoteIds;
	}

	public void setMainUploadMoteIds(Vector mainUploadMoteIds) {
		this.mainUploadMoteIds = mainUploadMoteIds;
	}
}