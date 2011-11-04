package DB1B;

import java.util.Comparator;

public class NonstopDataObject {

	String  origin, destination, airportGroup;

	int year, quarterID, passengers;


	public NonstopDataObject(String _origin, String _destination, String _airportGroup, int _year, int _quarterID, int _passengers){

		origin = _origin;
		destination = _destination;
		airportGroup = _airportGroup;
		
		year = _year;
		quarterID = _quarterID;
		passengers = _passengers;

	}

	public String toString() {
		return origin+ ";" +  destination + ";" + airportGroup +";" + year + ";" + quarterID + ";" + passengers;
	}
	
	
	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}

	public String getAirportGroup() {
		return airportGroup;
	}
	
	public int getYear() {
		return year;
	}

	public int getQuarterID() {
		return quarterID;
	}

	public int getPassengers() {
		return passengers;
	}

}

class AirportGroupComparator implements Comparator<NonstopDataObject>{	
	public int compare(NonstopDataObject arg0, NonstopDataObject arg1) {
		String firstAirportGroup = arg0.getAirportGroup();
		String secondAirportGroup = arg1.getAirportGroup();

		return firstAirportGroup.compareTo(secondAirportGroup);		
	}

}
