package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;


public class SConveyor extends Conveyor {
	
	// Konstruktor
	public SConveyor(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
	}

	@Override
	public void SensorNotification(Sensor sensor, SensorEvent event) {
		
		// Fehlerdiagnose des allgemeinen Förderers
		super.SensorNotification(sensor, event);
		
		// Unitverhalten im Normalfall		
		if (event.equals(SensorEvent.RisingEdge)) {
			this.busy		 = true;
			this.receptive	 = false;
		} else if (event.equals(SensorEvent.FallingEdge)) {
			this.busy		 = false;
			this.receptive 	 = true;
		}
		
		this.readytopass = (this.conveyingTransferCounter > 0) ? routingAllowed : false;
		
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
	
}
