package floor;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.time.temporal.ChronoUnit;

/**
 * Represents an entry in the CSV file. Contains information for the action an
 * elevator needs to perform.
 */
public class ElevatorRequest implements Comparable<ElevatorRequest> {

	private CSVParser.ElevatorFault fault;

	/**
	 * The requested direction.
	 */
	public enum ButtonDirection {
		/** The requested direction is up. */
		UP,
		/** The requested direction is down. */
		DOWN,
		/** No requested direction. */
		NONE
	}

	/** The requested direction. Either up or down. */
	private ButtonDirection buttonDirection;

	/** The button ID. */
	private int buttonId;

	/** The requested floor number. */
	private int floorNumber;

	/** The time of the request. */
	private LocalTime currTime = LocalTime.now();
	/**
	 * Track whether the elevator request has been loaded with the passenger
	 */
	private boolean loaded = false;
	/** If request has been processed */
	private boolean processed = false;
	/** When the elevator request was assigned by the elevator (for stats) */
	private LocalTime startTime;

	/**
	 * Create a new elevator request.
	 * @param buttonDirection The requested direction.
	 * @param floorNumber The current floor.
	 * @param buttonId The button ID - The button pressed inside the elevator (i.e. the designation floor)
	 * @param currTime The time of the request.
	 */
	public ElevatorRequest( LocalTime currTime, int floorNumber, ButtonDirection buttonDirection,  int buttonId) {
		this.currTime = currTime;
		this.floorNumber = floorNumber;
		this.buttonDirection = buttonDirection;
		this.buttonId = buttonId;
	}

	/**
	 * Constructor to deserialize from byte array
	 */
	public ElevatorRequest(byte[] data) {
		String dataString = new String(data, StandardCharsets.UTF_8).trim(); // Also trim the whole string
		String[] parts = dataString.split(";");

		if (parts.length >= 6) {
			this.currTime = LocalTime.parse(parts[0].trim(), DateTimeFormatter.ISO_LOCAL_TIME);
			this.buttonDirection = ButtonDirection.valueOf(parts[1].trim());
			this.floorNumber = Integer.parseInt(parts[2].trim());
			this.buttonId = Integer.parseInt(parts[3].trim());
			this.loaded = parts[4].trim().startsWith("1");
			this.processed = parts[5].trim().startsWith("1");
			if (parts.length >= 7) { // Check if there is a seventh part for fault
				this.fault = CSVParser.ElevatorFault.fromString(parts[6].trim());
			}
		} else {
			throw new IllegalArgumentException("Invalid data for ElevatorRequest");
		}
	}

	public void addFault(CSVParser.ElevatorFault fault) {
		this.fault = fault;
	}

	public String getFault() {
		if(this.fault != null) {
			return this.fault.toString();
		}
		return "NO_FAULT"; //in case there is no fault set in the csv file
	}

	public void removeFault() {this.fault = CSVParser.ElevatorFault.NO_FAULT;}

	/**
	 * Get the processing status.
	 * @return The requested direction.
	 */
	public boolean isProcessed() {
		return this.processed;
	}

	/**
	 * Get the requested direction.
	 * @return The requested direction.
	 */
	public ButtonDirection getButtonDirection() {
		return this.buttonDirection;
	}

	/**
	 * Get the floor number.
	 * @return The floor number.
	 */
	public int getFloorNumber() {
		return this.floorNumber;
	}

	/**
	 * Get the button ID.
	 * @return The button ID.
	 */
	public int getButtonId() {
		return this.buttonId;
	}

	/**
	 * Set the button ID.
	 * @param newButtonId The button ID.
	 */
	public void setButtonId(int newButtonId) {
		this.buttonId = newButtonId;
	}

	/**
	 * Get the time of the request.
	 * @return The time of the request.
	 */
	public LocalTime getTime() {
		return this.currTime;
	}

	/**
	 * Set loaded to true
	 */
	public void setLoaded() {
		loaded = true;
	}

	public void setProcessed() {
		processed = true;
	}

	/**
	 * Return loaded
	 * @return if the elevatorRequest is loaded
	 */
	public boolean isLoaded() {return this.loaded;}

	/**
	 * Get the start time
	 * @return startTime
	 */
	public LocalTime getStartTime() {
		return startTime;
	}

	/**
	 * Set the start time
	 * @param start startTime
	 */
	public void setStartTime(LocalTime start) {
		this.startTime = start;
	}

	/**
	 * Compare ElevatorRequests by floor number
	 * @param e the ElevatorRequest to be compared.
	 * @return result of comparison
	 */
	@Override
	public int compareTo(ElevatorRequest e) {
		return Integer.compare(this.getFloorNumber(), e.getFloorNumber());
	}

	/**
	 * Compare ElevatorRequests by time.
	 * @param e ehe ElevatorRequest to be compared.
	 * @return result of comparison
	 */
	public int compareByTime(ElevatorRequest e) {
		return currTime.compareTo(e.getTime());
	}

	/**
	 * Wait until ElevatorRequest time is reached. Baseline is used for comparison
	 * to avoid having to set software clock time.
	 * @param baseline The baseline time.
	 */
	public void waitForTime(LocalTime baseline) {
		long time = 0;
		time = baseline.until(currTime, ChronoUnit.MILLIS);
		if (time <= 0) {
			return;
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

	/**
	 * Returns a string representation of an elevator request
	 */
	@Override
	public String toString() {
		return "ElevatorRequest{" +
				"buttonDirection=" + buttonDirection +
				", buttonId=" + buttonId +
				", floorNumber=" + floorNumber +
				", currTime=" + currTime +
				", loaded=" + loaded +
				", processed=" + processed +
				", fault=" + (fault != null ? fault.toString() : "None") +
				'}';
	}


	/**
	 * Returns a bytes representation of an elevator request for UDP transport
	 * It first builds a string of the following format:
	 * "requestTime;buttonDirection;floorNumber;buttonID;1"
	 * Where requestTime is the time of the request
	 * buttonDirection is either UP or DOWN
	 * floorNumber is the current floor
	 * buttonID is the destination floor
	 */
	public byte[] getBytes() {
		// Use a delimiter to separate the properties in the string
		String delimiter = ";";

		// Serialize properties to string
		StringBuilder sb = new StringBuilder();
		// Format LocalTime to a string using a formatter
		DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
		sb.append(this.currTime.format(timeFormatter));
		sb.append(delimiter);
		sb.append(this.buttonDirection.name());
		sb.append(delimiter);
		sb.append(this.floorNumber);
		sb.append(delimiter);
		sb.append(this.buttonId);
		sb.append(delimiter);
		sb.append(this.loaded ? "1" : "0"); // Represent boolean as 1 (true) or 0 (false)
		sb.append(delimiter);
		sb.append(this.processed ? "1" : "0"); // Represent boolean as 1 (true) or 0 (false)
		if (this.fault != null) {
			sb.append(delimiter);
			sb.append(this.fault); // TODO: verify that this prints what we want
		}
		// Convert the serialized string to bytes
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}
}
