/**
 * @author James Meeker
 * 
 * Main data class for the Clype Program
 * Implemented via it's two subclasses, FileClypeData and MessageClypeData
 * 
 * userName 	A string which holds the username of whoever sent the message or file
 * type 		An int which describes the purpose of a particular data class
 * date 		Holds the time in which the ClypeData object was created
 * 
 * Last modified 14 Dec 2017: added PHOTO constant.
 */
	

package data;

import java.io.Serializable;
import java.util.Date;

public abstract class ClypeData implements Serializable{
	
	public static final int LISTUSERS = 0;
	public static final int DONE = 1;
	public static final int SENDFILE = 2;
	public static final int SENDMESSAGE = 3;
	public static final int PHOTO = 4;
	
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private String userName;
	private int type;
	private Date date;
	
	public ClypeData()
	{
		this("Anon", LISTUSERS);
	}
	
	public ClypeData(int type)
	{
		this("Anon", type);
	}
	
	public ClypeData(String userName, int type)
	{
		this.userName = userName;
		this.type = type;
		this.date = new Date();
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
	
	private char getChar(int i)
	{
		return ALPHABET.charAt(Math.floorMod(i, 26));
	}

	abstract Object getData();
	
	abstract Object getData(String key);

	public Date getDate() {
		return date;
	}

	private int getPos(char c)
	{
		return Math.floorMod(ALPHABET.indexOf(c), 26);
		
	}
	
	public int getType() {
		return type;
	}
	
	public String getUserName() {
		return userName;
	}
	
}