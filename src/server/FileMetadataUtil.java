package server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileMetadataUtil {
	
	private static Logger log = LogManager.getLogger( FileMetadataUtil.class.getName() );
	
	public static String[] getXPKeywords( String path ) {
		return getXPKeywords( new File( path ));
	} public static String[] getXPKeywords( File file ) {
		try {
			final JpegImageMetadata jpeg = (JpegImageMetadata) Imaging.getMetadata( file );
			if (jpeg == null) return new String[0];
			final TiffField field = jpeg.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
			if (field == null) return new String[0];
			return field.getStringValue().trim().split( ";" );
		} catch( ImageReadException | ClassCastException | IOException e ) {
			log.error( e, e );
			return new String[0];
		}
	}
}
