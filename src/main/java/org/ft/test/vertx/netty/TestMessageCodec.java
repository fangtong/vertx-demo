/**
 * 
 */
package org.ft.test.vertx.netty;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author fangtong
 *
 */
public class TestMessageCodec implements MessageCodec<Object, Object> {

	@Override
	public void encodeToWire(Buffer buffer, Object s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object decodeFromWire(int pos, Buffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object transform(Object s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

	
}
