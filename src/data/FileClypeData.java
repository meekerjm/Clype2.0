package data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author pawlactb
 *
 */
public class FileClypeData extends ClypeData {
	
	private String fileName, fileContents;
	
	
	public FileClypeData() {
		super();
		this.fileName = "";
		this.fileContents = "";
	}
	
	/**
	 * @param userName Username of the client sending data.
	 * @param fileName Filename of the file sent.
	 * @param type Type of transmission.
	 */
	public FileClypeData(String userName, String fileName, int type) throws IllegalArgumentException  {
		super(userName, type);
		this.fileName = fileName;
		this.fileContents = "";
		
		if(type != ClypeData.SENDFILE){
			throw new IllegalArgumentException("FileClypeData instantiated with non-file type.");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		boolean data, fileContents, type, userName;
		
		if (!(other instanceof ClypeData))
		{
			return false;
		}
		
		if (other instanceof FileClypeData) {
			data = ((FileClypeData) other).getData() == this.getData();
		}
		else return false;
		
		type = this.getType() == ((FileClypeData)other).getType();
		userName = this.getUserName() == ((FileClypeData)other).getUserName();
		
		return data && type && userName;
		
	}

	/* (non-Javadoc)
	 * @see data.ClypeData#getData()
	 */
	public Object getData() {
		return this.fileContents;
	}

	
	public String getData(String key) {
		return this.decrypt(this.fileContents, key);
	}
	
	/**
	 * @return Returns the filename of the sent file.
	 */
	public String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileContents == null) ? 0 : fileContents.hashCode());
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}
	
	public void readFileContents() throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(this.fileName);
			fr.read(this.fileContents.toCharArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		finally{
			try {
				if(fr != null)
				{
					fr.close();
				}
				
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param key encryption key
	 * @throws IOException if there is an issue reading file.
	 */
	public void readFileContents(String key) throws IOException {
		this.readFileContents();
		this.fileContents = this.encrypt(this.fileContents, key);
	}
	
	/**
	 * @param fileName New path to file.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 
	 */
	public void writeFileContents() {
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(this.fileName);
			fw.write(this.fileContents);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		finally{
			try{
				if(fw != null) {
					fw.close();
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param key decryption key
	 */
	public void writeFileContents(String key) {
		this.fileContents = this.decrypt(this.fileContents, key);
		this.writeFileContents();
	}

}
