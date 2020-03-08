/**
 * 
 */
package client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author chris
 *
 */
public class Request {
	
	
	private String requestURI = "", operation = "", userInput = "", inlineData = "", host = "", path = "";
	private String dataFile = "";
	private HashMap <String, String> headers = new HashMap<>();
	private HashMap <String, String> parameters = new HashMap<>();
	private boolean verbose = false;
	private final String [] HEADERS_LIST = {"Connection", "User-Agent", "Pragma", "Host", "Accept", "Referer", "Content-type", "Content-length"};
	private final HashSet <String> ACCEPTED_HEADERS = new HashSet<>(Arrays.asList(HEADERS_LIST));
	
	public Request ()
	{
		
	}
	
	public Request (String userInput)
	{
		this.userInput = userInput;
	}
	

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public String[] getHEADERS_LIST() {
		return HEADERS_LIST;
	}

	public HashSet<String> getACCEPTED_HEADERS() {
		return ACCEPTED_HEADERS;
	}
	
	

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public String getInlineData() {
		return inlineData;
	}

	public void setInlineData(String inlineData) {
		this.inlineData = inlineData;
	}
	

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String file) {
		this.dataFile = file;
	}
	
	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethodRequest ()
	{
		String getRequest = "";
		
		// 
		//getRequestURI()
		
		if (getOperation().equalsIgnoreCase("GET") && inlineData.equals("")&& dataFile.equals(""))
		{
			getRequest += getOperation().toUpperCase() + " " + path + " HTTP/1.0\n";
			
			if (!headers.containsKey("User-Agent"))
			{
				
				headers.put("User-Agent", "Das Browser");
			}
			
			
			for (Map.Entry<String, String> header: headers.entrySet())
			{
				getRequest += header.getKey() + ": " + header.getValue() + "\n"; 
			}
			
			getRequest +=  "Header-Count:" + (headers.size() + 1) + "\n"; 
		}
		return getRequest;
	}
	
	public String postMethodRequest ()
	{
		String postRequest = "";
		
		if (getOperation().equalsIgnoreCase("POST"))
		{
			postRequest += getOperation().toUpperCase() + " " + getPath() + " HTTP/1.0\n";
		}
		
		if (!headers.containsKey("User-Agent"))
		{
			
			headers.put("User-Agent", "Das Browser");
		}
		
		
		
		if (!dataFile.equals(""))
		{
			if (!headers.containsKey("Content-length"))
			{
				headers.put("Content-length", Integer.toString(dataFile.length()));
			}
			
			if (Integer.parseInt(headers.get("Content-length")) != dataFile.length())
			{
				headers.replace("Content-Length", headers.get("Content-length"), Integer.toString(dataFile.length()));
			}
			
			for (Map.Entry<String, String> header: headers.entrySet())
			{
				postRequest += header.getKey() + ": " + header.getValue() + "\n"; 
			}
			
			postRequest +=  "Header-Count:" + (headers.size() + 1) + "\n"; 
			
			postRequest += "\n" + dataFile;
			

		}
		
		else
		{
			if (!headers.containsKey("Content-length"))
			{
				headers.put("Content-length", Integer.toString(inlineData.length()));
			}
			
			if (Integer.parseInt(headers.get("Content-length")) != inlineData.length())
			{
				headers.replace("Content-Length", headers.get("Content-length"), Integer.toString(inlineData.length()));
			}
			
			for (Map.Entry<String, String> header: headers.entrySet())
			{
				postRequest += header.getKey() + ": " + header.getValue() + "\n"; 
			}
			
			postRequest +=  "Header-Count:" + (headers.size() + 1) + "\n"; 
			
			postRequest += "\n" + inlineData;
		}
		
		
		
		
		return postRequest;
	}

	
	public void processUserInput() throws IOException
	{
		String [] potentialRequest = userInput.split(" ");
		
		
		
		if (!potentialRequest[0].equals(null) && (potentialRequest[0].equalsIgnoreCase("GET") || potentialRequest[0].equalsIgnoreCase("POST")))
		{
			setOperation(potentialRequest[0]);
			
		}
		
		if (!potentialRequest[potentialRequest.length - 1].equals(null))
		{
			setRequestURI(potentialRequest[potentialRequest.length - 1]);
			
			
			String potentialParameters = "";
			
			if (potentialRequest[potentialRequest.length - 1].contains("?"))
			{
				potentialParameters = potentialRequest[potentialRequest.length - 1].substring(potentialRequest[potentialRequest.length - 1].lastIndexOf("?"));
				
				String [] params = potentialParameters.split("&");
				
				for (int i = 0; i < params.length; i++)
				{
					String [] couple = params[i].split("=");
					
					parameters.put(couple[0], couple[1]);
					
				}
			}
			
		}
		
		for (int i = 1; i < potentialRequest.length - 1; i++)
		{
			if (!potentialRequest[i].equalsIgnoreCase(null))
			{
				if (potentialRequest[i].equals("-v"))
				{
					if (!verbose)
					{
						setVerbose(true);
					}
					
					else
					{
						setVerbose(false);
					}
					
				}
				
				if (potentialRequest[i].equals("-h"))
				{
					String [] potentialHeaders = potentialRequest[i + 1].split(":");
					
					if (ACCEPTED_HEADERS.contains(potentialHeaders[0]))
					{
						headers.put(potentialHeaders[0], potentialHeaders[1]);
						
						i+=1;
					}
				}
				
				if (potentialRequest[i].equals("-d"))
				{
					String potentialInline = "";
					for (int j = i + 1; j < potentialRequest.length - 1; j++)
					{
						
						potentialInline += potentialRequest[j] + " ";

					}
					
					potentialInline = potentialInline.replace("'", "");
					setInlineData(potentialInline);
					i+=1;
					
				}
				
				if (potentialRequest[i].equals("-f"))
				{
					
					
					String potentialFileLocation = "";
					
					for (int j = i + 1; j < potentialRequest.length - 1; j++)
					{
						if (!potentialRequest[j].contains(requestURI))
						{
							potentialFileLocation += potentialRequest[j] + " ";
						}
					}
					
					File potentialFile = new File (potentialFileLocation);
					byte [] potentialByteFile = new byte [(int) potentialFile.length()];
					FileInputStream fis = new FileInputStream(potentialFile);
					fis.read(potentialByteFile);
					fis.close();
					String potentialProcessedFile = new String(potentialByteFile);
					
					setDataFile(potentialProcessedFile);
					
					i+=1;
					
				}
			}
		}
		
		
		
		getHostName();
	}
	
	private void getHostName ()
	{
		
		String [] potentialHostName = getRequestURI().split("/");
		
		if (getRequestURI().contains("http://") || getRequestURI().contains("https://"))
		{
			
			host =  potentialHostName[2];
			
			for (int i = 3; i < potentialHostName.length; i++)
			{
				path += "/" + potentialHostName[i];
			}
		}
		
		else
		{
			host = potentialHostName[0];
			
			for (int i = 1; i < potentialHostName.length; i++)
			{
				path += "/" + potentialHostName[i];
			}
		}
		
		if (potentialHostName.length == 1)
		{
			path += "/";
		}
		
		headers.put("Host", host);
	}

}
