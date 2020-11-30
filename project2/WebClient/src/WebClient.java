import java.io.* ; 
import java.net.* ; 
import java.util.* ;

public class WebClient {

	public static void main(String[] args) throws IOException{
		WebClient client=new WebClient();
		int num;
		Scanner sc = new Scanner(System.in);
		
		String data, s = null;
		while(true) {
			System.out.print("Select commnad(1: GET 2: POST 0: exit):");
			num = sc.nextInt();
			sc.nextLine();
			System.out.print("Insert URL : ");
			String url = sc.nextLine();
			if(num == 1) {
				s = client.getWebContentByGet(url);
				System.out.println(s);
			}
			else if(num ==2) {
				System.out.print("Insert Data : ");
				data = sc.nextLine();
				s = client.getWebContentByPost(url,data);
				System.out.println(s);
			}
			else {
				break;
			}
		}

	}

	public String getWebContentByPost(String urlString,String data) throws IOException {  
		return getWebContentByPost(urlString, data,"UTF-8", 5000);//iso-8859-1  
	}  

	public String getWebContentByGet(String urlString) throws IOException {  
		return getWebContentByGet(urlString, "iso-8859-1", 5000);  
	} 

	public String getWebContentByGet(String urlString, final String charset, int timeout) throws IOException {
		
		if(urlString == null || urlString.length() == 0) {
			return null;
		}
		
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString : ("http://" + urlString.intern());
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		
		conn.setRequestProperty("User-Agent", "2016025205/KIMBEOMJIN/WebClient/ComNet");
		
		conn.setRequestProperty("Accept", "text/html");
		conn.setConnectTimeout(timeout);
		try {
			if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		InputStream input = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		if(reader != null) {
			reader.close();
		}
		if(conn != null) {
			conn.disconnect();
		}
		return sb.toString();
	}

	public String getWebContentByPost(String urlString, String data, final String charset, int timeout) throws IOException{
		if(urlString == null || urlString.length() == 0) {
			return null;
		}
		
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString : ("http://" + urlString.intern());
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		
		connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		
		connection.setRequestProperty("user-Agent", "2016025205/KIMBEOMJIN/WebClient/ComNet");
		
		connection.setRequestProperty("Accept","text/xml");
		connection.setConnectTimeout(timeout);
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		
		byte[] content = data.getBytes("UTF-8");
		
		out.write(content);
		out.flush();
		out.close();
		
		try {
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),charset));
		String line;
		StringBuffer sb = new StringBuffer();
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		if(reader != null) {
			reader.close();
		}
		if(connection != null) {
			connection.disconnect();
		}
		
		return sb.toString();
	}
}