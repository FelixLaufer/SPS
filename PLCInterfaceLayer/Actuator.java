package PLCInterfaceLayer;

/* Abgeleitete natürlich Klasse --> PhysicalIO:
 * Repräsentiert einen Aktuator
 */
public class Actuator extends PhysicalIO {
	
	// Konstruktor
	public Actuator(LUnit unit, String name, int _byte, int _bit, String description) {
		super(unit, name, _byte, _bit, description);
	}
	
	public void setValue (boolean value) {
	    PLC pcl = getLUnit().getPLC();
        
        if (pcl != null) {	        
        	boolean writesuccess = pcl.writeBit(this._byte, this._bit, value);
        	if (!writesuccess) {
	        	System.out.println("Aktuator [" + _byte + "," + _bit + "] konnte nicht ins Ausgaberegister schreiben.");
	        }
        } else {
        	System.out.println("Aktuator [" + _byte + "," + _bit + "] konnte seine SPS nicht auflösen.");
        }
	}
}
