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
			Graphics2D graphics = img.createGraphics();
			graphics.setPaint ( new Color( 255, 0, 0 ) );
			graphics.fillRect ( 0, 0, 16, 16 );
			
			TrayIcon icon = new TrayIcon(img);
			icon.addMouseListener(new LsnMouse(server));
			try {
				tray.add(icon);
			}
			catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else return false;
	}
}
