import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.zip.CRC32;

public class CatchingFileReceiver {

	private static final int PORT = 4711;
	private static final int TIMEOUT = 5000;
	private DatagramSocket socket = null;
	private FileObject fileObject = null;
	private FileObject[] fileGather = null;
	CRC32 crc = null;
	
	// 63000 kb size max
	private final double packetSize = 1024 * 1000 * 63;
	private byte[] buffer = new byte[(int) packetSize];
	
	public static void main(String[] args) throws IOException {
		
		CatchingFileReceiver cfr = new CatchingFileReceiver();
		cfr.catchingReceive(0.1, 0.05, 0.05);
	}
	
	/**
	 * Listens on datagram socket and serializes incoming bytes data to file object.
	 * @param pBitFlip 
	 * @param pPacketLoss 
	 * @param pPacketDuplicate 
	 * @throws IOException 
	 */
	private void catchingReceive(double pPacketLoss, double pPacketDuplicate, double pBitFlip) throws IOException {
		try {
			socket = new DatagramSocket(PORT);
			socket.setSoTimeout(TIMEOUT);
			
			while (true) {
				DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
				crc = new CRC32();
				int curSeqnum = -1;
				FileObject fileResponse = new FileObject();
				fileResponse.setAck(false);

				// receive fileObjects
				do {
					socket.receive(dataPacket);				
					
					// simulating network issues
						// DatagramPacket Wrapper
					FaultyDatagramPacket faultyDataPacket = new FaultyDatagramPacket(dataPacket,
																							deserializeData(dataPacket.getData()),
																							pPacketLoss,
																							pPacketDuplicate,
																							pBitFlip);
					
					fileObject = deserializeData(faultyDataPacket.getData());				
					
					// ACK validation
					boolean validPacket = (fileObject.getAck() != fileResponse.getAck());
					
					// checksum validation
					long checksum = fileObject.getChecksum();
					fileObject.setChecksum(-1);
					crc.update(serializeObject(fileObject));
					validPacket = validPacket && (crc.getValue() == checksum);
					
					// larger files: seqnum validation
						// -- ( saving higher packets in stack ? )
					validPacket = validPacket && (fileObject.getSeqnum() == curSeqnum + 1);
					
					if (validPacket) {
						// sufficient array length
						if (fileGather == null)
							fileGather = new FileObject[(int) Math.ceil(fileObject.getFileSize() / packetSize)];
		
						// collect fileObject
						fileGather[fileObject.getSeqnum()] = fileObject;

						// full size reached: create file with fileObject[]
						if (fileObject.getFileSize() == fileObject.getSeqnum() * packetSize) 
							writeFile();

						fileResponse.setAck(!fileResponse.getAck());
						curSeqnum++;
					}

					// send fileResponse
					byte[] responseData = serializeObject(fileResponse);				
					DatagramPacket responsePacket = new DatagramPacket(responseData, 
																		responseData.length,
																		dataPacket.getAddress(),
																		dataPacket.getPort());
					socket.send(responsePacket);
					System.out.println("FileResponse (ABP) sent.");
					// Thread.sleep(3000);
				} while (true);
				
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
	 * @param fileObject
	 * @return
	 * @throws IOException
	 */
	private byte[] serializeObject(FileObject fileObject) throws IOException {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bais);  
		oos.writeObject(fileObject);
		return bais.toByteArray();
	}

	/**
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private FileObject deserializeData(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return (FileObject) ois.readObject();
	}

	/**
	 * writes file according to fileObject in same dir
	 */
	private void writeFile() {
		File file = new File(fileObject.getFileName());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			// -- merge packets fileObject[]
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

