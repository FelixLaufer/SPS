package PLCInterfaceLayer;

/* Technische Klasse: Aktualisiert kontinuierlich Sensorinformationen, gleicht sie mit vorherigen ab 
 * und feuert bei Verschiedenheit Events an die betreffenden Sensor-Objekte.
 */
public class SensorUpdater implements Runnable {
	private volatile boolean doScan = true;
	private final static int maxNumberOfBytes = 10;
	
	private boolean[][][] sensormap;	// [PLC][Byte][Bit]
	
	// Konstruktor
	public SensorUpdater() {
		this.sensormap = new boolean[ConnectionManager.getPLCs().size()][maxNumberOfBytes][8];
	}
	
	@Override
	public void run() {
		
		// Initialisierungs-Scan
		{
			int plc_idx = 0;
			for (PLC plc : ConnectionManager.getPLCs()) {	
				int bufferlength = plc.readRequest();
			   
				for (int _byte = 0; _byte < Math.min(bufferlength, maxNumberOfBytes); ++_byte) {
			    	for (int _bit = 0; _bit < 8; ++_bit) {
				        boolean readbit = plc.readBit(_byte, _bit); 	
				        sensormap[plc_idx][_byte][_bit] = readbit;
				    }
				} 
			    ++plc_idx;
			}
		}
		
		// Kontinuierlicher Scan
		while(doScan) {
			int plc_idx = 0;
			for (PLC plc : ConnectionManager.getPLCs()) {	
				int bufferlength = plc.readRequest();

			    for (int _byte = 0; _byte < Math.min(bufferlength, maxNumberOfBytes); ++_byte) {
			    	for (int _bit = 0; _bit < 8; ++_bit) {
				        boolean readbit = plc.readBit(_byte, _bit);
				            	
				        if(sensormap[plc_idx][_byte][_bit] != readbit) {
				            // Sensor-Event liegt vor
				            sensormap[plc_idx][_byte][_bit] = readbit;		         
					        Sensor s = plc.getSensor(_byte, _bit);
					        if (s != null) {
					            s.notifyEvent((readbit) ? Sensor.SensorEvent.RisingEdge : Sensor.SensorEvent.FallingEdge);
					        } else {
					            System.out.println("Sensor [" + _byte + "," + _bit + "] konnte nicht aufgelöst werden.");
					        }
				        }
				    }
				} 
			    ++plc_idx;
			}
		}	
	}
	
	public void stopScan() {
		this.doScan = false;
	}
	
	public void startScan() {
		this.doScan = true;
	}

}