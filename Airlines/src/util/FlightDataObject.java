package util;

import java.util.Comparator;

public class FlightDataObject {

	String carrier, carrierName, origin, destination;

	int year, month, runningMonth, aircraftGroup;

	int passengers;
	
	public FlightDataObject(String _carrier, String _carrierName, int _year, int _month, int _runningMonth, String _origin, String _destination, int _passengers, int _aircraftGroup){
		carrier = _carrier;
		carrierName = _carrierName;
		year = _year;
		month = _month;
		runningMonth = _runningMonth;
		origin = _origin;
		destination = _destination;
		passengers = _passengers;
		aircraftGroup = _aircraftGroup;
		
	}
	
	
	public String toString() {
		return  origin+ ";" + destination + ";" + carrier +";" + year + ";" + month + ";" + passengers + ";" + aircraftGroup;
	}

	public String getCarrier() {
		return carrier;
	}

	public int getAircraftGroup() {
		return aircraftGroup;
	}
	
	public String getCarrierName() {
		return carrierName;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}

	public int getPassengers() {
		return passengers;
	}

	public int getRunningMonth(){
		return runningMonth;
	}

}

class OriginComparator implements Comparator<FlightDataObject>{	
	public int compare(FlightDataObject arg0, FlightDataObject arg1) {
		String firstOrigin = arg0.getOrigin();
		String secondOrigin = arg1.getOrigin();

		return firstOrigin.compareTo(secondOrigin);		
	}	
}

class DestinationComparator implements Comparator<FlightDataObject>{	
	public int compare(FlightDataObject arg0, FlightDataObject arg1) {
		String firstDest = arg0.getDestination();
		String secondDest = arg1.getDestination();

		return firstDest.compareTo(secondDest);		
	}	
}
