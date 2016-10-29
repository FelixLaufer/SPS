package PLCInterfaceLayer;

import java.util.ArrayList;

/* Technische Klasse: Startet und beendet die Verbindungen zu physikalischen SPSen. */
public class ConnectionManager {
	
	private static ArrayList<PLC> plcs = new ArrayList<PLC>();
	
	public static void connectPLCs(ArrayList<PLC> plcs) {
		ConnectionManager.plcs = plcs;       
        connectAll(); 
	}
	
	// Verbinde einzelnes Device
	public static void connect(PLC device) {   
        boolean res = device.Connect();
        if (!res) {
            System.out.println("Verbindungsfehler: Auf SPS-Device " + device.getIPAdress() + " konnte nicht zugegriffen werden.");
        } else {
        	System.out.println("SPS-Device " + device.getIPAdress() + " connected.");
        }
	}
	
	// Löse Verbindung eines einzelnen Device
	public static void disconnect(PLC device) {
		device.DisConnect();
		System.out.println("SPS-Device " + device.getIPAdress() + " disconnected.");
	}
	
	// Verbinde alle definierten Devices
	public static void connectAll() {	
		for (PLC device : plcs) {
			connect(device);
		}
	}
	
	// Löse Verbindung aller definierten Devices
	public static void disconnectAll() {	
		for (PLC device : plcs) {
			disconnect(device);
		}
	}
	
	// Sind alle Devices verbunden?
	public static boolean areConnected() {	
		for (PLC device : plcs) {
			if (!device.IsConnected()) return false;
		}
		return true;
	}
	
	public static ArrayList<PLC> getPLCs() {
		return plcs;
	} 
}
