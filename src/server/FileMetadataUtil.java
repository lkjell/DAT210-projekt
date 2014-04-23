package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileMetadataUtil {
	
	private static Logger log = LogManager.getLogger( FileMetadataUtil.class.getName() );
	
//	public static String[] getXPKeywords( String path ) {
//		return getXPKeywords( new File( path ));
//	}

	public static String[] getXPKeywords( InputStream is, String path ) {
		try {
			final JpegImageMetadata jpeg = (JpegImageMetadata) Imaging.getMetadata( is, path );
			if (jpeg == null) return new String[0];
			final TiffField field = jpeg.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
			if (field == null) return new String[0];
			return field.getStringValue().trim().split( ";" );
		} catch( ClassCastException e ) {
			log.warn( "Image has no Jpeg metadata" );
			return new String[0];
		} catch( ImageReadException | IOException e ) {
			log.error( e, e );
			return new String[0];
		}
	}
	
	public static boolean writeXPKeywords( File file, String keywords ) {
		OutputStream os = null;
		try {
			TiffOutputSet outset = null;
			
			final JpegImageMetadata jpgMeta = (JpegImageMetadata) Imaging.getMetadata( file );
			if( jpgMeta != null ) {
				final TiffImageMetadata exif = jpgMeta.getExif();
				if( exif != null ) outset = exif.getOutputSet();
			}
			if( outset == null ) return false;

			TiffOutputDirectory ifd = outset.getOrCreateExifDirectory();
			ifd.removeField( MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS );
			ifd.add( MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, keywords );
			
			os = new FileOutputStream( file );
			os = new BufferedOutputStream( os );
			new ExifRewriter().updateExifMetadataLossless( file, os, outset );
			os.close();
			os = null;
			return true;
		} catch( ImageReadException | ClassCastException | IOException | ImageWriteException e ) {
			log.error( e, e );
			return false;
		} finally { if( os != null ) try { os.close(); } catch( IOException e ) {} }
	}
}
