package server;
import java.io.*;
import java.net.*;

public final class WebServer {
	public static void main(String[] args) throws Exception {
		try {
			ServerSocket serversocket = new ServerSocket(1234);
			
			while(true) {
				Socket socket =serversocket.accept();
				
				HttpRequest request = new HttpRequest(socket);
				
				Thread thread = new Thread(request);
				
				thread.start();
				
			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
}
