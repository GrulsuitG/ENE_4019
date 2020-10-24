package server;

import java.io.*;
import java.net.*;
import java.util.*;




enum StatusCode{
	OK, BAD_REQUEST, FORBIDDEN, NOT_FOUND, HTTP_VERSION_NOT_SUPPORTED, INTERNAL_SERVER_ERROR
}

public final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	final static String HTTP_VERSION = "1.1";
	final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	
	final static int BUFFER_IN_SIZE = 2048;
	final static int BUFFER_OUT_SIZE =2048;
	final static Properties CONTENT_TYPES = new Properties();
	final static EnumMap<StatusCode, String> SCODES = new EnumMap<StatusCode, String>(StatusCode.class);
	
	static {
		CONTENT_TYPES.setProperty("html", "text/html");
		CONTENT_TYPES.setProperty("jpg", "image/jpeg");
		
		SCODES.put(StatusCode.OK, "200");
		SCODES.put(StatusCode.BAD_REQUEST, "400");
		SCODES.put(StatusCode.FORBIDDEN, "403");
		SCODES.put(StatusCode.NOT_FOUND, "404");
		
		SCODES.put(StatusCode.HTTP_VERSION_NOT_SUPPORTED, "505");
	}
	StatusCode code;
	Socket socket;
	File requestedFile;
	
	public HttpRequest(Socket socket) throws Exception{
		this.socket = socket;
		this.code = null;
		this.requestedFile = null;
	}
	
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processRequest() throws Exception{
		InputStream is = null;
		DataOutputStream os = null;
		BufferedReader br = null;
		FileInputStream fis = null;

		try {
			is = socket.getInputStream();
			os = new DataOutputStream(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(is), BUFFER_IN_SIZE);
		
			String requestLine = br.readLine();
			String errorMsg = parseRequestLine(requestLine);
			
			String headerLine = null;
			while((headerLine = br.readLine()).length() != 0)
				System.out.println(headerLine);
			
			if(errorMsg == null) {
				try {
					fis =new FileInputStream(requestedFile);
				} catch (FileNotFoundException e){
					e.printStackTrace();
					code = StatusCode.NOT_FOUND;
				}
			} else {
				System.out.println();
				System.out.println(errorMsg);
			}
			sendResponseMessage(fis,os);
		}finally {
			if(os != null)
				os.close();
			if(br != null)
				br.close();
			if(fis != null)
				fis.close();
			socket.close();
		} 
	}
		
	private String parseRequestLine(String requestLine) {
		System.out.println();
		System.out.println("Received HTTP request:");
		System.out.println(requestLine);
		StringTokenizer tokens = new StringTokenizer(requestLine);
		if(tokens.countTokens() != 3) {
			code = StatusCode.BAD_REQUEST;
			return "Requested line is malformed.";
		}
		String method = tokens.nextToken().toUpperCase();
		String fileName = tokens.nextToken();
		fileName = "." + fileName;
		File file = new File(fileName);
		
		if(!file.exists()) {
			code = StatusCode.NOT_FOUND;
			return "Requested file " + fileName + " does not exist.";
		}
		if(!file.canRead()) {
			code = StatusCode.FORBIDDEN;
			return "Requested file " + fileName + " is not readable.";
		}
		if (file.isDirectory()) {
			File[] list = file.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String f) {
					if (f.equalsIgnoreCase("index.html"))
						return true;
					return false;
				}
			});
			if(list == null || list.length == 0 ){
				code = StatusCode.NOT_FOUND;
				return "No index file found at requested location " + fileName;
			}
			else if (list.length != 1) {
				code =StatusCode.INTERNAL_SERVER_ERROR;
				return "Found more than one index file at requested location " + fileName;
			}
			file = list[0];
		}
		
		requestedFile = file;
		
		String version = tokens.nextToken().toUpperCase();
		if(version.equals("HTTP/1.0")) {
			code = StatusCode.BAD_REQUEST;
			return "The HTTP version is not supported version"; 
		}
		if(!version.matches("HTTP/([1-9][0-9.]*)")) {
			code = StatusCode.BAD_REQUEST;
			return "HTTP version string is malformed.";
		}
		if(!version.equals("HTTP/1.1")) {
			code = StatusCode.HTTP_VERSION_NOT_SUPPORTED;
			return version +" not supproted.";
		}
		code = StatusCode.OK;
		return null;
	}
	
	private void sendResponseMessage(FileInputStream fis, DataOutputStream os) throws Exception{
		
		String statusLine = "HTTP/" +HTTP_VERSION + " " + SCODES.get(code)+ " ";
		String entityBody = "<HTML>" + CRLF + "<HEAD><TITLE>?</TITLE></HEAD>"+ CRLF
							+ "<BODY>?</BODY>" +  CRLF+"</HTML>";
		String message;
		switch(code) {
		case OK:
			message = "OK";
			break;
		case BAD_REQUEST:
			message = "Bad Request";
			break;
		case FORBIDDEN:
			message = "Forbidden";
			break;
		case NOT_FOUND:
			message = "Not Found";
			break;
		case HTTP_VERSION_NOT_SUPPORTED:
			message = "HTTP Version Not Supproted";
			break;
		default:
			message = "error code";
		}
		
		statusLine  += message;
		if( code != StatusCode.OK)
			entityBody = entityBody.replaceAll("\\?", message + " - sent by WebServer");
		
		System.out.println("statusLine: "+ statusLine);
		System.out.println("entityBody: "+entityBody);
		
		os.writeBytes(statusLine + CRLF);
		sendHeaderLines(os);
		os.writeBytes(CRLF);
		if(code == StatusCode.OK) {
			System.out.println("Sending requested file to client...");
			sendBytes(fis, os);
		} else {
			System.out.println("Sending error message to client...");
			os.writeBytes(entityBody);
		}
	}
	
	private void sendHeaderLines(DataOutputStream os) throws Exception{
		StringBuffer headerLines = new StringBuffer();
		
		String contentTypeLine = "Content-Type: ";
		String contentLength = "Content-Length: ";
		System.out.println("code "+ code);
		
		switch(code) {
		case OK:
			contentTypeLine += contentType(requestedFile.getName()) + CRLF;
			contentTypeLine += contentLength + "1024" + CRLF;
			
			
		default:
			contentTypeLine += DEFAULT_CONTENT_TYPE + CRLF;
		}
		
		headerLines.append(contentTypeLine);
		os.writeBytes(headerLines.toString());
		
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception{
		byte[] buffer = new byte[BUFFER_OUT_SIZE];
		int bytes = 0;
		
		while((bytes = fis.read(buffer)) != -1) {
			os.write(buffer,0,bytes);
		}
	}
	
	private static String contentType(String fileName) {
		String fname = fileName.toLowerCase();
		int lastdot = fname.lastIndexOf(".");
		if((lastdot != -1) && (lastdot != fname.length() -1)) {
			System.out.println("type : " + CONTENT_TYPES.getProperty(fname.substring(lastdot +1)));
			return CONTENT_TYPES.getProperty(fname.substring(lastdot+1), DEFAULT_CONTENT_TYPE);
		}
		return DEFAULT_CONTENT_TYPE;
	}
}
