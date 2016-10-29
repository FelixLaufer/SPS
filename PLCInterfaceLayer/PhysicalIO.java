package PLCInterfaceLayer;

/* Natürliche Klasse: Repräsentiert einen Physikalischen Sensor oder Aktuator */
public class PhysicalIO {
	
	protected int _byte;
	protected int _bit;
	protected LUnit unit;
	protected String name;
	protected String description;
	
	// Konstruktor
	public PhysicalIO(LUnit unit, String name, int _byte, int _bit, String description) {
		this.unit = unit;
		this.name = name;
		this.description = description;
		this._byte = _byte;
		this._bit = _bit;
		
		String IOtype = (this instanceof Sensor) ? "Sensor" : "Aktor";
		System.out.println("		" + IOtype + " '" + name + "' wurde angelegt.");
	}
	
	public LUnit getLUnit() {
		return this.unit;
	}
	
	public boolean hasAddr(int _byte, int _bit) {
		return (this._byte == _byte && this._bit == _bit);
	}

}
