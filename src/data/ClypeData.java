/**
 * @author James Meeker
 * 
 * Main data class for the Clype Program
 * Implemented via it's two subclasses, FileClypeData and MessageClypeData
 * 
 * userName 	A string which holds the username of whoever sent the message or file
 * type 		An int which describes the purpose of a particular data class
 * date 		Holds the time in which the ClypeData object was created
 */
	

package data;

import java.io.Serializable;
import java.util.Date;

public abstract class ClypeData implements Serializable{
	
	protected static final int LISTUSERS = 0;
	protected static final int DONE = 1;
	protected static final int SENDFILE = 2;
	protected static final int SENDMESSAGE = 3;
	
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private String userName;
	private int type;
	private Date date;
	
	private int getPos(char c)
	{
		return Math.floorMod(ALPHABET.indexOf(c), 26);
		
	}
	
	private char getChar(int i)
	{
		return ALPHABET.charAt(Math.floorMod(i, 26));
	}
	
	protected String encrypt(String inputStringToEncrypt, String key)
	{
		String output = "";
		
		for(int i=0; i<inputStringToEncrypt.length(); i++)
		{
			if(ALPHABET.contains(Character.toString(inputStringToEncrypt.charAt(i))))
			{
				output += getChar(getPos(inputStringToEncrypt.charAt(i)) + getPos(key.charAt(Math.floorMod(i, key.length()))) );
			}
			else
				output += inputStringToEncrypt.charAt(i);
		}
		
		return output;
	}
	
	protected String decrypt(String inputStringToDecrypt, String key)
	{
		String output = "";
		
		for(int i=0; i<inputStringToDecrypt.length(); i++)
		{
			if(ALPHABET.contains(Character.toString(inputStringToDecrypt.charAt(i))))
			{
				output += getChar(getPos(inputStringToDecrypt.charAt(i)) - getPos(key.charAt(Math.floorMod(i, key.length()))) );
			}
			else
				output += inputStringToDecrypt.charAt(i);
		}
		
		return output;
	}

	public ClypeData(String userName, int type)
	{
		this.userName = userName;
		this.type = type;
		this.date = new Date();
	}
	
	public ClypeData(int type)
	{
		this("Anon", type);
	}

	public ClypeData()
	{
		this("Anon", LISTUSERS);
	}
	
	public String getUserName() {
		return userName;
	}

	public int getType() {
		return type;
	}

	public Date getDate() {
		return date;
	}
	
	abstract String getData();
	
	abstract String getData(String key);
	
}