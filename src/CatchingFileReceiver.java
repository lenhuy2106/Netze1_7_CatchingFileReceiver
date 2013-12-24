import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class CatchingFileReceiver {

	private DatagramSocket socket = null;
	private FileObject fileObject = null;
	
	private final int port = 4711;
	
	// why two byte arrays neccessary ?
	private byte[] buffer = new byte[1024 * 1000 * 63];
	byte[] data = null;
	
	public static void main(String[] args) {
		
		CatchingFileReceiver cfr = new CatchingFileReceiver();
		cfr.DatagramReceivingLoop();
	}
	
	/**
	 * listens on datagram socket
	 * serializes incoming bytes data to file object
	 */
	public void DatagramReceivingLoop() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			
			while (true) {
				DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
				socket.receive(dataPacket);
				data = dataPacket.getData();
				
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bais);
				
				// Serializing
				fileObject = (FileObject) ois.readObject();
				
				// serializing error handling
//				if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
//	            System.out.println("Some issue happened while packing the data @ client side");
//	            	System.exit(0);
//	      		}
				
			}
			
		}
		
		catch() {}
	}

}
