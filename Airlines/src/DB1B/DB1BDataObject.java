package DB1B;

public class DB1BDataObject {

	String  origin, destination, airportGroup, tkGroup, opGroup, tkCarrier, itinID, marketID;

	int year, quarterID, tkChange, passengers, marketFare, marketDistance, nonstopDistance;


	public DB1BDataObject(String _origin, String _destination, String _airportGroup, String _tkGroup, String _opGroup, String _tkCarrier, String _itinID, String _marketID, int _year, int _quarterID, int _tkChange, int _passengers, int _marketFare, int _marketDistance, int _nonstopDistance){

		origin = _origin;
		destination = _destination;
		airportGroup = _airportGroup;
		tkGroup = _tkGroup;
		opGroup = _opGroup;
		tkCarrier = _tkCarrier;
		itinID = _itinID;
		marketID = _marketID;
		
		year = _year;
		quarterID = _quarterID;
		tkChange = _tkChange;
		passengers = _passengers;
		marketFare = _marketFare;
		marketDistance = _marketDistance; 
		nonstopDistance = _nonstopDistance;

	}

	public String toString() {
		return origin+ ";" +  destination + ";" + airportGroup + ";" + tkGroup + ";" + opGroup + ";" + tkCarrier + ";" + itinID + ";" + marketID + ";" + year + ";" + quarterID + ";" + tkChange + ";" + passengers + ";" + marketFare + ";" + marketDistance + ";" + nonstopDistance;
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

	public String getTkGroup() {
		return tkGroup;
	}

	public String getOpGroup() {
		return opGroup;
	}

	public String getTkCarrier() {
		return tkCarrier;
	}

	public String getItinID() {
		return itinID;
	}

	public String getMarketID() {
		return marketID;
	}
	
	public int getYear() {
		return year;
	}

	public int getQuarterID() {
		return quarterID;
	}

	public int getTkChange() {
		return tkChange;
	}

	public int getPassengers() {
		return passengers;
	}

	public int getMarketFare() {
		return marketFare;
	}


	public int getMarketDistance() {
		return marketDistance;
	}

	public int getNonstopDistance() {
		return nonstopDistance;
	}

}
