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
	private int seqnum; 
	private int acknum;
	private long checksum;
	CRC32 crc = null;

	private String fileName;
	private long fileSize;
	private byte[] data;
	
	public int getSeqnum() {
		return seqnum;
	}
	public void setSeqnum(int seqnum) {
		this.seqnum = seqnum;
	}
	public int getAcknum() {
		return acknum;
	}
	public void setAcknum(int acknum) {
		this.acknum = acknum;
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

	// auto generates checksum
	public void setData(byte[] data) {
		this.data = data;
		crc = new CRC32();
		crc.update(data);
		checksum = crc.getValue();
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
