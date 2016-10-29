package AbstractedLayer;
import java.util.ArrayList;
import java.util.Date;

import PLCInterfaceLayer.LUnit;
import PLCInterfaceLayer.PLC;
import PLCInterfaceLayer.Sensor;
import PLCInterfaceLayer.Sensor.SensorEvent;

public abstract class Conveyor extends LUnit {
	
	public enum ConveyorState {
		OK,
		LostPacket,
		UnknownPacket,
	}
	
	public class ConveyingTransferObserver extends Thread {
		private Conveyor observedConeyor;
		private static final long minConveyingTime = 1000;
		private static final long maxConveyingTime = 4000;
		private Date startTimestamp;
		
		public ConveyingTransferObserver (Conveyor observedConeyor) {
			this.observedConeyor = observedConeyor;
			this.startTimestamp = new Date();
		}
		
		public void run() {
		    try {
				Thread.sleep(maxConveyingTime);
			} catch (InterruptedException e) {
				Date stopTimestamp = new Date();
				long conveyingtime = stopTimestamp.getTime() - this.startTimestamp.getTime();
				if (conveyingtime < minConveyingTime) {
					// Externer Eingriff: Förderzeit unrealistisch kurz -> Fördergut wurde manuell hinzugefügt
					LUnit successor = this.observedConeyor.getSuccessor();
				    System.out.println("Fehler: Externer Eingriff zwischen Einheiten'" + this.observedConeyor.name + "' und '" + successor.getName() + "' : Fördergut manuell hinzugefügt!");
				    this.observedConeyor.reportErrorState(ConveyorState.LostPacket);
				}
			}  
		    // Externer Eingriff: Förderzeit unrealistisch lang -> Fördergut wurde manuell hinzugefügt
		    LUnit successor = this.observedConeyor.getSuccessor();
		    System.out.println("Fehler: Externer Eingriff zwischen Einheiten'" + this.observedConeyor.name + "' und '" + successor.getName() + "' : Fördergut manuell entfernt!");
		    this.observedConeyor.reportErrorState(ConveyorState.LostPacket);
		}
	}
	
	protected ConveyingTransferObserver conveyingTransferObserver = null;
	protected int conveyingTransferCounter = 0;
	protected boolean routingAllowed = true;
	protected boolean movingband = false;
	protected ConveyorState conveyorstate = ConveyorState.OK;
	
	// Konstruktor
	public Conveyor(PLC plc, int id, String name, ArrayList<Integer> IORows) {
		super(plc, id, name, IORows);
		this.busy = false;
		this.readytopass = false;
		this.receptive = true;
	}
	
	protected void startBand() {
		setActuator(this.actuators.get(0), true);
		this.movingband = true;
	}
	
	protected void stopBand() {
		setActuator(this.actuators.get(0), false);
		this.movingband = false;
	}
	
	protected LUnit getSuccessor() {
		return (getSuccessors().size() > 0) ? getSuccessors().get(0) : null;
	}
	
	protected LUnit getPredecessor() {
		return (getPredecessors().size() > 0) ? getPredecessors().get(0) : null;
	}
	
	protected Sensor getFirstSensor() {
		return this.sensors.get(0);
	}
	
	protected Sensor getLastSensor() {
		return this.sensors.get(this.sensors.size()-1);
	}

	@Override
	public void SensorNotification(Sensor sensor, SensorEvent event) {

		// Fehlerdiagnose gegen Verklemmungen bei Übergängen zwischen Einheiten
		if (sensor.equals(this.getFirstSensor()) && event.equals(SensorEvent.RisingEdge)) {
			LUnit predecessor = this.getPredecessor();
			if (this.movingband && predecessor != null && predecessor instanceof Conveyor) {
				Conveyor cpredecessor = (Conveyor) predecessor;
				cpredecessor.completeConveyingTransfer();
			}
		}
		
		if (sensor.equals(this.getLastSensor())) {
			if (event.equals(SensorEvent.RisingEdge)) {
				LUnit successor = this.getSuccessor();
				if (successor != null && successor instanceof Conveyor) {
					this.incConveyingTransferCounter();
				}
			}
			else if(this.movingband && event.equals(SensorEvent.FallingEdge)) {
				// Erzeuge und starte neuen ConveyingTransferObserver für diesen Fördervorgang
				this.conveyingTransferObserver = new ConveyingTransferObserver(this);
				this.conveyingTransferObserver.start();
			}
		}
		
		// Externe statische Fehlerdiagnose gegen Entnahme oder Zugabe von Fördergut bei den Sensoren
		if (!this.movingband) {
			if (event.equals(SensorEvent.FallingEdge) && this.getSuccessor() != null) {
				// Fördergut entnommen
				System.out.print("Fehler: Externer Eingriff in Einheit'" + this.name + "': "); 
				System.out.println("Fördergut manuell entfernt!");
				this.reportErrorState(ConveyorState.LostPacket);
			} else if (event.equals(SensorEvent.RisingEdge) && this.getPredecessor() != null) {
				// Fördergut hinzugefügt
				System.out.print("Fehler: Externer Eingriff in Einheit'" + this.name + "': "); 
				System.out.println("Fördergut manuell hinzugefügt!");
				this.reportErrorState(ConveyorState.UnknownPacket);
			}
		}
	}
	
	protected void incConveyingTransferCounter() {
		++this.conveyingTransferCounter;
	}

	public void decConveyingTransferCounter() {
		--this.conveyingTransferCounter;
		if (this.conveyingTransferObserver != null) this.conveyingTransferObserver.interrupt();
		this.readytopass = (this.conveyingTransferCounter > 0) ? this.routingAllowed : false;
	}
	
	public void completeConveyingTransfer() {
		this.decConveyingTransferCounter();
	}
	

	protected boolean conveyingAllowed() {
		LUnit predecessor = getPredecessor();
		LUnit successor   = getSuccessor();
		return (this.receptive   && (predecessor != null && predecessor.isReadytopass())
			|| (this.readytopass && (successor != null && successor.isReceptive())) && this.conveyingTransferCounter <= 1);
	}
	
	protected void controlConveyor() {
		if (conveyingAllowed()) {
			startBand();
		} else {
			stopBand();
		}
	}

	// TODO: Sinnvolle Reaktion auf Fehlerfälle!
	protected void reportErrorState(ConveyorState unitstate) {    
		/*
		this.conveyorstate = unitstate;
		switch(unitstate) {
			case LostPacket:
			case UnknownPacket:
				stopBand();
				this.readytopass = false;
				this.receptive	 = false;
				break;
			default:
		}
		*/
	}

}
