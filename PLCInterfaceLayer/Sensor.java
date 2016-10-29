package PLCInterfaceLayer;

/* Abgeleitete natürliche Klasse --> PhysicalIO:
 * Repräsentiert einen Sensor, kann bei Sensorereignissen Event Threads starten
 */
public class Sensor extends PhysicalIO {

	public enum SensorEvent {
		RisingEdge,
		FallingEdge
	}
	
	private volatile SensorEvent lastEvent;
	
	// Konstruktor
	public Sensor(LUnit unit, String name, int _byte, int _bit, String description) {
		super(unit, name, _byte, _bit, description);
		// Registriere Sensor bei PLC für Schnellzugriff
		unit.getPLC().registerSensor(this, _byte, _bit);
	}

	public void notifyEvent(SensorEvent event) {
		this.lastEvent = event;
		this.unit.SensorNotification(this, event);
		
		if (event == SensorEvent.RisingEdge) {
			System.out.println("Sensor '" + this.name + "': 0->1");
		} else {
			System.out.println("Sensor '" + this.name + "': 1->0");
		}
	}
	
	public boolean State() {
		return (this.lastEvent == SensorEvent.RisingEdge) ? true : false;
	}

}
 