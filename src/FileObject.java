import java.io.Serializable;
import java.util.zip.CRC32;

/**
 * @author Nhu Huy Le & Long Matthias Yan
 * 
 * Allowing serialized file handling at sending and receiving.
 *
 */
public class FileObject implements Serializable {

	public FileObject() {}
	
	private static final long serialVersionUID = 4523626L;
	
	// recognizing errors
	private int seqnum; // starts at zero
	private boolean ack;
	private long checksum;
	
	// has to be set
	CRC32 crc = null;

	private String fileName;
	
	// incl. overhead
	private long fileSize;
	private byte[] data;
	
	public int getSeqnum() {
		return seqnum;
	}
	public void setSeqnum(int seqnum) {
		this.seqnum = seqnum;
	}
	public boolean getAck() {
		return ack;
	}
	public void setAck(boolean ack) {
		this.ack = ack;
	}
	public long getChecksum() {
		return checksum;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;

	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
