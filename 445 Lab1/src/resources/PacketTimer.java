/**
 * 
 */
package resources;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

/**
 * @author chris
 *
 */
public class PacketTimer extends TimerTask {

	private DatagramPacket dp;
	private DatagramSocket ds;
	
	public PacketTimer ()
	{
		
	}
	
	public PacketTimer (DatagramPacket dp, DatagramSocket ds)
	{
		this.dp = dp;
		this.ds = ds;
		
	}
	
	
	/**
	 * @return the dp
	 */
	public DatagramPacket getDp() {
		return dp;
	}

	/**
	 * @param dp the dp to set
	 */
	public void setDp(DatagramPacket dp) {
		this.dp = dp;
	}

	/**
	 * @return the ds
	 */
	public DatagramSocket getDs() {
		return ds;
	}

	/**
	 * @param ds the ds to set
	 */
	public void setDs(DatagramSocket ds) {
		this.ds = ds;
	}

	@Override
	public void run() {
		
		try {
			ds.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
