package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;


public class LConveyor extends Conveyor {

	protected int intersensorcounter = 0;
	protected boolean busy_s1 = false;
	protected boolean busy_s2 = false;
	
	public LConveyor(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
	}

	@Override
	public void SensorNotification(Sensor sensor, SensorEvent event) {
		
		// Fehlerdiagnose des allgemeinen Förderers
		super.SensorNotification(sensor, event);
		
		// Unitverhalten im Normalfall
		if (sensor.equals(this.getFirstSensor())) {
			if (event.equals(SensorEvent.RisingEdge)) {
				this.busy_s1 = true;
				++this.intersensorcounter;
			} else if (event.equals(SensorEvent.FallingEdge)) {
				this.busy_s1 = false;
			}
		} else if (sensor.equals(this.getLastSensor())) {
			if (event.equals(SensorEvent.RisingEdge)) {
				--this.intersensorcounter;
				this.busy_s2 = true;
			} else if (event.equals(SensorEvent.FallingEdge)) {
				this.busy_s2 = false;
			}
		}
		
		this.readytopass = (this.conveyingTransferCounter > 0) ? routingAllowed : false;
		this.receptive   = (this.intersensorcounter < 2) && !this.busy_s2;
		this.busy        = busy_s1 || busy_s2 || (intersensorcounter > 0);
			
		// Bandsteuerung
		controlConveyor();
			
		if (!this.conveyorstate.equals(ConveyorState.OK)) {
			System.out.println("Einheit '" + this.name + "' befindet sich im Fehlerzustand '" + this.conveyorstate.toString() + "'");
		}
		
		// Informiere Nachbarunits
		notifyNeighbouredUnits();
	}

	@Override
	public void UnitNotification(LUnit unit) {
		// Bandsteuerung
		controlConveyor();
	}
	
	@Override
	protected boolean conveyingAllowed() {
		LUnit successor = this.getSuccessor();
		boolean packetbetweensensors = (this.intersensorcounter > 1 || (this.intersensorcounter == 1 && (successor != null && successor.isReceptive())));
		return super.conveyingAllowed() || packetbetweensensors;
	}

}
