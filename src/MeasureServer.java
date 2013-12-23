import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author Nhu-Huy Le & Mathias Long Yan
 * 
 * Measures the data-rate of incoming packets. 
 * Times out if nothing is sent anymore.
 *
 */
public class MeasureServer {

	private byte[] buffer = new byte[1400];
	private double startTime = 0;
	private int receivedSum = 0;
	private int port = 4711;
//	in milliseconds
	private int timeOut = 10000;

	/**
	 * Listens to a port by a protocol.
	 * 
	 * @param args0				Receiving UDP or TCP?
	 * @param args1				Milliseconds til server times out.
	 * @param args2				Server buffer for receiving data.
	 * @param args3				Listening port.
	 * @throws IOException		
	 */
	public static void main (String[] args) throws IOException {
				
		String protocol = args[0];
		MeasureServer ms = new MeasureServer (Integer.parseInt (args[1]));
		ms.buffer = new byte[Integer.parseInt (args[2])];
		ms.port = Integer.parseInt (args[3]);
		
		if (protocol.equals ("UDP")) ms.receivingUDP (ms.port);
		else if ( protocol.equals ("TCP")) ms.receivingTCP (ms.port);	
		else System.out.println ("Protocol unknown: UDP or TCP ?");			
	}
	
	/**
	 * Constructor.
	 * 
	 * @param timeOut			Milliseconds til server times out.
	 */
	public MeasureServer (int timeOut) {
		
		this.timeOut = timeOut;
	}
	
	/**
	 * Opens UDP socket and gives out.
	 * 
	 * @param port				Listening port.
	 * @throws IOException
	 */
	private void receivingUDP (int port) throws IOException {
		
		try (DatagramSocket serverSocket = new DatagramSocket (port);) {
			System.out.print ("receiving UDP ...");
			loopingReceiveUDP (serverSocket);
		} catch (SocketTimeoutException s) {
			System.out.println ("stopped.");
			consoleOut ();
		}
	}
	
	/**
	 * Receives data by UDP and sets the options.
	 * 
	 * @param serverSocket		
	 * @throws IOException
	 */
	private void loopingReceiveUDP (DatagramSocket serverSocket) throws IOException {
		
		DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
		
		serverSocket.setSoTimeout (timeOut);
		while (true) {
			serverSocket.receive (packet);
			if (startTime == 0) startTime = System.currentTimeMillis ();
			receivedSum++;
			packet.setLength (buffer.length);
		}
	}
	
	/**
	 * Opens TCP socket and gives out.
	 * 
	 * @param port
	 * @throws IOException
	 */
	private void receivingTCP (int port) throws IOException {
		
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.print ("receiving TCP ...");
			loopingReceiveTCP (serverSocket);
		} catch (SocketTimeoutException s) {
			System.out.println ("stopped.");
			consoleOut ();
		}
	}

	/**
	 * Receives data by TCP and sets the options.
	 * 
	 * @param serverSocket		
	 * @throws IOException
	 */
	private void loopingReceiveTCP (ServerSocket serverSocket) throws IOException {
		
		serverSocket.setSoTimeout (timeOut);
		while (true)
			try (Socket s = serverSocket.accept ();
					InputStream input = s.getInputStream ();
					BufferedReader reader = new BufferedReader (new InputStreamReader (input));) {
				if (startTime == 0) startTime = System.currentTimeMillis ();
				receivedSum++;
			}
	}
	
	/**
	 * Calculates the data receiving rate ( Goodput ), showing it on the console.
	 */
	private void consoleOut () {
		
		double goodput = 0;
		double endTime = 0;
		double receiveTime = 0;
		PrintStream out = System.out;
		
		endTime = System.currentTimeMillis() - timeOut;
		receiveTime = (endTime - startTime) / 1000;
		goodput = (receivedSum * ((buffer.length * 8)) / 1024) / receiveTime;
		out.println ("packets received: " + receivedSum);
		out.println ("packet buffer size (in byte): " + buffer.length);
		out.println ("receiving time elapsed (in s): " + receiveTime);
		out.println ("Data receiving rate (Goodput) (in kbit/s): " + goodput);
	}
}
