package data;

/**
 * @author pawlactb
 *
 */
public class MessageClypeData extends ClypeData {
	
	private String message;
	
	/**
	 * @param userName User name of the client sending data.
	 * @param message Contents of text transmission.
	 * @param type Type of transmission.
	 * @param key Encryption key.
	 */
	public MessageClypeData(String userName, String message, int type, String key) {
		super(userName, type);
		this.message = this.encrypt(message, key);
		
		if(type == ClypeData.FILE){
			throw new IllegalArgumentException("FileClypeData instantiated with non-file type.");
		}
	}
	
	/**
	 * @param userName Username of the client sending data.
	 * @param message Contents of text transmission.
	 * @param type Type of transmission.
	 */
	public MessageClypeData(String userName, String message, int type) {
		super(userName, type);
		this.message = message;
		
		if(type == ClypeData.FILE){
			throw new IllegalArgumentException("FileClypeData instantiated with non-file type.");
		}
	}
	
	public MessageClypeData() {
		super();
		this.message = "";
	}

	/* (non-Javadoc)
	 * @see data.ClypeData#getData()
	 */
	public String getData() {
		return this.message;
	}
	
	public String getData(String key) {
		return this.decrypt(this.message, key);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		//TODO: implement.
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "MessageClypeData\n"+
				"Username: " + this.userName + "\n" +
				"Message: " + this.message + "\n" +
				"";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		boolean data, type, userName;
		
		if (!(other instanceof ClypeData))
		{
			return false;
		}
		
		if (other instanceof MessageClypeData) {
			data = ((MessageClypeData) other).getData() == this.getData();
		}
		else return false;
		
		type = this.getType() == ((MessageClypeData)other).getType();
		userName = this.getUserName() == ((MessageClypeData)other).getUserName();
		
		return data && type && userName;
	}

}
