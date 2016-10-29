package PLCInterfaceLayer;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;

import AbstractedLayer.BRotaryTable;
import AbstractedLayer.CRotaryTable;
import AbstractedLayer.Generator;
import AbstractedLayer.JRotaryTable;
import AbstractedLayer.LConveyor;
import AbstractedLayer.SConveyor;
import AbstractedLayer.StackerCrane;
import AbstractedLayer.Storage;

/* Natürliche Klasse: Repräsentiert eine einzelne SPS und erzeugt ihre Einheiten 
 * sowie deren Verbindungen aus ihrem jeweiligen Excel-Datenblatt.
 */
public class PLC {
	
	public enum ConnectionMode {
		Simulation,
		Hardware
	}
	
	private static String configpath;
	
	private String name;
	private ArrayList<LUnit> units 		 = new ArrayList<LUnit>();
	private ArrayList<LUnit> inputUnits  = new ArrayList<LUnit>();
	private ArrayList<LUnit> outputUnits = new ArrayList<LUnit>();
	private boolean[][] LUnitAdjacency;
	
	// Caching für Schnellzugriffe auf Register, Kurnamen, Vorgänger und Nachfolger
	private Sensor[][] outregisters 						= new Sensor[10][8];
	private HashMap<String, LUnit> lunitmap					= new HashMap<String, LUnit>();
	private HashMap<LUnit, ArrayList<LUnit>> predecessormap = new HashMap<LUnit, ArrayList<LUnit>>();
	private HashMap<LUnit, ArrayList<LUnit>> successormap   = new HashMap<LUnit, ArrayList<LUnit>>();
	
	private Connection ODBCcon;
	
	// Maximal mögliche Registergröße in Byte
	private static int maxNumberOfBytes = 10;
    private InterfaceDriver IDriver;
	
	// Konstruktor
	public PLC(String name, String IP, ConnectionMode connectionmode) {
		IDriver = (connectionmode.equals(ConnectionMode.Simulation)) ? new SimulationDriver(maxNumberOfBytes) : new HardwareDriver(maxNumberOfBytes, IP);
		
		try {
			configpath = this.getClass().getResource("/Config/").toURI().getPath().substring(1);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		this.name = name;
		System.out.println("SPS '" + name + "' wurde angelegt.");
		
		// Starte neue ODBC-Verbindung mittels MS-Excel-Treiber (32bit)
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String path = PLC.configpath + this.name + ".xls";
			this.ODBCcon = DriverManager.getConnection("jdbc:odbc:DRIVER={Microsoft Excel Driver (*.xls)};DBQ=" + path);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		// Erzeuge LUnits gemäß Spezifikation aus Datenblatt
		buildUnits();
		
		// Schließe ODBC-Verbindung
		try {
			this.ODBCcon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Erzeuge alle für diese SPS definierten LUnits und verbinde sie gemäß Datenblatt
	private void buildUnits() {
		try {
			// Lese LUnits
			ResultSet rs = sqlQuery("SELECT * FROM LUnits");
			
			int i = 0;
			while (rs.next()) {
				// Neue LUnit-Objekte anlegen
				String unitname = rs.getString(1);
				String unittype	= rs.getString(2);
				ArrayList<Integer> IORows = new ArrayList<Integer>();
				
				Object _IORows;
				if ((_IORows = rs.getObject(3)) != null) {
					StringTokenizer tokens = new StringTokenizer(_IORows.toString(), ",");  
					while (tokens.hasMoreElements()) {  
						IORows.add(Integer.parseInt((String)tokens.nextElement())); 
					} 
				}
				
				LUnit newLUnit = null;			
				switch (unittype) {
					case "SConveyor": 		newLUnit = new SConveyor		(this, i, unitname, IORows); break;
					case "LConveyor": 		newLUnit = new LConveyor		(this, i, unitname, IORows); break;
					case "CRotaryTable": 	newLUnit = new CRotaryTable		(this, i, unitname, IORows); break;
					case "BRotaryTable": 	newLUnit = new BRotaryTable		(this, i, unitname, IORows); break;
					case "JRotaryTable": 	newLUnit = new JRotaryTable		(this, i, unitname, IORows); break;
					case "StackerCrane": 	newLUnit = new StackerCrane		(this, i, unitname, IORows); break;
					case "Storage": 		newLUnit = new Storage			(this, i, unitname, IORows); break;
					case "Generator": 		newLUnit = new Generator		(this, i, unitname, IORows); break;
					// ... weitere Typen!
					default: System.out.println("Unbekannte LUnit vom Typ '" + unittype + "'! LUnit-Objekt kann nicht erzeugt werden.");
				}
				
				// Komponiere erzeugtes LUnit-Objekt an PLC-Objekt
				this.units.add(newLUnit);
				i++;
			}
			
			// Lese LUnit-Matrix
			rs = sqlQuery("SELECT * FROM LUnitMatrix");
			
			
			System.out.println("\n 	Adjazenzmatrix:");
			// Lege Adjazenzmatrix an und bestimme Ein-/Ausgangs-LUnits der PLC
			this.LUnitAdjacency = new boolean[i][i];
			
			i = 0;
			while (rs.next()) {
				System.out.print("	");
				for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
					Integer matrixentry;		
					if((matrixentry = rs.getInt(j)) != null) {
						
						System.out.print(matrixentry.toString() + " ");
						
						if (matrixentry == 2) {
							this.inputUnits.add(this.units.get(j-1));
						} else if (matrixentry == 3) {
							this.outputUnits.add(this.units.get(j-1));
						} else if (matrixentry == 1) {
							this.LUnitAdjacency[i][j-1] = true;
						}
					}
					else {
						this.LUnitAdjacency[i][j-1] = false;
						System.out.print("0 ");
					}
				}
				i++;
				System.out.println();
			}
			System.out.print("_______________________________________________\n");
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// Ermittele Vorgänger aus Adjazenzmatrix
	public ArrayList<LUnit> getPredecessors(LUnit currentunit) {
		ArrayList<LUnit> result = new ArrayList<LUnit>();
		// Hashtable-Caching
		if (predecessormap.containsKey(currentunit)) {
			result = predecessormap.get(currentunit);
		} else {
			int currentindex = this.units.indexOf(currentunit);
			for (int i = 0; i < this.LUnitAdjacency[0].length; i++) {
				if (this.LUnitAdjacency[currentindex][i]) {
					result.add(this.units.get(i));
				}
			}
			predecessormap.put(currentunit, result);
		}
		return result;
	}
	
	// Ermittele Nachfolger aus Adjazenzmatrix
	public ArrayList<LUnit> getSuccessors(LUnit currentunit) {
		ArrayList<LUnit> result = new ArrayList<LUnit>();
		// Hashtable-Caching
		if (successormap.containsKey(currentunit)) {
			result = successormap.get(currentunit);
		} else {
			int currentindex = this.units.indexOf(currentunit);
			for (int i = 0; i < this.LUnitAdjacency.length; i++) {
				if (this.LUnitAdjacency[i][currentindex]) {
					result.add(this.units.get(i));
				}
			}
			successormap.put(currentunit, result);
		}
		return result;
	}
	
	// Löse Kurznamen nach LUnit-Objekt auf
	public LUnit getLUnitByName(String name) {
		LUnit result = null;
		
		if(lunitmap.containsKey(name)) {
			result = lunitmap.get(name);
		} else {
			for (LUnit unit : this.units) {
				if (unit.getName().equals(name)) {
					result = unit;
					lunitmap.put(name, unit);
					break;
				}
			}
		}
		return result;
	}

	// Führe SQL-Anfrage an Excel-ODBC-Treiber durch
	public ResultSet sqlQuery(String query) throws Exception {
		Statement stmt = this.ODBCcon.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		return rs;
	}
	
	public String getConfigpath() {
		return PLC.configpath;
	}
	
	public String getName() {
		return this.name;
	}

	// Löse Sensor-Adresstupel zu Sensor-Objekt auf
	public Sensor getSensor(int _byte, int _bit) {
		return this.outregisters[_byte][_bit];
	}
	
	// Registriere Sensor für Schnellzugriff
	public void registerSensor(Sensor sensor, int _byte, int _bit) {
		this.outregisters[_byte][_bit] = sensor;
	}
	
	public void emergencyStop() {
		byte[] emergencySet = new byte[maxNumberOfBytes];
		for (int i = 0; i<emergencySet.length; i++) emergencySet[i] = 0; // TODO: Nötig?
		
		boolean success = IDriver.writeBuffer(emergencySet);
		if (!success) {
			System.out.println("Notstop fehlgeschlagen: SPS " + this.name);
		}
	}
	
	public int readRequest() {
		return IDriver.readRequest();
	}

	public boolean readBit(int _byte, int _bit) {
		return IDriver.readBit(_byte, _bit);
	}

	public boolean writeBit(int _byte, int _bit, boolean value) {
		return IDriver.writeBit(_byte, _bit, value);
	}

	public void setConnecttimeout(int connecttimeout) {
		if (IDriver instanceof HardwareDriver) {
			((HardwareDriver) IDriver).setConnecttimeout(connecttimeout);
		}
	}

	public void setReadTimeout(int readtimeout) {
		if (IDriver instanceof HardwareDriver) {
			((HardwareDriver) IDriver).setReadTimeout(readtimeout);
		}
	}

	public String getIPAdress() {
		return IDriver.getIPAdress();
	}

	public boolean Connect() {
		return IDriver.connect();
	}

	public void DisConnect() {
		IDriver.disconnect();
	}

	public boolean IsConnected() {
		return IDriver.isConnected();
	}
}
