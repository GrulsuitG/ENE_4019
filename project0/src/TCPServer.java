import java.io.*;
import java.net.*;


class TCPServer {
	public static void main(String[] args) throws Exception {
		String clientSentence1;
		String resultSentence;
		ServerSocket welcomeSocket = new ServerSocket(1234);
		
		while(true) {
			Socket connetionSocket = welcomeSocket.accept();
			
			
			BufferedReader inFromClient1 = new BufferedReader(new InputStreamReader(connetionSocket.getInputStream()));
			
			DataOutputStream outToClient = new DataOutputStream(connetionSocket.getOutputStream());
			
			clientSentence1 = inFromClient1.readLine();
			
			System.out.println("FROM CLIENT: " +clientSentence1);
			
			resultSentence = clientSentence1.toUpperCase() + '\n';
			
			outToClient.writeBytes(resultSentence);
		}	
	}
}

