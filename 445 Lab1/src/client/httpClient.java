/**
 * 
 */



package client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import resources.*;

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
	static short routerPort = 3000, serverPort = 2000, incomingPort = 1000;
	static Timer timer = new Timer();
	static int timeDelay = 5000;
	
	
	
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

		
		try (DatagramSocket socket = new DatagramSocket (incomingPort, InetAddress.getLocalHost()))
		{
				
				
			String requestMessage = "";
			
			
			switch (currentRequest.getOperation().toUpperCase())
			{
			case "GET":
			{
				requestMessage = currentRequest.getMethodRequest();
				System.out.println(requestMessage);
				break;
			}
			
			case "POST":
			{
				requestMessage = currentRequest.postMethodRequest();
				System.out.println(requestMessage);
				break;
				
			}
			
			
			
		}
		
			
			//Packet Variables
			Packet incoming = new Packet ();
			Packet outgoing = new Packet();
			Packet expectedSize = new Packet (DATA_TYPE.DATA.getValue(), 0, InetAddress.getLocalHost(), serverPort, new byte [1013]);
			byte [] emptyLoad = new byte[0];
			byte [] requestLoad = requestMessage.getBytes();
			byte[] emptyPacket = Packet.packetToBytes(expectedSize);
			DatagramPacket inBound = new DatagramPacket(emptyPacket, emptyPacket.length);
			DatagramPacket outBound = new DatagramPacket(emptyPacket, emptyPacket.length);
			DatagramPacket holdingPacket = new DatagramPacket(emptyPacket, emptyPacket.length);
			
			
			//Message and window size variables. Window size and sequence numbers are determined by the server. 
			int requestSize = requestLoad.length;
			int outgoingPackets = 0;
			int windowSize = 0;
			int maxSequenceNumber = 0;
			int totalSequenceNumbers = 0;
			int windowBase = 0;
			int windowMax = 0;
			
			if (requestSize % 1013 == 0)
			{
				outgoingPackets = requestSize/1013;
			}
			
			else
			{
				outgoingPackets = (requestSize/1013) + 1;
			}
			
			
			
			
			// HTTP Handshake
			
			boolean handshake = false;
			
			while (!handshake)
			{
				//Build SYN packet and send to server. The sequence number will be the total size of the expected request.
			
				outgoing = new Packet (DATA_TYPE.SYN.getValue(), requestSize, InetAddress.getLocalHost(), serverPort, emptyLoad);
				
				outBound = Packet.buildOutgoingPacket(outgoing, InetAddress.getLocalHost(), routerPort);
				socket.send(outBound);
				
				
				//Block by calling receive method and await response.
				inBound = holdingPacket;
				socket.receive(inBound);
				incoming = Packet.buildIncomingPacket(inBound);
				
				//Set incoming variables as per server specifications
				windowSize = incoming.getSequenceNumber();
				windowMax = windowSize - 1;
				maxSequenceNumber = (windowSize *2) + 1;
				totalSequenceNumbers = maxSequenceNumber + 1;
				
				//Receive SYN_ACK. Send out ACK to server and complete handshake.
				if (incoming.getPacketType() == DATA_TYPE.SYN_ACK.getValue())
				{
					outgoing = new Packet (DATA_TYPE.ACK.getValue(), 0, InetAddress.getLocalHost(), serverPort, emptyLoad);
					outBound = Packet.buildOutgoingPacket(outgoing, InetAddress.getLocalHost(), routerPort);
					socket.send(outBound);
					
					//Handshake = true;
					System.out.println("Handshake complete");
					handshake = true;
				}
				
				
			}
			
			//requestMessage gets chopped up into a byte array will be sent out 1013 bytes at a go as the packet payload
			DatagramPacket [] readyPacks = Packet.buildPackets(requestMessage, maxSequenceNumber, InetAddress.getLocalHost(), serverPort, routerPort);
			int packetCounter = 0;
			boolean initialBurst = false;
			boolean [] sent = new boolean [] {false,false,false,false,false,false,false,false,false,false};
			boolean [] acked = new boolean [] {false,false,false,false,false,false,false,false,false,false};
			int sentPackets = 0;
			int lastAcked = 0;
			int hops = 0;
			
			//Set up the packet timers
			PacketTimer [] packetTimers = new PacketTimer [totalSequenceNumbers];
			
			for (int i = 0; i < totalSequenceNumbers; i++)
			{
				packetTimers[i] = new PacketTimer();
			}
			
			
			//Outgoing Request Logic
			while (sentPackets < outgoingPackets)
			{
				//Send an initial burst of packets to begin pipelining.
				 if (!initialBurst)
				 {
					 for (int j = windowBase; j < windowSize; j++)
					 {
						 
						 Packet.sendPacket(packetTimers[j], socket, readyPacks[packetCounter], timeDelay, timer); 
						 sent[j] = true;
						 packetCounter ++;
						 
						 if (packetCounter == readyPacks.length)
						 {
							 break;
						 }
					 }
					 
					 initialBurst = true;
					 
				 }
				 
				 
				 //Wait for an ack from the first window. The timer will handle retransmissions 
				 incoming = Packet.receivePacket(inBound, holdingPacket, socket);
				 lastAcked = incoming.getSequenceNumber();
				 
				 if (!acked[lastAcked])
				 {
					 acked [lastAcked] = true;
					 packetTimers[lastAcked].cancel();
					 sentPackets++;
					 
					 if (sentPackets == outgoingPackets)
					 {
						 break;
					 }
				 }
				 
				 //Check if you should shift the window, and by how much
				 if (lastAcked == windowBase)
				 {
					 hops = 0;
					 
					 for (int i = 0; i < windowSize; i++)
					 {
						 if (acked[(i + windowBase) % totalSequenceNumbers])
						 {
							 hops ++;
							 acked[(i + windowBase) % totalSequenceNumbers] = false;
							 sent[(i + windowBase) % totalSequenceNumbers] = false;
						 }
						 else
						 {
							 break;
						 }
					 }
					 
					 //If there are any moves to be made, the window will be shifted here.
					 if (hops != 0)
					 {
						 windowBase = (windowBase + hops) % totalSequenceNumbers; 
						 windowMax = (windowMax + hops) % totalSequenceNumbers;
						 
						//Send out any new packets if the window has shifted
						 
						 for (int i = 0; i < windowSize; i++)
						 {
							 if (!sent[(i + windowBase) % totalSequenceNumbers])
							 {
								 Packet.sendPacket(packetTimers[(i + windowBase) % totalSequenceNumbers], socket, readyPacks[packetCounter], timeDelay, timer);
								 sent[(i + windowBase) % totalSequenceNumbers] = true;
								 packetCounter ++;
							 }
						 }
					 }
					 	 
				 }
				 
				 
			}
			
			 
			
		/*	System.out.println("\nAwaiting response......\n");
			
			String rawResponse = "";
			String responseBit = "";
			
			while ((responseBit = serverIn.readLine())!=null)
			{
				rawResponse += responseBit + "\n";
			} 
			
			
			Response currentResponse = new Response (rawResponse, currentRequest.isVerbose());
			currentResponse.generateResponse();
			
			currentResponse.printResponse();  */
			
			
		
			
			//Message window and buffer variables
			int incomingPackets = 0;
			windowBase = 0;
			windowMax = windowSize - 1;
			
			
			inBound = holdingPacket;
			socket.receive(inBound);
			incoming = Packet.buildIncomingPacket(inBound);
			
			
			if (incoming.getPacketType() == DATA_TYPE.SYN.getValue())
			{
				incomingPackets = incoming.getSequenceNumber();
				
				outgoing = new Packet (DATA_TYPE.SYN_ACK.getValue(), 0, InetAddress.getLocalHost(), serverPort, emptyLoad);
				outBound = Packet.buildOutgoingPacket(outgoing, InetAddress.getLocalHost(), routerPort);
				socket.send(outBound);
			}
			
			
			//Variables for receiving packets
			Packet ackPacket = new Packet (DATA_TYPE.ACK.getValue(), 0, InetAddress.getLocalHost(), serverPort, emptyLoad);
			Packet [] expectedPayload = new Packet [incomingPackets];
			Packet [] lowerBuffer = new Packet [5];
			Packet [] upperBuffer = new Packet [5];
			boolean lowerFlushed = false;
			boolean upperFlushed = false;
			boolean received = false;
			int packetsReceived = 0;
			int displacement = 0;
			
			
			//Incoming response logic
			//This simulates the buffer and window receiving the payload packets from the client.
					while (packetsReceived < incomingPackets )
				{
					
					//Receive a packet and build it ready for buffering
					received = false;
					
					inBound = holdingPacket;
					socket.receive(inBound);
					incoming = Packet.buildIncomingPacket(inBound);
					int position =  incoming.getSequenceNumber();
					
					//Determine where to put the packet in the buffer
					switch (position)
					{
					case 0: case 1: case 2: case 3: case 4:
					{
						if (lowerBuffer[position] == null)
						{
							lowerBuffer[position] = incoming;
						}
						
						else
						{
							received = true;
						}
						
						break;
					}
					
					case 5: case 6: case 7: case 8: case 9:
					{
						
						if (upperBuffer[position - 5] == null)
						{
							upperBuffer[position - 5] = incoming;
						}
						
						else
						{
							received = true;
						}
						break;
					}
					
					}
					
					//Send ack for received packet and update counter
					ackPacket.setSequenceNumber(position);
					outgoing = ackPacket;
					outBound = Packet.buildOutgoingPacket(ackPacket, InetAddress.getLocalHost(), routerPort);
					socket.send(outBound);

					//Move the window if a new packet came in					
					if (!received)
					{
						packetsReceived += 1;
						windowBase = (windowBase + 1) % totalSequenceNumbers; 
						windowMax = (windowMax + 1) % totalSequenceNumbers;
					}
					
					
					//Flush lower buffer
					if ((windowBase == 5 || packetsReceived == incomingPackets) && lowerBuffer[0] !=null)
					{
						for (int k = 0; ;k++ )
						{
							
							if (expectedPayload[(displacement * totalSequenceNumbers) + k] == null && lowerBuffer[k] != null)
							{
								expectedPayload[(displacement * totalSequenceNumbers) + k] = lowerBuffer[k];
								
							/*	if (packetsReceived == incomingPackets)
								{
									break;
								} */
								lowerBuffer[k] = null;
								
							}
							
							if (k == lowerBuffer.length || k+1 == expectedPayload.length)
							{
								lowerFlushed = true;
								break;
							}
						}
					}
					
					//Flush upper buffer
					if ((windowMax == 0 || packetsReceived == incomingPackets) && upperBuffer[0] != null)
					{
						for (int k = 0; ;k++ )
						{
							
							if (expectedPayload[(displacement * totalSequenceNumbers) + k + 5] == null && upperBuffer[k] != null)
							{
								expectedPayload[(displacement * totalSequenceNumbers) + k + 5] = upperBuffer[k];
								
						
								
								lowerBuffer[k] = null;
								
							}
							
							if (k == upperBuffer.length  || k + 1 == expectedPayload.length)
							{
								upperFlushed = true;
								break;
							}
						}
					}
					
					
					//Sets the buffers to unflushed so they can be reused
					if (upperFlushed && lowerFlushed)
					{
						displacement++;
						upperFlushed = false;
						lowerFlushed = false;
					}
					
				}
					
					String rawResponse = Packet.buildPayload(expectedPayload);
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

	
