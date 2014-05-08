package server;

import java.io.FileReader;

import org.ini4j.Ini;

public class Config {
	
	private Ini ini;
	private Ini.Section section;
	
	public int port;
	public String webDir;
	public String webIndex;
	
	public Config( String filename ) {
		ini = new Ini();
        try {
        	ini.load( new FileReader( filename ));
        	section = ini.get( "Main" );
		} catch ( Exception e ) {}
        
        port     = getInt( "port", MetaNetServer.DEFAULT_PORT );
        webDir   = getStr( "webDir", "./web" );
        webIndex = getStr( "webIndex", "index.html" );
	}

	private String getStr( String key, String defaultValue ) {
        try { return section.get( key ); }
        catch ( Exception e ) {
        	//TODO: Log error - cant find config.ini key <key>
        	System.err.println( "Can't find config.ini key "+ key +" default value: "+ defaultValue );
        	return defaultValue;
        }
	}
	
	private int getInt( String key, int defaultValue ) {
        try { return Integer.parseInt( section.get( key )); }
        catch ( Exception e ) {
        	//TODO: Log error - cant find config.ini key <key>
        	System.err.println( "Can't find config.ini key "+ key +" default value: "+ defaultValue );
        	return defaultValue;
        }
	}
	
	//private void setSection( String name ) { section = ini.get( name ); }
}
