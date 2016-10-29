package PLCInterfaceLayer;

import java.util.BitSet;

import PLCCom.ConnectResult;
import PLCCom.ReadRequest;
import PLCCom.ReadResult;
import PLCCom.TCP_ISO_Device;
import PLCCom.WriteRequest;
import PLCCom.WriteResult;
import PLCCom.authentication;
import PLCCom.ePLCType;
import PLCCom.eRegion;

public class HardwareDriver extends TCP_ISO_Device implements InterfaceDriver{

	// PLCCom-Lizenzierung
	private static final String _serial = "96562-28186-106466-2766950";
	private static final String _user   = "Laufer";
	static {
		authentication.Serial(_serial);
		authentication.User(_user);
	}
	
	// SPS-Daten
	private static final int rack = 0;
	private static final int slot = 2;
	private static final ePLCType plctype = ePLCType.S7_300_400_compatibel;
	
	// Timeout-Verhalten [ms]
	private static final int _connecttimeout = 1000;
	private static final int _readtimeout 	 =  500;
	
	private int maxNumberOfBytes;
	private ReadResult[] readres;
	
	// Konstruktor
	public HardwareDriver (int maxNumberOfBytes, String IP) {
		super(IP, rack, slot, plctype);
		this.maxNumberOfBytes = maxNumberOfBytes;
		this.setConnecttimeout(_connecttimeout);
		this.setReadTimeout(_readtimeout);
	}

	@Override
	public int readRequest() {
		ReadRequest[] oRequest = new ReadRequest[1];
		oRequest[0] = new ReadRequest();
		oRequest[0].setRegion(eRegion.Input);
		oRequest[0].setDB(0);
		oRequest[0].setStartByte(0);
		oRequest[0].setLen(maxNumberOfBytes);
 
	    this.readres = this.read(oRequest);
	    if (this.readres[0].HasWorked() & this.readres[0].DataAvailable()) {
	    	return this.readres[0].BufferLen();
	    } else {
	    	return 0;
	    }
	}

	@Override
	public boolean readBit(int _byte, int _bit) {
		return toBitSet(this.readres[0].get_Byte(_byte)).get(_bit);
	}

	@Override
	public boolean writeBit(int _byte, int _bit, boolean value) {
		WriteRequest[] oWriteRequest = new WriteRequest[1];
        oWriteRequest[0] = new WriteRequest();
        oWriteRequest[0].setRegion(eRegion.Output);
        oWriteRequest[0].setDB(0);
        oWriteRequest[0].setStartByte(_byte);
        oWriteRequest[0].setBit((byte)_bit);
        oWriteRequest[0].addBit(value);
		
        WriteResult[] res = this.write(oWriteRequest);
        return res[0].HasWorked();
	}

	@Override
	public boolean writeBuffer(byte[] buffer) {
		WriteRequest[] oWriteRequest = new WriteRequest[1];
		oWriteRequest[0] = new WriteRequest();
		oWriteRequest[0].setRegion(eRegion.Output);
		oWriteRequest[0].setDB(0);
		oWriteRequest[0].setStartByte(0);
		oWriteRequest[0].addByte(buffer);

		WriteResult[] res = this.write(oWriteRequest);
		return res[0].HasWorked();
	}
	
	private BitSet toBitSet(byte b) {
		BitSet bs = new BitSet(Byte.SIZE);
		for (int i = 0; i < Byte.SIZE; i++) {
			if (((b >> i) & 1) == 1) bs.set(i);
		}
		return bs; 
	}

	@Override
	public boolean connect() {
		ConnectResult res = this.Connect();
		return res.HasConnected();
	}

	@Override
	public void disconnect() {
		this.DisConnect();
	}

	@Override
	public boolean isConnected() {
		return this.IsConnected();
	}
}
