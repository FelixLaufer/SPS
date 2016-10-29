package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.PLC;


public class BRotaryTable extends RotaryTable implements Branching {

	public BRotaryTable(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
	}


}
