package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class OriginDestCarrierYearComparator implements Comparator<FlightDataObject>{	
	public int compare(FlightDataObject arg0, FlightDataObject arg1) {
		String firstOrig = arg0.getOrigin();
		String secondOrig = arg1.getOrigin();

		int comp = firstOrig.compareTo(secondOrig);
		if(comp != 0){
			return comp; 
		}else{
			//both origins are the same. We test for the destination:
			String firstDestination = arg0.getDestination();
			String secondDestination = arg1.getDestination();
			
			comp = firstDestination.compareTo(secondDestination);
			if(comp != 0){
				return comp;
			}else{
				//both, origins and destinations are the same. We test for the carrier:
				String firstCarrier = arg0.getCarrier();
				String secondCarrier = arg1.getCarrier();
				
				comp = firstCarrier.compareTo(secondCarrier);
				if(comp != 0)
					return comp;
				else{
					//all, origin, dest and carrier are the same. Sort by year:
					int firstYear = arg0.getYear();
					int secondYear= arg1.getYear();
					
					
					return firstYear-secondYear;
					
						
				}
				
			}
	
		}
	
	
	}	

}




