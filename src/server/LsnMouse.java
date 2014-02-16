package server;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.eclipse.jetty.server.Server;

public class LsnMouse implements MouseListener {
	
	Server server;
	
	public LsnMouse( Server server ) { this.server = server; }
	
	@Override
	public void mouseClicked(MouseEvent evt) {
		if ( evt.getButton() == 1 ) {
			System.out.println( "user clicked the trayicon. Exiting server" );
			try { server.stop(); }
			catch (Exception e) { e.printStackTrace(); }
		} else if ( evt.getButton() == 3 ) {
			System.out.println( "user rightclicked the trayicon." );
		}
	}

	@Override
	public void mouseEntered( MouseEvent evt ) {}

	@Override
	public void mouseExited( MouseEvent evt ) {}

	@Override
	public void mousePressed( MouseEvent evt ) {}

	@Override
	public void mouseReleased( MouseEvent evt ) {}
}
