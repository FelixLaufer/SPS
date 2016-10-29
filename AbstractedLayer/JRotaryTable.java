package AbstractedLayer;
import java.util.ArrayList;

import PLCInterfaceLayer.PLC;


public class JRotaryTable extends RotaryTable implements Joining {

	public JRotaryTable(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		// TODO Auto-generated constructor stub
	}


}
