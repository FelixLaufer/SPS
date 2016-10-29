package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;


public class StackerCrane extends LUnit implements Branching, Joining{

	public StackerCrane(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
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

	@Override
	public void SensorNotification(Sensor sensor, SensorEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UnitNotification(LUnit unit) {
		// TODO Auto-generated method stub
		
	}

}
