import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class uploads the executable on the mote
 * 
 */
public class Upload {

	public static void upload(MoteJob mj) throws Exception {

		String[] commands = new String[]{"msp430-objcopy","--output-target=ihex",
				mj.getMainFile(), mj.getMainFolder()+mj.getIhexName()};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			child.waitFor();

		}catch(Exception e){
			System.out.println(" while trying msp430-objcopy --output-target=ihex");
			throw e;
		}
		commands = new String[]{"tos-set-symbols","--objcopy","msp430-objcopy","--objdump","msp430-objdump",
				"--target","ihex",mj.getMainFolder()+mj.getIhexName(),mj.getMainFolder()+mj.getIhexName()+".out-"+mj.getMoteId(),
				"TOS_NODE_ID="+mj.getMoteId()};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			int ret = child.waitFor();
		}catch(Exception e){
			System.out.println(" while trying to copy ihex file to ihex.out file");
			throw e;
		}
		
		commands = new String[]{"tos-bsl","--telosb","-c",mj.getComPort(),"-r","-e","-I","-p",mj.getMainFolder()+mj.getIhexName()+".out-"+mj.getMoteId()};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			InputStream in = child.getErrorStream();
			String output = getString(in);
			in.close();
			int ret = child.waitFor();
			if(output.contains("Reset device")) {
				System.out.println(mj.getMainFile()+" Successfully loaded on mote/t"+mj );  }
			else{
				System.out.println("Error while loading on mote \t"+mj);
				throw new Exception();
			}

		}
		catch (Exception e) {
			stamp("Error while trying to execute tos-bsl --telosb" );
			throw e;
		}
		commands = null;
		commands = new String[]{"rm","-f",mj.getMainFolder()+mj.getIhexName()+".out-"+mj.getMoteId(),mj.getMainFolder()+mj.getIhexName(),mj.getMainFile()+".out-"+mj.getMoteId()};
		try{
			Process child = Runtime.getRuntime().exec(commands);
			child.waitFor();

		}
		catch (Exception e) {
			stamp("error while removing temporary files ");
			throw e;
		}
		
	}
	
	public static String getString(InputStream stream) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try{
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
		}
		catch (IOException e) {
			System.out.print("error while reading from inpustream");
			throw e;
		}

		return sb.toString();
	}
	
	public static void stamp(String s) {
		System.out.println("Upload "+s);
	}
}
