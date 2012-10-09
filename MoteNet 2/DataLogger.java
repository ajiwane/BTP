/*									tab:4
 * "Copyright (c) 2000-2005 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and
 * its documentation for any purpose, without fee, and without written
 * agreement is hereby granted, provided that the above copyright
 * notice, the following two paragraphs and the author appear in all
 * copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
 * PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2002-2005 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
/* Authors:	Phil Levis <pal@cs.berkeley.edu>
 * Date:        December 1 2005
 * Desc:        Generic Message reader
 *               
 */

/**
 * @author Phil Levis <pal@cs.berkeley.edu>
 */




import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

import sun.net.ftp.FtpClient;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;
import net.tinyos.sf.*;

/**
 * DataLogger is a class which collects data from motes after the job is installed on motes   
 * 
 *
 */
public class DataLogger implements net.tinyos.message.MessageListener {
/**
 * This is vector of Serial forwaders
 */
  private static Vector SF = new Vector();
  /**
   * This is vector of data Loggers
   */
  private static Vector DL = new Vector();
  /**
   * mote IF
   */
  private MoteIF moteIF;
  /**
   * mote Data
   */
  private MoteData md;
  private int count =0;
  /**
   * 
   * vector where Messages are buffered before writing it to a file
   */
  private Vector Buffer;
  /**
   * Pheonix Source
   */
  private PhoenixSource ps;
  /**
   * Buffer vector size()
   */
  private int MaxBufferSize=10;
  /**
   * bufferd writer used to write to a file
   */
  BufferedWriter bw;
  /**
   * boolean which indicates state of the data logger
   */
  private boolean STATE;
  /**
   * print Stream
   * @param source
   */
  private MyPrintStream mps;
  /**
   * 
   * to find if it is detected 
   */
  private DLHelper dlh;
  /**
   * mote properties
   * 
   */
  private Mote mote;
  /**
   * Creates new datalogger
   * @param source : source from which data is to be collected 
   */
  public DataLogger(String source)  {
    if (source != null) {
    	//write null after debugging
    	ps = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
    	ps.start();
        moteIF = new MoteIF(ps);
    }
    else {
    	ps = BuildSource.makePhoenix(PrintStreamMessenger.err);
    	ps.start();
      moteIF = new MoteIF(ps);
    }
    Buffer = new Vector();
    
  }
  /**
   * returns true if datalogger has started correctly else false. 
   * @return
   */
  public boolean getStatus() {
	  if(ps == null) return false;
	  if(moteIF == null) return false;
//	  if(bw == null)
	  return true;
  }
  /**
   * helper function for consructor
   * @param j
   * @throws Exception
   */
  public void initialize(MoteData j) throws Exception {
	  STATE = false;
	  String source = j.getSource();
	  mps = new MyPrintStream(j.getMoteId());
	  if (source != null) {
		  System.out.println("my data logger");
		 ps = BuildSource.makePhoenix(source, mps);
		 if(ps != null)
	      moteIF = new MoteIF(ps);
		 else{
			 moteIF = null;
		 }
	    }
	    else {
	    	ps = BuildSource.makePhoenix(null);
//	    	ps.start();
	    	if(ps !=null)
	    	moteIF = new MoteIF(ps);
	    	else{
				 moteIF = null;
	    	}
	    } 
	  Enumeration e = j.getClassFile().elements();
	  while(e.hasMoreElements()){
		  System.out.println("class file");
		  moteIF.registerListener((Message)e.nextElement(), this);
	  }
	  Buffer = new Vector();
	  try{
		  System.out.println("writing data collected at mote to file "+md.getDataFile());
		  bw = new BufferedWriter(new FileWriter(md.getDataFile()));
		  
		  STATE = true;
	  }catch(IOException ee){
		  stamp("file to write data could not be opened");
		  ee.getMessage();
		  ee.printStackTrace();
		  throw ee;
	  }
	  
	  dlh = new DLHelper(mote.getMoteId(),mote.getFName(),mote.getMotePort());
	  System.out.println(mote.getMoteId()+" "+mote.getFName()+"  "+mote.getMotePort());
	  dlh.start();
	  
  }
  /**
   * For debugging
   * @param s
   */
  public void stamp(String s){
	  System.out.println("DATALOGGER:  "+s);
  }
  /**
   * creates DataLogger 
   * @param j
   * @param mote
   * @throws Exception
   */
  public DataLogger(MoteData j, Mote mote) throws Exception{
	  this.md=j;
	  this.mote = mote;
	  try{
	  initialize(j);
	  
	  }catch(Exception e){
		  throw e;
	  }
	  
  }
  /**
   * stops datalogger, first by writing messages in the buffer to file and then 
   * @throws Exception
   */
  public void stop() throws Exception{
	  dlh.stop();
	  if(bw != null) {
		  for(int i =0;i<Buffer.size();i++) {
	    		ModifiedMessage m = (ModifiedMessage)Buffer.get(i);
	    		try{
	    		bw.write(m.getStamp()+"\n");
	    		bw.write(m.getOrigMessage().toString()+"\n");
	    		//System.out.println(m.getOrigMessage().toString()+"\n");
	    		}catch(Exception e){
	    			System.out.println("in file data logger in function message received");
	    			e.printStackTrace();
	    		}
		  }
	    	bw.close();
	  }
	  if(ps!= null)
	  ps.shutdown();
	  ps = null;
	  moteIF = null;
	  Thread.sleep(2000);
	  
  }
  public void start() {
	 
  }
  /**
   * this function is called when a message is received , it first stores the messages in a buffer till it reaches maxbuffersize and then writes messages to file. messages are stored in buffer to avoid file operations
   */
  public void messageReceived(int to, Message message) {
	  
	 
    long t = System.nanoTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SS");
    Date date = new Date();
    String stamp = dateFormat.format(date);
    ModifiedMessage mm = new ModifiedMessage(message,stamp,md.getMoteId());
    
    if(Buffer.size()<MaxBufferSize) {
    	   	Buffer.add(mm);
    	 }
    else{
    	for(int i =0;i<Buffer.size();i++) {
    		ModifiedMessage m = (ModifiedMessage)Buffer.get(i);
    		try{
    		bw.write(m.getStamp()+"\n");
    		bw.write(m.getOrigMessage().toString()+"\n");
    		//System.out.println(m.getOrigMessage().toString()+"\n");
    		}catch(Exception e){
    			System.out.println("in file data logger in function message received");
    			e.printStackTrace();
    		}
    	}
    	Buffer = new Vector();
    	Buffer.add(mm);
    }
    
    //    Date d = new Date(t);
   // System.out.print("from job "+md.num+"   " + t + ": ");
//    System.out.println(message);
  }

  
  private static void usage() {
    System.err.println("usage: DataLogger [-comm <source>] message-class [message-class ...] goto hell");
  }
/**
 * registers the given message type to serial forwarder.
 * @param msg
 */
  private void addMsgType(Message msg) {
    moteIF.registerListener(msg, this);
  }
  
  public static void  otherfunction() throws Exception{
	  String[] arg = new String[5];
	  arg[0] = "-no-gui";
	  arg[1] = "-comm";
	  arg[2] = "serial@/dev/ttyUSB0:telosb";
	  arg[3] = "-port";
	  arg[4] = "9002";
      MySerialForwarder sf = new MySerialForwarder("9002","/dev/ttyUSB0");
      sf.start();
      sf.check();
      SF.add(sf);
    // System.out.println(sf.motecom+"  "+ sf.serverPort);
	  MoteIF mif ;
	  File f = new File("/home/ashwin/motenet/ashwin/job9/");
	  URL[] u = new URL[]{f.toURL()};
	  ClassLoader cl = new URLClassLoader(u);// ClassLoader(u);
	  Class cc = Class.forName("StatusMessage",false,cl);
	  //Class cc = Class.forName("BlinkToRadioMsg");
	  Object p = cc.newInstance();
	  String s =null;
	  
	  Vector v = new Vector();
	  v.add(p);
	  MoteData md = new MoteData("sf@localhost:9002",v,1,"datafile");
	  Mote m =null;
	  DataLogger dl = new DataLogger(md,m);
	  
	  DL.add(dl);
	  
	 
  }
  public static void main(String[] args) throws Exception {
	  
	  
	  
	  /***********88DBLOgger code *************
    String source = null;
    int i=0;
    //take input file
    BufferedReader br = new BufferedReader(new FileReader(args[0]));
    while(br.ready()) {
    	StringTokenizer st = new StringTokenizer(br.readLine());
    	String token = st.nextToken();
    	Vector cf = new Vector();
    	MoteData j = new MoteData();
    	if(token.equals("-comm")) j.setSource(st.nextToken());
    	else cf.add(token);
    	while(st.hasMoreTokens()){
    		Class c = Class.forName(st.nextToken());
    		Object packet = c.newInstance();
    		Message m = (Message)packet;
    		cf.add(m);
    	}
    	j.setClassFile(cf);
    	j.num=i++;
    	DataLogger dl = new DataLogger(j);
    	dl.start();
    }
    /***********/
//	  otherfunction();
//while(true) {
//		  
//	  }
	//  moteIF = new MoteIF(BuildSource.makePhoenix(source, PrintStreamMessenger.err));
	  
	//  PacketSource pe = BuildSource.makePacketSource("sf@127.0.0.1:9002");
	  
//	  PhoenixSource ps =  BuildSource.makePhoenix("sf@localhost:9002",PrintStreamMessenger.err);
	  //System.out.println("hhhhhh");
//	  mif  = new MoteIF(ps);
	  //System.out.println("hhhhhhhhhh");

//	  mif.registerListener((Message)p,dl);
	  //otherfunction();
//	  
	//
	  
    /*
    Vector v = new Vector();
    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
	if (args[i].equals("-comm")) {
	  source = args[++i];
	}
	else {
	  String className = args[i];
	  try {
	    Class c = Class.forName(className);
	    Object packet = c.newInstance();
	    Message msg = (Message)packet;
	    v.addElement(msg);
	  }
	  catch (Exception e) {
	    System.err.println(e);
	  }
	}
      }
    }
    
    else if (args.length != 0) {
      usage();
      System.exit(1);
    }
	
    DataLogger dl = new DataLogger(source);
    Enumeration msgs = v.elements();
    while (msgs.hasMoreElements()) {
      Message m = (Message)msgs.nextElement();
      dl.addMsgType(m);
    }
    dl.start();
    */
    
    
  }
public MoteData getMd() {
	return md;
}
public void setMd(MoteData md) {
	this.md = md;
}


}




//
//
//import java.io.IOException;
//
//import net.tinyos.*;
//import net.tinyos.message.MoteIF;
//import net.tinyos.packet.BuildSource;
//import net.tinyos.packet.PhoenixError;
//import net.tinyos.packet.PhoenixSource;
//
//
//public class DataLogger implements PhoenixError {
//
//	
//	public  void error(IOException e){
//		System.out.println("Error in data logger : ");
//		e.printStackTrace();
//	}
//public void connector() {
//	
//	/*
//	 * ourPhoenix = 
//          BuildSource.makePhoenix("sf@" + connectString + ":" +
//                                  connectPort, null);
//        ourPhoenix.setPacketErrorHandler(this);
//        sf = new MoteIF(ourPhoenix, connectGID);
//	 */
//	PhoenixSource ps = BuildSource.makePhoenix("sf@localhost:9002", null);
//	ps.setPacketErrorHandler(this);
//	MoteIF mi = new MoteIF(ps);
//	
//	
//	}
//	
//	
//	//assume that you are given computer node id , usb address ,check status of the mote 
//	
//	//resolve the issue of remote usb address
//	
//	//let us for now start serial forwarder on local machine 
//	
//	
//}