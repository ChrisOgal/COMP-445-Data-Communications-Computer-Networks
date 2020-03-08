/**
 * 
 */

package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeFormatter;
/**
 * @author chris
 *
 */
public class Response {

	String incomingRequest = "", operation = "", requestedFile = "", dataFile = "", expectedResponse = "", responseFile = "", filePath = "",
		   statusLine = "", statusPhrase = "" ;
	
	final String PROTOCOL = "HTTP/1.CHRIS";
	HashMap <String, String> headers = new HashMap <>();
	HashMap<String, String> responseHeaders = new HashMap<>();
	int statusCode = 0;


	public Response ()
	{

	}

	public Response (String incomingRequest, String filePath)
	{
		this.incomingRequest = incomingRequest;
		this.filePath = filePath;
	}

	public String getIncomingRequest() {
		return incomingRequest;
	}

	public void setIncomingRequest(String incomingRequest) {
		this.incomingRequest = incomingRequest;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getRequestedFile() {
		return requestedFile;
	}

	public void setRequestedFile(String requestedFile) {
		this.requestedFile = requestedFile;
	}


	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public String getExpectedResponse() {
		return expectedResponse;
	}

	public void setExpectedResponse(String expectedResponse) {
		this.expectedResponse = expectedResponse;
	}

	public String getResponseFile() {
		return responseFile;
	}

	public void setResponseFile(String responseFile) {
		this.responseFile = responseFile;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusLine() {
		return statusLine;
	}

	public void setStatusLine(String statusLine) {
		this.statusLine = statusLine;
	}

	public String getStatusPhrase() {
		return statusPhrase;
	}

	public void setStatusPhrase(String statusPhrase) {
		this.statusPhrase = statusPhrase;
	}

	public HashMap<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(HashMap<String, String> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public String getPROTOCOL() {
		return PROTOCOL;
	}

	public void parseRequest ()
	{
		String [] potentialRequest = incomingRequest.split("\\n");

		String [] identifier = potentialRequest[0].split(" ");

		setOperation(identifier[0]);

		setRequestedFile(identifier[1]);
		int headerCount = 0;
		
		for (int i = 1; i < potentialRequest.length; i++)
		{
			if (potentialRequest[i].contains("Header-Count"))
			{
				String [] count = potentialRequest[i].split(":");
				headerCount = Integer.parseInt(count[1]);
				
			}
		}
		
		if (identifier[0].equalsIgnoreCase("GET"))
		{
			for (int i = 1; i < headerCount; i++)
			{
				String [] potentialHeader = potentialRequest[i].split(":");

				headers.put(potentialHeader[0], potentialHeader[1]);
			}
		}

		
		

		if (identifier[0].equalsIgnoreCase("POST"))
		{
			for (int i = 1; i < headerCount; i++)
			{
				
					String [] potentialHeader = potentialRequest[i].split(":");
					headers.put(potentialHeader[0], potentialHeader[1]);
				
				
			}
			
			for (int i = headerCount + 2; i < potentialRequest.length; i++)
			{
				dataFile += potentialRequest[i] + "\n";
			}
			
		}
		
		
	}
	
	public void processGet () throws IOException
	{
		
		File folder = new File(filePath);
		File[] listOfFiles = folder.listFiles();
		
		setRequestedFile(requestedFile.replace('/', '\\'));
		
		if (requestedFile.equals("\\"))
		{
			for (File file : listOfFiles)
			{
				if (file.isFile())
				{
					responseFile += file.getName() + "\n";
				}
			}
			
			statusCode = 200;
		}
		
		else
		{
			statusCode = 404;
			for (File file: listOfFiles)
			{
				if (file.getName().equals(requestedFile.replace("\\", "")))
				{
					byte [] potentialByteFile = new byte [(int) file.length()];
					FileInputStream fis = new FileInputStream(file);
					fis.read(potentialByteFile);
					fis.close();
					responseFile = new String(potentialByteFile);
					statusCode = 200;
					break;
				}
			}
		}
		
	}
	
	public void processPost () throws FileNotFoundException
	{
		
		FileOutputStream out = new FileOutputStream (filePath + requestedFile);
		PrintWriter write = new PrintWriter(out, true);
		
		write.println(dataFile);
		write.close();
		
		statusCode = 201;
	}
	
	public String generateResponse () throws IOException
	{
		
		switch (getOperation().toUpperCase())
		{
		case "GET":
		{
			processGet();
			break;
		}
		case "POST":
		{
			processPost();
			break;
		}
		}
		switch (statusCode)
		{
		case 200:
		{
			statusPhrase = "OK";
			break;
		}
			
		case 201:
		{
			statusPhrase = "Created";
			 responseHeaders.put("Content-Length", Integer.toString(dataFile.length()));
			 break;
		}
			
		case 404:
		{
			statusPhrase = "Not Found";
			break;
		}
		
		}
		
		statusLine = PROTOCOL + " " + statusCode + " " + statusPhrase;
		
		 LocalDateTime rawResponseTime = LocalDateTime.now();
		 DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		 String responseTime = rawResponseTime.format(format);
		 
		 responseHeaders.put("Server", "Chris File Server");
		 responseHeaders.put("Date", responseTime);
		 if (!responseFile.equals(""))
		 {
			 responseHeaders.put("Content-Length", Integer.toString(responseFile.length()));
		 }
		 responseHeaders.put("Connection", "Close");
		 
		 expectedResponse += statusLine + "\n";
		 
		 for (Map.Entry<String, String> header: responseHeaders.entrySet())
		 {
			 expectedResponse += header.getKey() + ":" + header.getValue() + "\n";
		 }
		 
		 expectedResponse += "\n" + responseFile;
		 
		 return expectedResponse;
		
	}
	

}
