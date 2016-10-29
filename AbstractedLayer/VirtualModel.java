package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.ConnectionManager;
import PLCInterfaceLayer.PLC.ConnectionMode;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.SensorUpdater;


public class VirtualModel {
	Packet[] packets;
	
	private static SensorUpdater sU;
	private static Thread  sUThread;
	
	private static ArrayList<PLC> plcs = new ArrayList<PLC>();
	
	
	public static void main(String[] args) {
				
		PLC HRG1 = new PLC("HRG1", "140.80.0.1", ConnectionMode.Hardware);
		plcs.add(HRG1);
		
		// Verbindungen zu allen SPSen herstellen
		ConnectionManager.connectPLCs(plcs);
		
		// Alle SPSen verbunden? -> Sensor-Überwachungsthread starten
		if (ConnectionManager.areConnected()) {
			sU = new SensorUpdater();
			sUThread = new Thread(sU);
			sUThread.start();
		} else {
			System.out.println("SensorUpdater konnte nicht gestartet werden: Nicht alle SPSen verbunden.");
		}
		
	}
	
	public void emergencyStop() {
		sU.stopScan();
		for (PLC plc : plcs) {
			plc.emergencyStop();
		}
	}
}
