package jeremy_replica.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jeremy_replica.enums.City;
import jeremy_replica.enums.FlightClass;
import jeremy_replica.global.Constants;

public class FlightRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private City origin;
	private City destination;
	private Date flightDate;
	private HashMap<FlightClass, FlightSeats> flightClasses;
	private ReadWriteLock originLock;
	private ReadWriteLock destinationLock;
	private ReadWriteLock flightDateLock;
	
	public FlightRecord(Integer id, City origin, City destination, Date flightDate,
			HashMap<FlightClass, FlightSeats> flightClasses) {
		super();
		this.id = id;
		this.origin = origin;
		this.destination = destination;
		this.flightDate = flightDate;
		this.flightClasses = flightClasses;
		this.originLock = new ReentrantReadWriteLock(true);
		this.destinationLock = new ReentrantReadWriteLock(true);
		this.flightDateLock = new ReentrantReadWriteLock(true);
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public City getOrigin() {
		this.originLock.readLock().lock();
		try {
			return origin;
		} finally {
			this.originLock.readLock().unlock();
		}
	}

	public void setOrigin(City origin) {
		this.originLock.writeLock().lock();
		try {
			this.origin = origin;
		} finally {
			this.originLock.writeLock().unlock();
		}
	}

	public City getDestination() {
		this.destinationLock.readLock().lock();
		try {
			return destination;
		} finally {
			this.destinationLock.readLock().unlock();
		}
	}

	public void setDestination(City destination) {
		this.destinationLock.writeLock().lock();
		try {
			this.destination = destination;
		} finally {
			this.destinationLock.writeLock().unlock();
		}
	}

	public Date getFlightDate() {
		this.flightDateLock.readLock().lock();
		try {
			return flightDate;
		} finally {
			this.flightDateLock.readLock().unlock();
		}
	}

	public void setFlightDate(Date flightDate) {
		this.flightDateLock.writeLock().lock();
		try {
			this.flightDate = flightDate;
		} finally {
			this.flightDateLock.writeLock().unlock();
		}
	}

	public HashMap<FlightClass, FlightSeats> getFlightClasses() {
		return flightClasses;
	}

	public void setFlightClasses(HashMap<FlightClass, FlightSeats> flightClasses) {
		this.flightClasses = flightClasses;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id + Constants.DELIMITER + origin + Constants.DELIMITER + destination + Constants.DELIMITER
				+ new SimpleDateFormat(Constants.DATE_FORMAT).format(flightDate));
		sb.append(Constants.DELIMITER + FlightClass.FIRST + Constants.DELIMITER + flightClasses.get(FlightClass.FIRST));
		sb.append(Constants.DELIMITER + FlightClass.BUSINESS + Constants.DELIMITER + flightClasses.get(FlightClass.BUSINESS));
		sb.append(Constants.DELIMITER + FlightClass.ECONOMY + Constants.DELIMITER + flightClasses.get(FlightClass.ECONOMY));
		return sb.toString();
	}
}
