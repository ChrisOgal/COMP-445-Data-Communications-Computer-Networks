/**
 * 
 */
package client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chris
 *
 */
public class Response {
	
	private String statusLine = "", protocol = "", statusCode = "", statusPhrase = "", requestedData = "", incomingData = "";
	private HashMap <String, String> headers = new HashMap<>();
	private boolean verbose = false;
	
	
	
	public Response ()
	{
		
	}
	
	public Response(String incomingData, boolean verbose) 
	{
		this.incomingData = incomingData;
		this.verbose = verbose;
		
	}

	public String getStatusLine() {
		return statusLine;
	}

	public void setStatusLine(String statusLine) {
		this.statusLine = statusLine;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusPhrase() {
		return statusPhrase;
	}

	public void setStatusPhrase(String statusPhrase) {
		this.statusPhrase = statusPhrase;
	}

	public String getRequestedData() {
		return requestedData;
	}

	public void setRequestedData(String requestedData) {
		this.requestedData = requestedData;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public String getIncomingData() {
		return incomingData;
	}

	public void setIncomingData(String incomingData) {
		this.incomingData = incomingData;
	}
	
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void generateResponse ()
	{
		String [] potentialResponse = getIncomingData().split("\n");
		
		String [] potentialStatusLine = potentialResponse[0].split(" ");
		
		setProtocol(potentialStatusLine[0]);
		
		setStatusCode(potentialStatusLine[1]);
		
		String potentialStatusPhrase = "";
		
		for (int i = 2; i < potentialStatusLine.length; i++)
		{
			potentialStatusPhrase += potentialStatusLine[i] + " ";
		}
		
		setStatusPhrase(potentialStatusPhrase);
		
		statusLine = getProtocol() + " " + getStatusCode() + " " + getStatusPhrase();
		
		int headerNum = 0;
		for (int i = 1; i < potentialResponse.length; i++)
		{
			if (potentialResponse[i].contains(":") && !potentialResponse[i].contains("\""))
			{
				String [] potentialHeaders = potentialResponse[i].split(":", 2);
				
				headers.put(potentialHeaders[0], potentialHeaders[1]);
				
				headerNum++;
						
			}
		}
		
		String rawData = "";
		for (int i = headerNum + 1; i < potentialResponse.length; i++)
		{
			rawData += potentialResponse[i] + "\n";
		}
		
		setRequestedData(rawData);
	}
	
	public void printResponse ()
	{
		System.out.println(getStatusLine());
		if (isVerbose())
		{
			for (Map.Entry<String, String> header: getHeaders().entrySet())
			{
				
				System.out.println(header.getKey() + ":" + header.getValue());
				
			}
		}
		
		System.out.println(requestedData);
	}
	
	
	
	

}
