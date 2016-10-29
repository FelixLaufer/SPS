package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;


public class RotaryTable extends SConveyor {

	public enum DTPos {
		POS1,
		POS2,
		Unknown
	}
	
	public class neigborLUnits {
		public String predecessor;
		public String successor;
		
		public neigborLUnits (String predecessor, String successor) {
			this.predecessor = predecessor;
			this.successor = successor;
		}
	}
	
	protected DTPos position;
	protected neigborLUnits[] neighbors = new neigborLUnits[2];
	protected boolean reverseConveying = false;
	
	public RotaryTable(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
	}

	protected LUnit getCurrentPredecessor() {
		if (this.getCurrentPosition().equals(DTPos.Unknown)) return null;
		for (LUnit candidate : this.getPredecessors()) {
			if (candidate.getName().equals(this.neighbors[this.getCurrentPosition().ordinal()].predecessor)) {
				return candidate;
			}
		}
		return null;
	}

	protected LUnit getCurrentSuccessor() {
		if (this.getCurrentPosition().equals(DTPos.Unknown)) return null;
		for (LUnit candidate : this.getSuccessors()) {
			if (candidate.getName().equals(this.neighbors[this.getCurrentPosition().ordinal()].successor)) {
				return candidate;
			}
		}
		return null;
	}
	
	protected DTPos getCurrentPosition() {
		return this.position;
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
