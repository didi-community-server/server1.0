
package com.didi.community.socket;

import java.nio.ByteBuffer;

/** Controls how objects are transmitted over the network. */
public interface Serialization {
	/** @param connection May be null. */
	public void write (Connection connection, ByteBuffer buffer, Object json);

	public String read (Connection connection, ByteBuffer buffer);

	/** The fixed number of bytes that will be written by {@link #writeLength(ByteBuffer, int)} and read by
	 * {@link #readLength(ByteBuffer)}. */
	public int getLengthLength ();

	public void writeLength (ByteBuffer buffer, int length);

	public int readLength (ByteBuffer buffer);
}
