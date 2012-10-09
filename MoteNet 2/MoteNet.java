/**
 * This is main class through which all the programs are strated
 * @au
 *
 */
public class MoteNet {
	/**
	 * boolean to indicate if all threads to be stopped
	 */
	public static boolean STOP = false;
	/**
	 * Current running job id
	 */
	public static int CURRENTJOBID;
	/**
	 * This is for starting minore node 
	 * @param args
	 */
	public static void main(String args[]) {
		
		new Trigger().start();
		//new UserHandler().start();
	}
}
