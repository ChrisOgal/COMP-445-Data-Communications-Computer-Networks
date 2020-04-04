/**
 * 
 */
package resources;

/**
 * @author chris
 *
 */

public enum DATA_TYPE {
	DATA((byte) 0),
	ACK((byte) 1),
	SYN( (byte) 2),
	SYN_ACK((byte)3);
	
	private final byte value;
	
	DATA_TYPE(byte value) {
		
		this.value = value;
	}
	
	public byte getValue ()
	{
		return value;
	}
}
