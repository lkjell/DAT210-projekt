package server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

public class ImageOrientation {	

	// Inner class containing image information
	public static class ImageInformation {
	    
		public final int orientation;
	    public final int width;
	    public final int height;
	
	    public ImageInformation(int orientation, int width, int height) {
	        this.orientation = orientation;
	        this.width = width;
	        this.height = height;
	    }
	
	    public String toString() {
	        return String.format("%dx%d,%d", this.width, this.height, this.orientation);
	    }
	}
	
	
	public static ImageInformation readImageInformation(File imageFile)  throws IOException, MetadataException, ImageProcessingException {
	    Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
	    Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
	    JpegDirectory jpegDirectory = (JpegDirectory)metadata.getDirectory(JpegDirectory.class);
	
	    int orientation = 1;
	    try {
	        orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
	    } catch (MetadataException me) {
	        System.out.println("Could not get orientation");
	    }
	    int width = jpegDirectory.getImageWidth();
	    int height = jpegDirectory.getImageHeight();
	
	    return new ImageInformation(orientation, width, height);
	}
	
}