package com.shimizukenta.secstestutil;

import java.io.Closeable;
import java.io.IOException;

import com.shimizukenta.secs.SecsException;
import com.shimizukenta.secs.SecsMessage;
import com.shimizukenta.secs.hsms.AbstractHsmsDataMessage;
import com.shimizukenta.secs.hsms.HsmsMessageType;
import com.shimizukenta.secs.hsmsss.AbstractHsmsSsControlMessage;
import com.shimizukenta.secs.hsmsss.HsmsSsCommunicator;
import com.shimizukenta.secs.hsmsss.HsmsSsCommunicatorConfig;
import com.shimizukenta.secs.secs1.Secs1Communicator;
import com.shimizukenta.secs.secs1.Secs1MessageBuilder;
import com.shimizukenta.secs.secs1.Secs1TooBigSendMessageException;
import com.shimizukenta.secs.secs1ontcpip.Secs1OnTcpIpCommunicator;
import com.shimizukenta.secs.secs1ontcpip.Secs1OnTcpIpCommunicatorConfig;
import com.shimizukenta.secs.secs1ontcpip.Secs1OnTcpIpNotConnectedException;

/**
 * 
 * run like SH-2000
 *
 */
public class Secs1OnTcpIpHsmsSsConverter implements Closeable {
	
	public static final byte REJECT_BY_OVERFLOW = (byte)0x80;
	public static final byte REJECT_BY_NOT_CONNECT = (byte)0x81;
	
	private final Secs1Communicator secs1;
	private final HsmsSsCommunicator hsmsSs;
	private boolean closed;
	
	public Secs1OnTcpIpHsmsSsConverter(
			Secs1OnTcpIpCommunicatorConfig secs1Config,
			HsmsSsCommunicatorConfig hsmsSsConfig) {
		
		this.secs1 = Secs1OnTcpIpCommunicator.newInstance(secs1Config);
		this.hsmsSs = HsmsSsCommunicator.newInstance(hsmsSsConfig);
		this.closed = false;
	}
	
	public Secs1OnTcpIpHsmsSsConverter(
			HsmsSsCommunicatorConfig hsmsSsConfig,
			Secs1OnTcpIpCommunicatorConfig secs1Config
			) {
		
		this(secs1Config, hsmsSsConfig);
	}
	
	public void open() throws IOException {
		
		synchronized ( this ) {
			if ( closed ) {
				throw new IOException("Already closed");
			}
		}
		
		secs1.addReceiveMessagePassThroughListener(msg -> {
			
			byte[] header = createToHsmsSsHead(msg);
			
			try {
				hsmsSs.send(new AbstractHsmsDataMessage(header, msg.secs2()) {

					private static final long serialVersionUID = -5692025300162749028L;
				});
			}
			catch ( InterruptedException ignore ) {
			}
			catch ( SecsException giveup ) {
			}
		});
		
		hsmsSs.addReceiveMessagePassThroughListener(msg -> {
			
			if ( msg.getStream() < 0 ) {
				return;
			}
			
			byte[] header = createToSecs1Head(msg);
			
			try {
				secs1.send(Secs1MessageBuilder.build(header, msg.secs2()));
			}
			catch ( InterruptedException ignore ) {
			}
			catch ( Secs1OnTcpIpNotConnectedException e ) {
				rejectToHsmsSs(msg, REJECT_BY_NOT_CONNECT);
			}
			catch ( Secs1TooBigSendMessageException e ) {
				rejectToHsmsSs(msg, REJECT_BY_OVERFLOW);
			}
			catch ( SecsException giveup ) {
			}
		});
		
		secs1.open();
		hsmsSs.open();
	}
	
	@Override
	public void close() throws IOException {
		
		synchronized ( this ) {
			if ( closed) {
				return;
			}
			
			this.closed = true;
		}
		
		IOException ioExcept = null;
		
		try {
			secs1.close();
		}
		catch ( IOException e ) {
			ioExcept = e;
		}
		
		try {
			hsmsSs.close();
		}
		catch ( IOException e ) {
			ioExcept = e;
		}
		
		if ( ioExcept != null ) {
			throw ioExcept;
		}
	}
	
	private byte[] createToSecs1Head(SecsMessage msg) {
		
		byte[] ref = msg.header10Bytes();
		
		byte[] head = new byte[] {
				ref[0],
				ref[1],
				ref[2],
				ref[3],
				(byte)0x0,
				(byte)0x0,
				ref[6],
				ref[7],
				ref[8],
				ref[9]
		};
		
		if ( secs1.isEquip() ) {
			head[0] |= (byte)0x80;
		}
		
		return head;
	}
	
	private byte[] createToHsmsSsHead(SecsMessage msg) {
		
		byte[] ref = msg.header10Bytes();
		
		byte[] head = new byte[] {
				(byte)(ref[0] & 0x7F),
				ref[1],
				ref[2],
				ref[3],
				(byte)0x0,
				(byte)0x0,
				ref[6],
				ref[7],
				ref[8],
				ref[9]
		};
		
		return head;
	}
	
	private void rejectToHsmsSs(SecsMessage primary, byte reason) {
		
		byte[] ref = primary.header10Bytes();
		HsmsMessageType mt = HsmsMessageType.REJECT_REQ;
		
		byte[] head = new byte[] {
				(byte)0xFF,
				(byte)0xFF,
				(byte)0x0,
				reason,
				mt.pType(),
				mt.sType(),
				ref[6],
				ref[7],
				ref[8],
				ref[9]
		};
		
		try {
			hsmsSs.send(new AbstractHsmsSsControlMessage(head) {

				private static final long serialVersionUID = 1L;
			});
		}
		catch ( InterruptedException ignore ) {
		}
		catch ( SecsException giveup ) {
		}
	}
	
	public static Secs1OnTcpIpHsmsSsConverter newInstance(
			Secs1OnTcpIpCommunicatorConfig secs1Config,
			HsmsSsCommunicatorConfig hsmsSsConfig) {
		
		return new Secs1OnTcpIpHsmsSsConverter(secs1Config, hsmsSsConfig);
	}
	
	public static Secs1OnTcpIpHsmsSsConverter newInstance(
			HsmsSsCommunicatorConfig hsmsSsConfig,
			Secs1OnTcpIpCommunicatorConfig secs1Config
			) {
		
		return new Secs1OnTcpIpHsmsSsConverter(hsmsSsConfig, secs1Config);
	}
	
	private static Secs1OnTcpIpHsmsSsConverter open(Secs1OnTcpIpHsmsSsConverter i) throws IOException {
		
		try {
			i.open();
		}
		catch ( IOException e ) {
			
			try {
				i.close();
			}
			catch ( IOException giveup ) {
			}
			
			throw e;
		}
		
		return i;
	}
	
	public static Secs1OnTcpIpHsmsSsConverter open(
			Secs1OnTcpIpCommunicatorConfig secs1Config,
			HsmsSsCommunicatorConfig hsmsSsConfig)
					throws IOException {
		
		return open(new Secs1OnTcpIpHsmsSsConverter(secs1Config, hsmsSsConfig));
	}
	
	public static Secs1OnTcpIpHsmsSsConverter open(
			HsmsSsCommunicatorConfig hsmsSsConfig,
			Secs1OnTcpIpCommunicatorConfig secs1Config)
					throws IOException {
		
		return open(new Secs1OnTcpIpHsmsSsConverter(hsmsSsConfig, secs1Config));
	}
	
}

