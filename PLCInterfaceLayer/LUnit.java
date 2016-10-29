package PLCInterfaceLayer;

import java.sql.ResultSet;
import java.util.ArrayList;

import PLCInterfaceLayer.Sensor.SensorEvent;

/* Abstrakte, natürliche Klasse: Repräsentiert eine unspezifizierte logistische Einheit
 * Erzeugt ihre Sensoren und Aktuatoren gemäß der Spezifikation im zugehörigen SPS-Datenblatt.
 */
public abstract class LUnit {
	
	protected PLC plc;
	protected int id;
	protected String name;
	protected ArrayList<Sensor>   sensors   = new ArrayList<Sensor>();
	protected ArrayList<Actuator> actuators = new ArrayList<Actuator>();
	
	protected boolean receptive = false;
	protected boolean readytopass = false;
	protected boolean busy = false;
	
	// Konstruktor
	public LUnit (PLC plc, int id, String name, ArrayList<Integer> IORows) {
		System.out.println("	Einheit '" + name + "' vom Typ " + this.getClass().getName() + " wurde angelegt.");
		
		this.plc = plc;
		this.id = id; 
		this.name = name;
		
		buildPhysicalIOs(IORows);
	}

	// Erzeuge Sensoren und Aktuatoren
	private void buildPhysicalIOs(ArrayList<Integer> IORows) {
		try {
			ResultSet rs = this.plc.sqlQuery("SELECT * FROM IOConfig");
				
			int i = 1;
			while(rs.next()) {
				if(IORows.contains(i)) {			
					String ioname      = rs.getString(1);
					String description = rs.getString(3);
					String address 	   = rs.getString(2).replaceAll("\\s+", "");
			
					boolean isSensor = (address.charAt(0) == 'E') ? true : false;
					String[] addr = address.substring(1).split("\\.");
					int _byte = Integer.parseInt(addr[0]);
					int _bit  = Integer.parseInt(addr[1]);
				
					if(isSensor) {
						Sensor newsensor = new Sensor(this, ioname, _byte, _bit, description);
						this.sensors.add(newsensor);
					} else { 
						Actuator newactuator = new Actuator(this, ioname, _byte, _bit, description);
						this.actuators.add(newactuator);
					}
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<LUnit> getPredecessors() {
		return this.plc.getPredecessors(this);
	}
	
	public ArrayList<LUnit> getSuccessors() {
		return this.plc.getSuccessors(this);
	}
	
	protected abstract LUnit getPredecessor();
	
	protected abstract LUnit getSuccessor();
	
	public PLC getPLC() {
		return this.plc;
	}
	
	public int getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ArrayList<Sensor> getSensors() {
		return this.sensors;
	}
	
	public ArrayList<Actuator> getActuators() {
		return this.actuators;
	}

	protected void setActuator(Actuator actuator, boolean value) {
		actuator.setValue(value);
	}
	
	public boolean isReceptive() {
		return this.receptive;
	}
	
	public boolean isReadytopass() {
		return this.readytopass;
	}
	
	public boolean isBusy() {
		return this.busy;
	}
		
	public abstract void SensorNotification(Sensor sensor, SensorEvent event);
	
	public abstract void UnitNotification(LUnit unit);
	
	protected void notifyNeighbouredUnits() {
		LUnit predecessor = getPredecessor();
		LUnit successor   = getSuccessor();
		if (predecessor != null) predecessor.UnitNotification(this);
		if (successor   != null) successor.UnitNotification(this);
	}

}
