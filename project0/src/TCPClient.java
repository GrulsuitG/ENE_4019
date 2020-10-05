import java.io.*;
import java.net.*;

class TCPClient {
	public static void main(String[] args) throws Exception {
		String sentence1;
		String modifiedSentence;
		
		BufferedReader inFromUser1 = new BufferedReader(new InputStreamReader(System.in));
		
		Socket clientSocket = new Socket("192.168.200.119", 1111);
		
		DataOutputStream outToserver = new DataOutputStream(clientSocket.getOutputStream());
		
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		sentence1 = inFromUser1.readLine();
		
		outToserver.writeBytes(sentence1 + '\n');
		
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		
		clientSocket.close();
	}
}
