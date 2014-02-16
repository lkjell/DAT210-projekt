package server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class Config {
	
	Ini ini;
	Ini.Section section;
	
	public Config( String filename ) {
		ini = new Ini();
        try {
        	ini.load( new FileReader( filename ));
        	section = ini.get( "Main" );
		} catch ( InvalidFileFormatException | FileNotFoundException e ) {
			e.printStackTrace();
		}
        catch ( IOException e ) {}
	}
	
	public int getInt( String key ) {
		return Integer.parseInt( section.get( key ));
	}
	
	public void setSection( String name ) {
		section = ini.get( name );
	}
	
	public int getPort() { return getInt( "port" ); }
	public String getWebIndex() { return section.get( "webindex" ); }
}
