package AbstractedLayer;

import java.util.ArrayList;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;

public class Generator extends LUnit {

	public Generator(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void SensorNotification(Sensor sensor, SensorEvent event) {
		if (event.equals(SensorEvent.RisingEdge)) {
			this.setActuator(this.getActuators().get(0), true);
		} else {
			this.setActuator(this.getActuators().get(0), false);
		}
	}

	@Override
	public void UnitNotification(LUnit unit) {
		return;
	}

	@Override
	protected LUnit getPredecessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected LUnit getSuccessor() {
		// TODO Auto-generated method stub
		return null;
	}

}
