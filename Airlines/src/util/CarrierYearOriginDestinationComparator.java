package util;

import java.util.Comparator;

public class CarrierYearOriginDestinationComparator implements Comparator<FlightDataObject> {
	public int compare(FlightDataObject arg0, FlightDataObject arg1) {

		String firstCarrier = arg0.getCarrier();
		String secondCarrier = arg1.getCarrier();

		//We test for the carrier:
		int comp = firstCarrier.compareTo(secondCarrier);
		if(comp != 0){
			return comp;
		}else{
			//carrier is the same. We test for running month:

			int firstRMonth = arg0.getRunningMonth();
			int secondRMonth = arg1.getRunningMonth();

			if(firstRMonth != secondRMonth){

				return firstRMonth-secondRMonth;
			}else{
				//both carriers and running month are the same. We test for origin:
				String firstOrig = arg0.getOrigin();
				String secondOrig = arg1.getOrigin();

				comp = firstOrig.compareTo(secondOrig);
				if(comp != 0){
					return comp; 
				}else{
					//both carrier, running month and origins are the same. We test for the destination:
					String firstDestination = arg0.getDestination();
					String secondDestination = arg1.getDestination();

					comp = firstDestination.compareTo(secondDestination);

					return comp;

				}

			}
		}
	}
}


