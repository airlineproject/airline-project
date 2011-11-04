package DB1B;

import java.util.Comparator;


public class TkCarrierOpCarrierYearQuarterIDAirportGroupComparator implements Comparator<DB1BDataObject> {
	public int compare(DB1BDataObject arg0, DB1BDataObject arg1) {

		// We test for the ticketing carrier:

		String firstTkGroup = arg0.getTkGroup();
		String secondTkGroup = arg1.getTkGroup();

		int comp = firstTkGroup.compareTo(secondTkGroup);
		if(comp != 0){
			return comp;
		}else{

			// Ticketing carrier is the same. We test for the Operating Carrier:

			String firstOpGroup = arg0.getOpGroup();
			String secondOpGroup = arg1.getOpGroup();

			comp = firstOpGroup.compareTo(secondOpGroup);
			if(comp != 0){
				return comp;
			}else{

				//both carriers are the same. We test for the running quarter:

				int firstQuarterID = arg0.getQuarterID();
				int secondQuarterID = arg1.getQuarterID();

				if(firstQuarterID != secondQuarterID){

					return firstQuarterID-secondQuarterID;

				}else{
					// ticketing carrier, operating carrier and running quarter are the same. We test for the airportGroup:		


					String firstAirportGroup = arg0.getAirportGroup();
					String secondAirportGroup = arg1.getAirportGroup();

					comp = firstAirportGroup.compareTo(secondAirportGroup);
						return comp; 
				}

			}
		}
	}
}



