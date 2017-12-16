package data;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import javax.imageio.ImageIO;

public class PhotoClypeData extends ClypeData {
	private String fileName;
	private transient BufferedImage image;
	

	
	public PhotoClypeData(String userName, String imagePath) {
		super(userName, ClypeData.PHOTO);
		this.fileName = imagePath;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
         ImageIO.write(image, "png", out);
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            image = ImageIO.read(in);
        }        
        
        /**
	 * Returns stored image
	 * @return the image
	 */
	public BufferedImage getData() {
		return this.image;
	}
	
	@Override
	Object getData(String key) {
		return this.getData();
	}
	
	/**
	 * Reads photo from input filename
	 */
	public void readClientData() {
		try {
			this.image = ImageIO.read( new File( this.fileName ));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * @param fileName
	 */
	public void writeData(String fileName) {
		try {
			File outputFile = new File(fileName);
			ImageIO.write(this.image, "png", outputFile);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


}