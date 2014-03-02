package server;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class ServerTrayIcon {
	
	private SystemTray tray;
	private TrayIcon   icon;
	
	public ServerTrayIcon(MetaNetServer server) throws NotSupportedException {
		if ( SystemTray.isSupported() ) {
			BufferedImage img = new BufferedImage( 16, 16, BufferedImage.TYPE_3BYTE_BGR );
			Graphics2D g = img.createGraphics();
			g.setPaint ( Color.GRAY );
			g.fillRect ( 0, 0, 15, 15 );
			g.setPaint ( Color.BLACK );
			g.fillRect ( 1, 1, 13, 13 );
			g.setPaint ( Color.CYAN );
			g.fillRect ( 2, 2, 12, 12 );
			
			PopupMenu menu = new PopupMenu();
			Desktop dt = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if ( dt != null && dt.isSupported( Desktop.Action.BROWSE )) {
				MenuItem brws = new MenuItem( "Open in browser" );
				brws.addActionListener( new LsnBrowse( server ));
				menu.add( brws );
			}
			MenuItem stop = new MenuItem( "Stop server" );
			stop.addActionListener( new LsnStop( server ));
			menu.add( stop );
			
			TrayIcon icon = new TrayIcon( img );
			icon.setPopupMenu( menu );
			icon.setToolTip( "MetaNet" );
			//icon.addMouseListener( new LsnMouse( server ));
			tray = SystemTray.getSystemTray();
			try { tray.add( icon ); }
			catch ( AWTException e ) { throw new NotSupportedException(); }
		} else throw new NotSupportedException();
	}
	
	public void remove() { tray.remove(icon); }
	
	static class LsnBrowse implements ActionListener {
		
		URI uri;
		
		public LsnBrowse( MetaNetServer server ) {
			this.uri = URI.create( "http://127.0.0.1:"+ server.cnfg.port );
		}
		
		@Override
		public void actionPerformed( ActionEvent evt ) {
			try { Desktop.getDesktop().browse( uri ); }
			catch ( IOException e ) { e.printStackTrace(); }
		}
	}
	
	static class LsnStop implements ActionListener {
		
		MetaNetServer server;
		
		public LsnStop( MetaNetServer server ) { this.server = server; }
		
		@Override
		public void actionPerformed( ActionEvent evt ) {
			try { server.stop(); }
			catch ( Exception e ) { e.printStackTrace(); }
		}
	}

	@SuppressWarnings("serial")
	private class NotSupportedException extends Exception{} 
}
