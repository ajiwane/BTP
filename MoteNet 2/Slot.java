import java.sql.*;

public class Slot {
	
	private java.util.Date sDate;
	private Time startTime;
	private java.util.Date eDate;
	int scheduledJodID;
	//in minutes
	static int SLOTSIZEMIN =5;
	static int SLOTSIZEMILLISEC =5*60*1000;
	static int YEARBUG =1900;
	static int MONTHBUG =1;
	static int AFTER30MINS = 1800000; 
	
	Slot(java.util.Date d) {
		this.sDate = new java.util.Date(d.getTime());
		this.startTime = new Time(d.getHours(),d.getMinutes(),d.getSeconds());
		this.eDate = new java.util.Date(d.getTime());
		this.eDate.setMinutes(d.getMinutes()+SLOTSIZEMIN);
		
	}
	
	public void displaySlot(){
		System.out.println();
		System.out.println(this.sDate.toLocaleString()+"----"+this.eDate.toLocaleString());
	}

	public int getScheduledJodID() {
		return scheduledJodID;
	}

	public void setScheduledJodID(int scheduledJodID) {
		this.scheduledJodID = scheduledJodID;
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
}