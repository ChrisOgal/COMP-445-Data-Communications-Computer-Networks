/**
 * 
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author chris
 *
 */
public class FileServer {

	/**
	 * @param args
	 */
	static boolean exitServer = false;
	static boolean closeSocket = false;
	static Scanner userInput = new Scanner (System.in);
	static String command = "", directory = "G:\\workspace\\445 Lab1\\resources";
	static int port = 8080;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println("httpfs Welcome to Das Server" 
				+ "\nhttpfs is a simple file server."
				+ "\nhttpfs Input -v for help.");
		
		while (!exitServer)
		{
			closeSocket = false;
			System.out.println("\nhttpfs ");
			command = userInput.nextLine();
			
			String [] segmentedCommand = command.split(" ");
			
			if (command.contains("-v"))
			{
				debuggingMessages();
			}
			
			else
			{
				if (command.contains("-p"))
				{
					for (int i = 0; i < segmentedCommand.length; i++)
					{
						if (segmentedCommand[i].equalsIgnoreCase("-p"))
						{
							port = Math.abs(Integer.parseInt(segmentedCommand[i+1]));
							System.out.println("Port changed to " + port);
							break;
						}
					}
				}
				
				if (command.contains("-d"))
				{
					for (int i = 0; i < segmentedCommand.length; i++)
					{
						if (segmentedCommand[i].equalsIgnoreCase("-d"))
						{
							directory = segmentedCommand[i+1];
							System.out.println("Directory changed to " + directory);
							break;
						}
					}
				}
				
				if (command.equalsIgnoreCase("exit"))
				{
					exitServer = true;
				}
				
				
				boolean hasRun = false;
				while (!closeSocket)
				{
					if (!hasRun)
					{
						System.out.println("Server is running...");
						hasRun = true;
					}
					
					switch (command.toLowerCase())
					{
					case "exit": 
					{
						closeSocket = true;
						exitServer = true;
						break;
					}
					case "close":
					{
						closeSocket = true;
						break;
					}
					
					default:
					{
						//Inputs the request from the client and sends out the response
						handleRequest(command);
						
					}
					}
				}
			}
			
			
					
		}
		

	}
	
	public static void debuggingMessages()
	{
		System.out.println("\nusage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]\n"
				+ "\n-v Prints debugging messages."
				+ "\n-p Specifies the port number that the server will listen and serve at. Default is 8080."
				+ "\n-d Specifies the directory that the server will use to read/write"
				+ " requested files. Default is the current directory when launching the application."
				+ "\n\nIf you wish to use the default settings press the enter key");
	}
	
	public static void handleRequest(String command) throws UnknownHostException, IOException
	{
		
		try (ServerSocket serverSocket = new ServerSocket(port); //Creates the server side Socket that will listen for incoming clients on the given port
				Socket clientSocket = serverSocket.accept(); //When an incoming client makes a connection, a new TCP "thread" is opened to deal with this connection
				PrintWriter serverOut = new PrintWriter (clientSocket.getOutputStream(), true); //Sends data out back to the client
				BufferedReader clientIn = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));) // Receives data in from the client
				
		{
			
			String rawRequest = "";
			String requestBit = "";
			
			while ((requestBit = clientIn.readLine())!=null)
			{
				rawRequest += requestBit + "\n";	
				
				if (!clientIn.ready())
				{
					System.out.println("\nRequest Received");
					break;		//Exits reading of there is no data readily available to be read instead of blocking
				}
			}
			
			Response currentResponse = new Response (rawRequest, directory);
			currentResponse.parseRequest();
			serverOut.println(currentResponse.generateResponse());
			System.out.println("\nRequest Completed");
		}
				
	}

}
