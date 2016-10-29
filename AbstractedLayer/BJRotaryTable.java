package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.PLC;


public class BJRotaryTable extends RotaryTable implements Branching, Joining {

	public BJRotaryTable(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
	}

}
