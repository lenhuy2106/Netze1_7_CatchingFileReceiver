import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class CatchingFileReceiver {

	private static final int PORT = 4711;
	private static final int TIMEOUT = 5000;
	private DatagramSocket socket = null;
	private FileObject fileObject = null;
	
	// 63000 kb size
	private byte[] buffer = new byte[1024 * 1000 * 63];
	
	public static void main(String[] args) throws IOException {
		
		CatchingFileReceiver cfr = new CatchingFileReceiver();
		cfr.DatagramReceivingLoop();
	}
	
	/**
	 * listens on datagram socket
	 * serializes incoming bytes data to file object
	 * @throws IOException 
	 */
	public void DatagramReceivingLoop() throws IOException {
		try {
			socket = new DatagramSocket(PORT);
			socket.setSoTimeout (TIMEOUT);
			
			while (true) {
				DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
				socket.receive(dataPacket);
				byte[] data = dataPacket.getData();
				
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bais);
				
				// Serializing
				fileObject = (FileObject) ois.readObject();
				
				// serializing error handling
//				if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
//	            System.out.println("Some issue happened while packing the data @ client side");
//	            	System.exit(0);
//	      		}
				
				// -- create file with FileObject
				writeFile();
				
				// sending response
					// -- alternating bit protocol
				
				
				InetAddress IpAddress = dataPacket.getAddress();
				int port = dataPacket.getPort();
				String response = "Message received.";
				byte[] responseData = response.getBytes();
				DatagramPacket responsePacket = new DatagramPacket(responseData, 
																	responseData.length,
																	IpAddress,
																	port);
				socket.send(responsePacket);
				// Thread.sleep(3000);
			}
		
	    } catch (SocketException e) {
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    } catch (SocketTimeoutException e) {
	    	e.printStackTrace();
	    	// -- print stats ?
	    }
	}
	
	/**
	 * writes file according to fileObject in same dir
	 */
	private void writeFile() {
		File file = new File(fileObject.getFileName());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(fileObject.getData());
			fos.flush();
			fos.close();
		    System.out.println("File saved.");
		
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

}
