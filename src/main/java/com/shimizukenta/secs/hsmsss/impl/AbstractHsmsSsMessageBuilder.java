package com.shimizukenta.secs.hsmsss.impl;

import com.shimizukenta.secs.hsms.HsmsMessageType;
import com.shimizukenta.secs.hsms.impl.AbstractHsmsMessage;
import com.shimizukenta.secs.hsms.impl.AbstractHsmsMessageBuilder;
import com.shimizukenta.secs.hsms.impl.AbstractHsmsSession;

public abstract class AbstractHsmsSsMessageBuilder extends AbstractHsmsMessageBuilder {
	
	public AbstractHsmsSsMessageBuilder() {
		super();
	}
	
	@Override
	public AbstractHsmsMessage buildSelectRequest(AbstractHsmsSession session) {
		
		byte[] sysbytes = this.getSystem4Bytes(session);
		
		byte[] header = new byte[] {
				(byte)0xFF,
				(byte)0xFF,
				(byte)0x0,
				(byte)0x0,
				HsmsMessageType.SELECT_REQ.pType(),
				HsmsMessageType.SELECT_REQ.sType(),
				sysbytes[0],
				sysbytes[1],
				sysbytes[2],
				sysbytes[3]
		};
		
		return build(header);
	}
	
	@Override
	public AbstractHsmsMessage buildDeselectRequest(AbstractHsmsSession session) {
		
		byte[] sysbytes = this.getSystem4Bytes(session);
		
		byte[] header = new byte[] {
				(byte)0xFF,
				(byte)0xFF,
				(byte)0x0,
				(byte)0x0,
				HsmsMessageType.DESELECT_REQ.pType(),
				HsmsMessageType.DESELECT_REQ.sType(),
				sysbytes[0],
				sysbytes[1],
				sysbytes[2],
				sysbytes[3]
		};
		
		return build(header);
	}
	
	@Override
	public AbstractHsmsMessage buildSeparateRequest(AbstractHsmsSession session) {
		
		byte[] sysbytes = this.getSystem4Bytes(session);
		
		byte[] header = new byte[] {
				(byte)0xFF,
				(byte)0xFF,
				(byte)0x0,
				(byte)0x0,
				HsmsMessageType.SEPARATE_REQ.pType(),
				HsmsMessageType.SEPARATE_REQ.sType(),
				sysbytes[0],
				sysbytes[1],
				sysbytes[2],
				sysbytes[3]
		};
		
		return build(header);
	}
	
}
