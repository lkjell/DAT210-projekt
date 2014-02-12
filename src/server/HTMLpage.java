package server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTMLpage {
	
	String page;
	
	public static void main(String[] str) {
		new HTMLpage();
	}
	
	public HTMLpage() {
		// separate these later
		try {
			Path path = Paths.get(HTMLpage.class.getResource("HTMLpage.class")
				.toURI()).getParent().getParent();
			System.out.println(path); // DEBUG LOG
			path = path.resolve("index.html");
			System.out.println(path); // DEBUG LOG
			byte[] encoded = Files.readAllBytes(path);
			page = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
			System.out.println(page); // DEBUG LOG
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	static String readFile(String path) throws IOException {
		return readFile(path, StandardCharsets.UTF_8);
	}
}
