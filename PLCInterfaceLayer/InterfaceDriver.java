package PLCInterfaceLayer;

public interface InterfaceDriver {
	public abstract boolean connect();
	public abstract void disconnect();
	public abstract boolean isConnected();
	public abstract String getIPAdress();
	public abstract int readRequest();
	public abstract boolean readBit(int _byte, int _bit);
	public abstract boolean writeBit(int _byte, int _bit, boolean value);
	public abstract boolean writeBuffer(byte[] buffer);
}
