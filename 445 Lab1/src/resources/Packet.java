/**
 * 
 */
package resources;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Timer;

/**
 * @author chris
 *
 */




public class Packet {
	
	private static final int MIN_LENGTH = 11;
	private static final int MAX_LENGTH = 1024;
	
	private byte packetType;
	private int sequenceNumber;
	private InetAddress peerAddress;
	private short peerPort;
	private byte [] payLoad;

	public Packet ()
	{

	}

	/**
	 * @param packetType
	 * @param sequenceNumber
	 * @param peerAddress
	 * @param peerPort
	 * @param payLoad
	 */
	public Packet(byte packetType, int sequenceNumber, InetAddress peerAddress, short peerPort, byte[] payLoad)  {

		this.packetType = packetType;
		this.sequenceNumber = sequenceNumber;
		this.peerAddress = peerAddress;
		this.peerPort = peerPort;
		this.payLoad = payLoad;
	}

	/**
	 * @return the packetType
	 */
	public byte getPacketType() {
		return packetType;
	}

	/**
	 * @param packetType the packetType to set
	 */
	public void setPacketType(byte packetType) {
		this.packetType = packetType;
	}

	/**
	 * @return the sequenceNumber
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the peerAddress
	 */
	public InetAddress getPeerAddress() {
		return peerAddress;
	}

	/**
	 * @param peerAddress the peerAddress to set
	 */
	public void setPeerAddress(InetAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	/**
	 * @return the peerPort
	 */
	public short getPeerPort() {
		return peerPort;
	}

	/**
	 * @param peerPort the peerPort to set
	 */
	public void setPeerPort(short peerPort) {
		this.peerPort = peerPort;
	}

	/**
	 * @return the payLoad
	 */
	public byte[] getPayLoad() {
		return payLoad;
	}

	/**
	 * @param payLoad the payLoad to set
	 */
	public void setPayLoad(byte[] payLoad) {
		this.payLoad = payLoad;
	}
	
	public static byte[] packetToBytes (Packet p)
	{
		
		
		ByteBuffer buffer = ByteBuffer.allocate(MAX_LENGTH).order(ByteOrder.BIG_ENDIAN);
		
		buffer.put(p.packetType);
		buffer.putInt(p.sequenceNumber);
		buffer.put(p.peerAddress.getAddress());
		buffer.putShort(p.peerPort);
		buffer.put(p.payLoad);
		
		buffer.flip();

		byte [] rawPacket = new byte [buffer.remaining()];
		buffer.get(rawPacket);
		
		return rawPacket;
	}
	
	public static Packet bytesToPacket (byte [] b)
	{
		Packet preparedPacket = new Packet ();
		
		/*ByteBuffer buf = ByteBuffer.allocate(MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        buf.put(bytes);
        buf.flip();
        return fromBuffer(buf); */
		
		ByteBuffer buffer = ByteBuffer.allocate(MAX_LENGTH).order(ByteOrder.BIG_ENDIAN);
		buffer.put(b);
		buffer.flip();
		
		preparedPacket.packetType = buffer.get();
		preparedPacket.sequenceNumber = buffer.getInt();
		byte [] hostAddress = new byte [] {buffer.get(), buffer.get(), buffer.get(), buffer.get()};
		
		try {
			preparedPacket.peerAddress = Inet4Address.getByAddress(hostAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		preparedPacket.peerPort = buffer.getShort();
		byte [] packetLoad = new byte [buffer.remaining()];
		
		buffer.get(packetLoad);
		
		preparedPacket.payLoad = packetLoad;
		
		return preparedPacket;
	}
	
	public static DatagramPacket buildOutgoingPacket (Packet p, InetAddress outgoingAddress, short outgoingPort)
	{

		DatagramPacket dp = null;
		byte [] message = packetToBytes(p);
		dp = new DatagramPacket(message, message.length, outgoingAddress, outgoingPort);
		return dp;
	}
	
	public static Packet buildIncomingPacket (DatagramPacket dp)
	{
		Packet p = null;
		byte[] message = dp.getData();
		p = bytesToPacket(message);
		return p;
	}
	
	public static byte [][] dividePayload(String message)
	{
		
		
		byte [] rawMessage = message.getBytes();
		int wholeBlocks = rawMessage.length / 1013;
		int additionalBlock = 0;
		int additionalBlockSize = rawMessage.length % 1013;
		
		if ( additionalBlockSize != 0)
		{
			 additionalBlock = 1;
		}
		
		int totalBlocks = wholeBlocks + additionalBlock;
		
		byte [][] dividedPayload = new byte [totalBlocks][];
		
		for (int i = 0; i < wholeBlocks; i++)
		{
			byte [] segment = new byte [1013];
			
			for (int j = 0; j < segment.length; j++)
			{
				segment[j] = rawMessage[(i * 1013) + j];
			}
			
			dividedPayload [i] = segment;
		}
		
		if (additionalBlock != 0)
		{
			byte []  finalBlock = new byte [additionalBlockSize];
			
			for (int i = 0; i < additionalBlockSize; i++)
			{
				finalBlock [i] = rawMessage[(wholeBlocks * 1013) + i];
			}
			
			dividedPayload[totalBlocks - 1]  = finalBlock;
		}
		
		return dividedPayload;
		
	}
	
	public static DatagramPacket [] buildPackets (String message, int maxSequenceNumber, InetAddress peerAddress, short peerPort, short routerPort) throws UnknownHostException
	{
		
		int packCounter = maxSequenceNumber + 1;
		byte [][] dividedPayload =  dividePayload(message);
		DatagramPacket [] readyPacks = new DatagramPacket [dividedPayload.length];
		
		for (int i = 0; i < dividedPayload.length; i++)
		{
			readyPacks[i] = Packet.buildOutgoingPacket(new Packet (DATA_TYPE.DATA.getValue(), (i % packCounter), peerAddress, peerPort, dividedPayload[i]), peerAddress, routerPort);
					
		}
		
		return readyPacks;
		
	}
	
	public static void sendPacket (PacketTimer pt, DatagramSocket socket, DatagramPacket readyPack, long delay, Timer timer)
	{
		pt.setDs(socket);
		 pt.setDp(readyPack);
		 timer.schedule(pt, new Date(), delay);
	}
	
	public static Packet receivePacket (DatagramPacket inBound, DatagramPacket holdingPacket, DatagramSocket socket) throws IOException
	{
		inBound = holdingPacket;
		 socket.receive(inBound);
		 return Packet.buildIncomingPacket(inBound);
	}
	
	public static String buildPayload (Packet [] bufferedRaw)
	{
		String message = "";
		byte [] rawMessage = new byte [bufferedRaw.length * 1013];
		
		for (int i = 0; i < bufferedRaw.length; i++)
		{
			byte [] currentSegment = bufferedRaw[i].getPayLoad();
			
			for (int j = 0; j < currentSegment.length; j++)
			{
					rawMessage [(i * 1013) + j] = currentSegment [j];
				
			}
								
		}
		
		message = new String (rawMessage);
		
		return message;
	}



}
