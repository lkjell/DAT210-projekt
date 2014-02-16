package server;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import org.eclipse.jetty.server.Server;

public class ServerTrayIcon {
	
	public static boolean make(Server server) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			
			BufferedImage img = new BufferedImage(16,16,BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = img.createGraphics();
			g.setPaint ( Color.GRAY );
			g.fillRect ( 0, 0, 15, 15 );
			g.setPaint ( Color.BLACK );
			g.fillRect ( 1, 1, 13, 13 );
			g.setPaint ( Color.CYAN );
			g.fillRect ( 2, 2, 12, 12 );
			
			TrayIcon icon = new TrayIcon(img);
			icon.addMouseListener(new LsnMouse(server));
			try { tray.add(icon); }
			catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else return false;
	}
}
