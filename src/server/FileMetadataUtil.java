package server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;

public class FileMetadataUtil {
	
	// TODO: logg logg logg
	public static String[] getXPKeywords( String path ) {
		return getXPKeywords( new File( path ));
	} public static String[] getXPKeywords( File file ) {
		try {
			final JpegImageMetadata jpeg = (JpegImageMetadata) Imaging.getMetadata( file );
			if (jpeg == null) return new String[0];
			final TiffField field = jpeg.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
			if (field == null) return new String[0];
			return field.getStringValue().trim().split( ";" );
		} catch( ImageReadException | IOException e ) {
			e.printStackTrace(System.out);
			return new String[0];
		}
	}
}
