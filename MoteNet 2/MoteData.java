
import java.util.*;
/**
 * This class represents details of the files to which data received from the mote is to be written.
 * 
 *
 */
public class MoteData {
///source of data packet
	private String Source;
	private Vector ClassFile;
	private int moteId;
	private String DataFile;
	
	public int num;
	public MoteData(String Source, Vector ClassFile,int moteId,String DataFile){
		this.Source =Source;
		this.ClassFile = ClassFile;
		this.moteId = moteId;
		this.DataFile = DataFile;
	}
	public MoteData(){
		
	}
	public Vector getClassFile() {
		return ClassFile;
	}
	public void setClassFile(Vector classFile) {
		ClassFile = classFile;
	}
	public String getSource() {
		return Source;
	}
	public void setSource(String source) {
		Source = source;
	}
	public int getMoteId() {
		return moteId;
	}
	public void setMoteId(int moteId) {
		this.moteId = moteId;
	}
	public String getDataFile() {
		return DataFile;
	}
	public void setDataFile(String dataFile) {
		DataFile = dataFile;
	}
}