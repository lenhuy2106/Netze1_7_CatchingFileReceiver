import java.io.Serializable;

/**
 * @author Nhu Huy Le & Long Matthias Yan
 * 
 * Allowing serialized file handling at sending and receiving.
 *
 */
public class FileObject implements Serializable {

	public FileObject() {}
	
	private static final long serialVersionUID = 4523626L;
	
	private String destinationDir;
	private String sourceDir;
	private String fileName;
	private long fileSize;
	private byte[] data;
	
	public String getDestinationDir() {
		return destinationDir;
	}
	public void setDestinationDir(String destinationDir) {
		this.destinationDir = destinationDir;
	}
	public String getSourceDir() {
		return sourceDir;
	}
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
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
