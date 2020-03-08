/**
 * 
 */



package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author chris
 *
 */
public class httpClient {

	/**
	 * @param args
	 */
	
	static Scanner userInput = new Scanner (System.in);
	static boolean exit = false;
	static boolean invalidInput;
	static String command = "";
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		

		System.out.println("httpc Welcome to Das Browser.");

		while (!exit)
		{
			
			System.out.print("\nhttpc ");
			command = userInput.nextLine();

			switch(command)
			{

			case "help":
			{
				help();
				break;
			}

			case "help get":
			{
				helpGet();
				break;
			}

			case "help post":
			{
				helpPost();
				break;
			}
			
			case "exit":
			{
				exit = true;
				break;
			}

			default:
			{
				//Generate Request
				handleRequest(command);
				//Output Response
			}
			
			
			}
		}



	}


	public static void help()
	{
		System.out.println("\nhttpc is a curl-like application but supports HTTP protocol only.\n" + 
				"Usage:\n" + 
				" httpc command [arguments]\n" + 
				"The commands are:\n" + 
				" get executes a HTTP GET request and prints the response.\n" + 
				" post executes a HTTP POST request and prints the response.\n" + 
				" help prints this screen.\n" + 
				"Use \"httpc help [command]\" for more information about a command.\n");
	}

	public static void helpGet()
	{
		System.out.println("\nUsage: httpc get [-v] [-h key:value] URL\n" + 
				"Get executes a HTTP GET request for a given URL.\n" + 
				" -v Prints the detail of the response such as protocol, status,\n" + 
				"and headers.\n" + 
				"-h key:value Associates headers to HTTP Request with the format\n" + 
				"'key:value'.\n");
	}

	public static void helpPost()
	{
		System.out.println("\nUsage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n" + 
				"Post executes a HTTP POST request for a given URL with inline data or from\n" + 
				"file.\n" + 
				"-v Prints the detail of the response such as protocol, status,\n" + 
				"and headers.\n" + 
				"-h key:value Associates headers to HTTP Request with the format\n" + 
				"'key:value'.\n" + 
				"-d string Associates an inline data to the body HTTP POST request.\n" + 
				"-f file Associates the content of a file to the body HTTP POST\n" + 
				"request.\n" + 
				"Either [-d] or [-f] can be used but not both.\n");
	}
	
	public static void handleRequest(String userRequest) throws IOException
	{
		Request currentRequest = new Request (userRequest);
		
		currentRequest.processUserInput();
		
		//System.out.println(currentRequest.getHost());
		
		try (Socket socket = new Socket (currentRequest.getHost(), 8080);
				
				PrintWriter clientOut = new PrintWriter (socket.getOutputStream(), true);
				BufferedReader serverIn = new BufferedReader (new InputStreamReader(socket.getInputStream())))
			
		{
			
			switch (currentRequest.getOperation().toUpperCase())
			{
			case "GET":
			{
				clientOut.println(currentRequest.getMethodRequest());
				System.out.println("\n" + currentRequest.getMethodRequest());
				break;
			}
			
			case "POST":
			{
				clientOut.println(currentRequest.postMethodRequest());
				System.out.println("\n" + currentRequest.postMethodRequest());
				break;
				
			}
			
			
		}
			
			System.out.println("\nAwaiting response......\n");
			
			String rawResponse = "";
			String responseBit = "";
			
			while ((responseBit = serverIn.readLine())!=null)
			{
				rawResponse += responseBit + "\n";
			}
			
			
			Response currentResponse = new Response (rawResponse, currentRequest.isVerbose());
			currentResponse.generateResponse();
			
			currentResponse.printResponse();

			
		}
		
		
		catch (UnknownHostException e)
		{
			System.err.println("error");
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		
	}
	
	

}
