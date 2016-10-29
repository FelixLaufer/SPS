package PLCInterfaceLayer;

import java.util.BitSet;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;

public class SimulationDriver implements InterfaceDriver {

	private int maxNumberOfBytes;
	private static String interfaceidentifier = "剔卙䵉卟䡃䥎呔呓䱅䕌";
	
	public static final int PAGE_READONLY  = 0x02;
	public static final int PAGE_READWRITE = 0x04;
	public static final int PAGE_WRITECOPY = 0x08;

	public static final int FILE_MAP_COPY  = 0x0001;
	public static final int FILE_MAP_WRITE = 0x0002;
	public static final int FILE_MAP_READ  = 0x0004;
	public static HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant((Pointer.SIZE == 8) ? -1 : 0xFFFFFFFF));
	
	private Kernel32 kernel32;
	private HANDLE hmapFile;
	private Pointer AAdrSpace, EAdrSpace;
	
	private byte[] EAdrByteBuffer;
	
	// Konstruktor
	public SimulationDriver(int maxNumberOfBytes) {
		this.maxNumberOfBytes = maxNumberOfBytes;
	}

	@Override
	public boolean connect() {
		openFileMapping();
		mapViewOfFile();
		return (this.hmapFile != null);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isConnected() {
		return (this.hmapFile != null);
	}

	@Override
	public String getIPAdress() {
		return "TRYSIM_SCHNITTSTELLE";
	}
	
	private void openFileMapping() {
		this.kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS); 
		this.hmapFile = kernel32.CreateFileMapping(INVALID_HANDLE_VALUE, null, PAGE_READWRITE, 0, 4 * 64 * 1024, interfaceidentifier);
	}
	
	private void mapViewOfFile() {
		if (this.hmapFile != null) {
			this.EAdrSpace = this.kernel32.MapViewOfFile(this.hmapFile, FILE_MAP_READ,  0, 2 * 64 * 1024, 64 * 1024);
			this.AAdrSpace = this.kernel32.MapViewOfFile(this.hmapFile, FILE_MAP_WRITE, 0, 1 * 64 * 1024, 64 * 1024);
		}
	}
	
	@Override
	public int readRequest() {
		if (EAdrSpace != null) {
			this.EAdrByteBuffer = EAdrSpace.getByteArray(0, 64 * 1024);
			return maxNumberOfBytes; 
		} 
		return 0; 
	}

	@Override
	public boolean readBit(int _byte, int _bit) {
		return toBitSet(this.EAdrByteBuffer[(64 * 1024 - 1) - _byte]).get(_bit);
	}

	@Override
	public boolean writeBit(int _byte, int _bit, boolean value) {
		if (this.AAdrSpace != null) {
			BitSet readBitSet = toBitSet(AAdrSpace.getByteArray(0, 64 * 1024)[(64 * 1024 - 1) - _byte]);
			readBitSet.set(_bit, value);
			byte[] tobewrittenarr = readBitSet.toByteArray();
			byte tobewritten = (tobewrittenarr.length > 0) ? tobewrittenarr[0] : 0;
			this.AAdrSpace.setByte((64 * 1024 - 1) - _byte, tobewritten);
			return true; 
		}
		return false;
	}

	@Override
	public boolean writeBuffer(byte[] buffer) {
		if (this.AAdrSpace != null) {
			for (int _byte_idx = 0; _byte_idx < buffer.length; ++_byte_idx) {
				this.AAdrSpace.setByte((64 * 1024 - 1) - _byte_idx, buffer[_byte_idx]);	
			}
			return true; 
		}
		return false;
	}
	
	private static BitSet toBitSet(byte b) {
		BitSet bs = new BitSet(Byte.SIZE);
		for (int i = 0; i < Byte.SIZE; i++) {
			if (((b >> i) & 1) == 1) bs.set(i);
		}
		return bs;
	}
}
