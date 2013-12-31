import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
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

// -- pimp console outputs
public class CatchingFileReceiver {

	private static final int PORT = 4711;
	private static final int TIMEOUT = 100000;
	private DatagramSocket socket = null;
	private FileObject fileObject = null;
	private FileObject[] fileGather = null;
	CRC32 crc = null;
	int parts;
	
	// 63000 kb size max
	// -- actually buffersize!
	private final double packetSize = 1024 * 63 * 1000;
	
	
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

				crc = new CRC32();
				int curSeqnum = -1;
				boolean ack = false;

				// receive fileObjects
				do {
					byte[] buffer = new byte[(int) packetSize];
					DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
					System.out.println();
					System.out.print("receiving packets...");
					socket.receive(dataPacket);	
					System.out.println("done.");
					System.out.println("packet-size: " + dataPacket.getLength());
					System.out.println("from: " + dataPacket.getSocketAddress());

					// -- throws nullptr-excptn
					// simulating network issues
						// DatagramPacket Wrapper
					System.out.print("simulating network issues...");
					FaultyDatagramPacket faultyDataPacket = new FaultyDatagramPacket(dataPacket,
																							deserializeData(dataPacket.getData()),
																							pPacketLoss,
																							pPacketDuplicate,
																							pBitFlip);
					System.out.println("done.");
					System.out.print("pPacketLoss: " + pPacketLoss);
					System.out.print(", pPacketDuplicate: " + pPacketDuplicate);
					System.out.println(", pBitFlip: " + pBitFlip);

					System.out.print("deserializing data...");

					fileObject = deserializeData(faultyDataPacket.getData());

					System.out.println("done.");
					System.out.println("File-Name: " + fileObject.getFileName());
					System.out.println("File-Size: " + fileObject.getFileSize());
					System.out.println("Packet-SeqNo: " + fileObject.getSeqnum());
					System.out.println("Packet-ACK: " + fileObject.getAck());
					
					// ACK validation
					System.out.print("ACK valid...");
					boolean validPacket = (fileObject.getAck() != ack);
					System.out.println(validPacket);
					
					// checksum validation
					System.out.print("checksum valid...");
					long checksum = fileObject.getChecksum();
					fileObject.setChecksum(-1);
						// -- after 1 file or packet loss: update bug -> never valid
					crc.update(serializeObject(fileObject));

					validPacket = validPacket && (crc.getValue() == checksum);
					System.out.print(validPacket);
					System.out.println (": " + crc.getValue() + "==" + checksum);
					crc.reset();

					// seqnum validation
						// -- ( saving higher packets in stack ? )
					System.out.print("SeqNo valid...");
					validPacket = validPacket && (fileObject.getSeqnum() == curSeqnum + 1);
					System.out.print(validPacket);
					System.out.println (": " + fileObject.getSeqnum() + "==" + curSeqnum + "+1");

					if (validPacket) {
						
						// sufficient array length
						if (fileGather == null) {
							System.out.print("Gather file...");
							// -- packetSize != bufferSize
								// -- probably new attribute in header
							// -- last packet lost
								// -- ceiling wrong ?
									// -- -1
							parts  = (int) Math.ceil(fileObject.getFileSize() / (1024 * 63)) + 1;
							fileGather = new FileObject[parts];
							System.out.println(parts + " part(s).");
						}

						// collect fileObject
						System.out.print("Collecting...");
						fileGather[fileObject.getSeqnum()] = fileObject;
						System.out.println(fileObject.getSeqnum());

						// full size reached: create file with fileObject[]
							// packetSize != bufferSize
						// if (fileObject.getFileSize() == fileObject.getSeqnum() * (1024 * 63)) {

						// reduce by 2: began at -1 and late incremented
						if (curSeqnum == parts - 2) {
							writeFile();
							System.out.println("File created!");
							curSeqnum = -2;
						}

						ack = !ack;
						curSeqnum++;
					}
					
					// send response ( boolean )
					System.out.print("sending response...");
					byte[] responseData = new byte[] {(byte) (ack?1:0)};				
					DatagramPacket responsePacket = new DatagramPacket(responseData, 
																		responseData.length,
																		dataPacket.getAddress(),
																		dataPacket.getPort());
					socket.send(responsePacket);
					System.out.println("done.");
					System.out.println("size: " + responseData.length);
					System.out.println("response-Ack: " + ack);
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
		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (FileObject) ois.readObject();
		} catch (EOFException e) {
			return new FileObject();
		}
	}

	/**
	 * writes file according to fileObject in same dir
	 */
	private void writeFile() {
		//filepath to save
		File file = new File(fileObject.getFileName() + "sent" );
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			
			// merge packets fileObject[]
			for (int i = 0; i < parts; i++)
				fos.write(fileGather[i].getData());
			
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

