import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Random;

// -- validating p
class FaultyDatagramPacket {
	private DatagramPacket datagramPacket;
	Random random = null;
	long randomBit;
	byte[] modifiedData;
	
	/**
	 * @param datagramPacket
	 * @param fileObject
	 * @param pBitFlip
	 * @param pPacketLoss
	 * @param pPacketDuplicate
	 * @throws IOException
	 */
	public FaultyDatagramPacket(DatagramPacket datagramPacket,
								FileObject fileObject,							
								double pPacketLoss,
								double pPacketDuplicate,
								double pBitFlip) throws IOException {
		this.datagramPacket = datagramPacket;
		// deep copy
		modifiedData = datagramPacket.getData().clone();
		
		random  = new Random();
		randomBit = random.nextInt(modifiedData.length  * 8);
		

		
		// lose packet
		if (random.nextInt(100) < (pPacketLoss * 100)) {
			modifiedData = null;
		}
		
		// duplicate packet
		if (random.nextInt(100) < (pPacketDuplicate * 100))	{	
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			bais.write(modifiedData);
			bais.write(modifiedData);
			modifiedData = bais.toByteArray();
			
		// flip Bit
		if (random.nextInt(100) < (pBitFlip * 100))	
			modifiedData[(int) (randomBit / 8)] = (byte) (modifiedData[(int) (randomBit / 8)] ^ (1 << randomBit % 8));	
		}
	}
	
	public boolean equals(Object obj) {
		return datagramPacket.equals(obj);
	}

	public InetAddress getAddress() {
		return datagramPacket.getAddress();
	}

	public int getLength() {
		return datagramPacket.getLength();
	}

	public int getOffset() {
		return datagramPacket.getOffset();
	}

	public int getPort() {
		return datagramPacket.getPort();
	}

	public SocketAddress getSocketAddress() {
		return datagramPacket.getSocketAddress();
	}

	public int hashCode() {
		return datagramPacket.hashCode();
	}

	public void setAddress(InetAddress arg0) {
		datagramPacket.setAddress(arg0);
	}

	public void setData(byte[] arg0, int arg1, int arg2) {
		datagramPacket.setData(arg0, arg1, arg2);
	}

	public void setData(byte[] arg0) {
		datagramPacket.setData(arg0);
	}

	public void setLength(int arg0) {
		datagramPacket.setLength(arg0);
	}

	public void setPort(int arg0) {
		datagramPacket.setPort(arg0);
	}

	public void setSocketAddress(SocketAddress arg0) {
		datagramPacket.setSocketAddress(arg0);
	}

	public String toString() {
		return datagramPacket.toString();
	}

	public byte[] getData() { 
		return modifiedData;
	}
}