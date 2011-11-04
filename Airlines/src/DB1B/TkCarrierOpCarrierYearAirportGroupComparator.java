package DB1B;

import java.util.Comparator;


public class TkCarrierOpCarrierYearAirportGroupComparator implements Comparator<DB1BDataObject> {
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

				//both carriers are the same. We test for the year:

				int firstYear = arg0.getYear();
				int secondYear = arg1.getYear();

				if(firstYear != secondYear){

					return firstYear-secondYear;

				}else{
					// ticketing carrier, operating carrier and year are the same. We test for the airportGroup:		


					String firstAirportGroup = arg0.getAirportGroup();
					String secondAirportGroup = arg1.getAirportGroup();

					comp = firstAirportGroup.compareTo(secondAirportGroup);

						return comp; 
				}

			}
		}
	}
}



