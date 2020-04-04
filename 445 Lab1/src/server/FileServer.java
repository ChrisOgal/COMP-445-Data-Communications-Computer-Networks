/**
 * 
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;

import resources.*;

/*
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
	static short incomingPort = 2000, clientPort = 1000, routerPort = 3000;
	static int sequenceNumbers = 10, maxSequenceNumber = sequenceNumbers - 1, windowSize = (sequenceNumbers/2) - 1, defaultWindowBase = 0, defaultWindowMax = windowSize - 1;
	static long timeDelay = 5000;
	static Timer timer = new Timer();
	
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
							incomingPort = (short) Math.abs(Integer.parseInt(segmentedCommand[i+1]));
							System.out.println("Port changed to " + incomingPort);
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
				+ "\n-p Specifies the port number that the server will listen and serve at. Default is 2000."
				+ "\n-d Specifies the directory that the server will use to read/write"
				+ " requested files. Default is the current directory when launching the application."
				+ "\n\nIf you wish to use the default settings press the enter key");
	}
	
	
	
	public static void handleRequest(String command) throws UnknownHostException, IOException
	{
		
		try (DatagramSocket serverSocket = new DatagramSocket(incomingPort, InetAddress.getLocalHost())) //Creates the server side Socket that will listen for incoming clients on the given port
							
		{
			//Request Packet Variables
			Packet incoming = new Packet ();
			Packet outgoing = new Packet();
			Packet expectedSize = new Packet (DATA_TYPE.DATA.getValue(), 0, InetAddress.getLocalHost(), clientPort, new byte [1013]);
			byte [] emptyLoad = new byte[0];
			byte[] emptyPacket = Packet.packetToBytes(expectedSize);
			DatagramPacket inBound = new DatagramPacket(emptyPacket, emptyPacket.length);
			DatagramPacket outBound = new DatagramPacket(emptyPacket, emptyPacket.length);
			DatagramPacket holdingPacket = new DatagramPacket(emptyPacket, emptyPacket.length);
			
			//Message window and buffer variables
			int requestSize = 0;
			int incomingPackets = 0;
			int windowBase = defaultWindowBase;
			int windowMax = defaultWindowMax;
			
			
			boolean handshake = false;
			
			
			while (!handshake)
			{
				
				//Block by calling receive method and await request.
				serverSocket.receive(inBound);
				incoming = Packet.buildIncomingPacket(inBound);
				
				
				//Receive SYN. Send out SYN_ACK to client and await ACK to complete handshake.  Sequence number will be an acknowledgment of the request size.
				if (incoming.getPacketType() == DATA_TYPE.SYN.getValue())
				{
					clientPort = incoming.getPeerPort();
					requestSize = incoming.getSequenceNumber();
					
					if (requestSize % 1013 == 0)
					{
						incomingPackets = requestSize/1013;
					}
					
					else
					{
						incomingPackets = (requestSize/1013) + 1;
					}
					
					outgoing = new Packet (DATA_TYPE.SYN_ACK.getValue(), windowSize, InetAddress.getLocalHost(), clientPort, emptyLoad);
					outBound = Packet.buildOutgoingPacket(outgoing,InetAddress.getLocalHost(), routerPort);
					serverSocket.send(outBound);
					
				}
				
				//Receive ACK and complete handshake. Next act will be to wait for a request
				inBound = holdingPacket;
				serverSocket.receive(inBound);
				incoming = Packet.buildIncomingPacket(inBound);
				
				if (incoming.getPacketType() == DATA_TYPE.ACK.getValue())
				{
					System.out.println("Handshake complete");
					handshake = true;
					
				}
					
				
			}
			
			//Variables for receiving packets
			Packet ackPacket = new Packet (DATA_TYPE.ACK.getValue(), 0, InetAddress.getLocalHost(), clientPort, emptyLoad);
			Packet [] expectedPayload = new Packet [incomingPackets];
			Packet [] lowerBuffer = new Packet [5];
			Packet [] upperBuffer = new Packet [5];
			boolean lowerFlushed = false;
			boolean upperFlushed = false;
			boolean received = false;
			int packetsReceived = 0;
			int displacement = 0;
			
			
			//Incoming request logic
			//This simulates the buffer and window receiving the payload packets from the client.
					while (packetsReceived < incomingPackets )
				{
					
					//Receive a packet and build it ready for buffering
					received = false;
					
					inBound = holdingPacket;
					serverSocket.receive(inBound);
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
					serverSocket.send(outBound);

					//Move the window if a new packet came in					
					if (!received)
					{
						packetsReceived += 1;
						windowBase = (windowBase + 1) % sequenceNumbers; 
						windowMax = (windowMax + 1) % sequenceNumbers;
					}
					
					
					//Flush lower buffer
					if ((windowBase == 5 || packetsReceived == incomingPackets) && lowerBuffer[0] !=null)
					{
						for (int k = 0; ;k++ )
						{
							
							if (expectedPayload[(displacement * sequenceNumbers) + k] == null && lowerBuffer[k] != null)
							{
								expectedPayload[(displacement * sequenceNumbers) + k] = lowerBuffer[k];
								
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
							
							if (expectedPayload[(displacement * sequenceNumbers) + k + 5] == null && upperBuffer[k] != null)
							{
								expectedPayload[(displacement * sequenceNumbers) + k + 5] = upperBuffer[k];
								
						
								
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
			
			
			//Convert the payload back to its original form then generate the response
			String rawRequest = Packet.buildPayload(expectedPayload);
			Response currentResponse = new Response (rawRequest, directory);
			currentResponse.parseRequest();
			String responseMessage = currentResponse.generateResponse();
			
			
			//Response Packet Variables
			byte [] responseLoad = responseMessage.getBytes();
			
			//Message and window size variables. Window size and sequence numbers are determined by the server. 
			int responseSize = responseLoad.length;
			int outgoingPackets = 0;
			
			if (responseSize % 1013 == 0)
			{
				outgoingPackets = responseSize/1013;
			}
			
			else
			{
				outgoingPackets = (responseSize/1013) + 1;
			} 
			
			
			//Communicate size of incoming message to client
			outgoing = new Packet (DATA_TYPE.SYN.getValue(), outgoingPackets, InetAddress.getLocalHost(), clientPort, emptyLoad);
			outBound = Packet.buildOutgoingPacket(outgoing,InetAddress.getLocalHost(), routerPort);
			serverSocket.send(outBound);
			
			//Receive ACK and beginning sending response.
			inBound = holdingPacket;
			serverSocket.receive(inBound);
			incoming = Packet.buildIncomingPacket(inBound);
			
			if (incoming.getPacketType() == DATA_TYPE.SYN_ACK.getValue())
			{
				System.out.println("Begin Response");
				
				
			}
			
			
			//responseMessage gets chopped up into a byte array will be sent out 1013 bytes at a go as the packet payload
			DatagramPacket [] readyPacks = Packet.buildPackets(responseMessage, maxSequenceNumber, InetAddress.getLocalHost(), clientPort, routerPort);
			int packetCounter = 0;
			boolean initialBurst = false;
			boolean [] sent = new boolean [] {false,false,false,false,false,false,false,false,false,false};
			boolean [] acked = new boolean [] {false,false,false,false,false,false,false,false,false,false};
			int sentPackets = 0;
			int lastAcked = 0;
			int hops = 0;
			windowBase = defaultWindowBase;
			windowMax = defaultWindowMax;
			
			//Set up the packet timers
			PacketTimer [] packetTimers = new PacketTimer [sequenceNumbers];
			
			for (int i = 0; i < sequenceNumbers; i++)
			{
				packetTimers[i] = new PacketTimer();
			}
			
			//Outgoing Response Logic
			while (sentPackets < outgoingPackets)
			{
				//Send an initial burst of packets to begin pipelining.
				 if (!initialBurst)
				 {
					 for (int j = windowBase; j < windowSize; j++)
					 {
						 
						 Packet.sendPacket(packetTimers[j], serverSocket, readyPacks[packetCounter], timeDelay, timer); 
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
				 incoming = Packet.receivePacket(inBound, holdingPacket, serverSocket);
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
						 if (acked[(i + windowBase) % sequenceNumbers])
						 {
							 hops ++;
							 acked[(i + windowBase) % sequenceNumbers] = false;
							 sent[(i + windowBase) % sequenceNumbers] = false;
						 }
						 else
						 {
							 break;
						 }
					 }
					 
					 //If there are any moves to be made, the window will be shifted here.
					 if (hops != 0)
					 {
						 windowBase = (windowBase + hops) % sequenceNumbers; 
						 windowMax = (windowMax + hops) % sequenceNumbers;
						 
						//Send out any new packets if the window has shifted
						 
						 for (int i = 0; i < windowSize; i++)
						 {
							 if (!sent[(i + windowBase) % sequenceNumbers])
							 {
								 Packet.sendPacket(packetTimers[(i + windowBase) % sequenceNumbers], serverSocket, readyPacks[packetCounter], timeDelay, timer);
								 sent[(i + windowBase) % sequenceNumbers] = true;
								 packetCounter ++;
							 }
						 }
					 }
					 	 
				 }
				  
			}
			
			System.out.println("Response complete");
			
	}
			
			//Removed code
			/*
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
			
			
			serverOut.println(currentResponse.generateResponse());
			System.out.println("\nRequest Completed"); */
		}
				
	}


