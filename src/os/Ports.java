package os;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Ports 
{
	public static final int MIN_PORT_NUMBER = 1100;
	public static final int MAX_PORT_NUMBER = 49151;
	
	public static int getAvailablePort()
	{
		for(int i=MIN_PORT_NUMBER;i<MAX_PORT_NUMBER;i++)
		{
			if(available(i))
				return i;
		}
		return -1;
	}
	
	public static boolean available(int port) 
	{
	    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
}
