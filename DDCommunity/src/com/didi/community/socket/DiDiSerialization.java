
package com.didi.community.socket;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

public class DiDiSerialization implements Serialization {

	public DiDiSerialization () {
		
	}

	public synchronized void write (Connection connection, ByteBuffer buffer, Object json) {
		if(json instanceof FrameworkMessage.KeepAlive){
			buffer.put("didi-server-keepAlive".getBytes());
		}else if(json instanceof FrameworkMessage.RegisterTCP){
			buffer.put("client-register-success".getBytes());
		}else {
			buffer.put(json.toString().getBytes());
		}
		
	}

	public synchronized String read (Connection connection, ByteBuffer buffer) {
		try {
			return Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void writeLength (ByteBuffer buffer, int length) {//写包体长		
		buffer.putInt(length);
	}

	public int readLength (ByteBuffer buffer) {//包体长度
		return buffer.getInt();
	}

	public int getLengthLength () {//4字节包头		
		return 4;
	}
}
