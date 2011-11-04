package DB1B;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SortAndAggregateDB1BData {

	// Linux File Folders
	//static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/airports_geoCoordinates.dat";
	static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output";

	//static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	//static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";

	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";
	static String output = "flightdatalist";	
	static String extension = ".csv";

	static int startyear = 2000;
	static int endyear =  2000;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;

	public static void main(String[] args){

		new SortAndAggregateDB1BData().doAll();

	}



	private void doAll()  {

		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}


		findAndSortQuarters();

		// TODO: COMBINE QUARTERS


	}


	
	private void findAndSortQuarters() {

		output = "flightdatalist_sorted";
		for(int j = startyear; j <= endyear; j++){
			for(int k =4; k<=4; k++) {

				LinkedList<DB1BDataObject> sortedDB1B = findAndSortDB1BFlights(j,k);

				LinkedList<DB1BDataObject> QuarterDB1BDataList = aggregateWithinQuarter(sortedDB1B);

				output = "flightdatalist_quarterAggregate";	
				printDB1BDataObjects(QuarterDB1BDataList,j,k);

				sortedDB1B.clear();
				QuarterDB1BDataList.clear();

				/** TODO:
				 * Wieso reicht Memory nicht aus?
				 * 
				 * Datenstruktur so verändern, dass wenn die Carrier-Typ-Strecke schon besteht nur noch der Durchschnittspreis und die Passagierzahl hinzugefügt werden. 
				 * Nicht alle Daten nochmal neu...
				 */


			}
		}

	}



	/**
	 * This function reads in the output file from FindAndCombineDb1BData and constructs an DB1BFlightDataObject
	 * which contains all the sorted data.
	 * 
	 * @returns the data in a suitable way
	 */





	public LinkedList<DB1BDataObject>  findAndSortDB1BFlights(int _year, int _quarter){

		LinkedList<DB1BDataObject> sortedDB1B = new LinkedList<DB1BDataObject>();

		String line, temp, origin, destination, airportGroup, tkGroup, opGroup, tkCarrier, itinID, marketID;

		int  year=_year, quarter=_quarter, quarterID, tkChange, passengers, marketFare, marketDistance, nonstopDistance;		

		long time = System.currentTimeMillis();
		System.out.println("Reading in the data of year "+year+" quarter "+quarter+"...");

		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_segment+File.separator+fileSegment+year+"_"+quarter+extension)));

			line = br.readLine();

			//Problem with string tokenizer (does not understand ","). So we use matcher.
			//never change the number of groups in this pattern (2)! :-) 
			Pattern csvPattern = Pattern.compile("(\"[^\"]*\")|(?<=,|^)([^,]*)(?:,|$)");
			Matcher matcher = csvPattern.matcher(line);
			int numberOfTokensInEveryLine = 0;

			while (matcher.find()) {
				++numberOfTokensInEveryLine;
			}

			line = br.readLine(); //Jump to second line to skip header.

			String match;
			String[] tokens;
			int counter;

			int i = numberOfLinesToReadTestMode;


			while( i!= 0 && line != null){
				--i;

				try{
					tokens = new String[numberOfTokensInEveryLine];
					counter = 0;
					matcher = csvPattern.matcher(line);
					while (matcher.find()) {
						match = matcher.group(1);
						if (match!=null) {
							tokens[counter++] = match;
						}
						else {
							tokens[counter++] = matcher.group(2);
						}
					}

					//Itinerary ID.
					itinID = tokens[0];
					itinID = itinID.substring(1, itinID.length()-1);
					if(debug) System.out.print("   "+itinID+" - ");

					//Market ID.
					marketID = tokens[1];
					marketID = marketID.substring(1, marketID.length()-1);
					if(debug) System.out.print(marketID+" - ");

					//Origin.
					origin = tokens[5];
					origin = origin.substring(1 , origin.length()-1);
					if(debug) System.out.print(origin+" - ");

					//Destination.
					destination = tokens[13];
					destination = destination.substring(1, destination.length()-1);
					if(debug) System.out.print(destination+" - ");

					//Airport Group.			
					airportGroup = tokens[21];
					airportGroup = airportGroup.substring(1 , airportGroup.length()-1);
					if(debug) System.out.print(airportGroup+" - ");

					//Ticketing Carrier Change.			
					temp = tokens[23];
					temp = temp.substring(0, temp.length()-3);
					tkChange = Integer.parseInt(temp);
					if(debug) System.out.print(tkChange+" - ");

					//Ticketing Carrier Group.			
					tkGroup = tokens[24];
					tkGroup = tkGroup.substring(1 , tkGroup.length()-1);
					if(debug) System.out.print(tkGroup+" - ");

					//Operating  Carrier Group.			
					opGroup = tokens[26];
					opGroup = opGroup.substring(1 , opGroup.length()-1);
					if(debug) System.out.print(opGroup+" - ");

					//Ticketing Carrier, 99 if it changed.			
					tkCarrier = tokens[28];
					tkCarrier = tkCarrier.substring(1 , tkCarrier.length()-1);
					if(debug) System.out.print(tkCarrier+" - ");

					//Passengers
					temp = tokens[31];
					temp = temp.substring(0, temp.length()-3);
					passengers= Integer.parseInt(temp);
					if(debug) System.out.print(passengers+" - ");

					//MarketFare.			
					temp = tokens[32];
					temp = temp.substring(0, temp.length()-3);
					marketFare = Integer.parseInt(temp);
					if(debug) System.out.print(marketFare+" - ");

					//MarketDistance.			
					temp = tokens[33];
					temp = temp.substring(0, temp.length()-3);
					marketDistance = Integer.parseInt(temp);
					if(debug) System.out.print(marketDistance+" - ");

					//NonstopDistance.			
					temp = tokens[36];
					temp = temp.substring(0, temp.length()-3);
					nonstopDistance = Integer.parseInt(temp);
					if(debug) System.out.print(nonstopDistance+" - ");

					// Laufende Zahl des Quartals berechnen und quarterID setzen.
					int quarterInt = Integer.valueOf(quarter);
					if(debug) System.out.print(quarterInt+" - ");
					quarterID = (year-1993)*4+quarterInt-1;	
					if(debug) System.out.print(quarterID+"\n");


					sortedDB1B.add(new DB1BDataObject(origin, destination, airportGroup, tkGroup, opGroup, tkCarrier, itinID, marketID, year, quarterID, tkChange, passengers, marketFare, marketDistance, nonstopDistance));

				} catch (NoSuchElementException e) {
					System.out.println("Failure in line : "+line);
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}

				line = br.readLine();
			}
			System.out.println("... took "+(System.currentTimeMillis()-time)/1000+" seconds.");
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		time = System.currentTimeMillis();
		Collections.sort(sortedDB1B, new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator());

		System.out.println("...and the sorting took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return sortedDB1B;
	}



	public LinkedList<DB1BDataObject> aggregateWithinQuarter(LinkedList<DB1BDataObject> _sortedDB1B){

		LinkedList<DB1BDataObject> aggregatedDB1BList = new LinkedList<DB1BDataObject>();
		Comparator<DB1BDataObject> comparate = new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator();
		DB1BDataObject baseline, compareline, printline;
		int combinedPassengers, combinedMarketFare, comparelinePassengers, comparelineMarketFare;

		// Make a copy of the sortedDB1BDataList
		//LinkedList<DB1BDataObject> flights = new LinkedList<DB1BDataObject>();
		//for (Iterator<DB1BDataObject> i = _sortedDB1B.iterator(); i.hasNext();) {
		//	flights.add(i.next());
		//}

		LinkedList<DB1BDataObject> flights = _sortedDB1B;

		long time = System.currentTimeMillis();

		baseline = flights.removeFirst();
		combinedPassengers = baseline.getPassengers();
		combinedMarketFare = baseline.getMarketFare();


		while(flights.size()>0){
			compareline = flights.removeFirst();
			comparelinePassengers = compareline.getPassengers();
			comparelineMarketFare = compareline.getMarketFare();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same

				combinedMarketFare = ((combinedMarketFare*combinedPassengers+comparelinePassengers*comparelineMarketFare)/(combinedPassengers+comparelinePassengers));
				combinedPassengers += comparelinePassengers;

			}else{
				// Base and compareline are different
				printline = new DB1BDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getTkGroup(), baseline.getOpGroup(), baseline.getTkCarrier(), baseline.getItinID(), baseline.getMarketID(), baseline.getYear(), baseline.getQuarterID(), baseline.getTkChange(), combinedPassengers, combinedMarketFare, baseline.getMarketDistance(), baseline.getNonstopDistance());
				aggregatedDB1BList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group
		printline = new DB1BDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getTkGroup(), baseline.getOpGroup(), baseline.getTkCarrier(), baseline.getItinID(), baseline.getMarketID(), baseline.getYear(), baseline.getQuarterID(), baseline.getTkChange(), combinedPassengers, combinedMarketFare, baseline.getMarketDistance(), baseline.getNonstopDistance());
		aggregatedDB1BList.add(printline);

		System.out.println("Aggregating the data within quarter took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return aggregatedDB1BList; 


	}


	public LinkedList<DB1BDataObject> aggregateWithinYear(LinkedList<DB1BDataObject> _sortedDB1B){

		LinkedList<DB1BDataObject> aggregatedDB1BList = new LinkedList<DB1BDataObject>();
		Comparator<DB1BDataObject> comparate = new TkCarrierOpCarrierYearAirportGroupComparator();
		DB1BDataObject baseline, compareline, printline;
		int combinedPassengers, combinedMarketFare, comparelinePassengers, comparelineMarketFare;

		// Make a copy of the sortedDB1BDataList
		LinkedList<DB1BDataObject> flights = new LinkedList<DB1BDataObject>();
		for (Iterator<DB1BDataObject> i = _sortedDB1B.iterator(); i.hasNext();) {
			flights.add(i.next());
		}

		long time = System.currentTimeMillis();

		baseline = flights.removeFirst();
		combinedPassengers = baseline.getPassengers();
		combinedMarketFare = baseline.getMarketFare();


		while(flights.size()>0){
			compareline = flights.removeFirst();
			comparelinePassengers = compareline.getPassengers();
			comparelineMarketFare = compareline.getMarketFare();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same

				combinedMarketFare = ((combinedMarketFare*combinedPassengers+comparelinePassengers*comparelineMarketFare)/(combinedPassengers+comparelinePassengers));
				combinedPassengers += comparelinePassengers;

			}else{
				// Base and compareline are different
				printline = new DB1BDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getTkGroup(), baseline.getOpGroup(), baseline.getTkCarrier(), baseline.getItinID(), baseline.getMarketID(), baseline.getYear(), baseline.getQuarterID(), baseline.getTkChange(), combinedPassengers, combinedMarketFare, baseline.getMarketDistance(), baseline.getNonstopDistance());

				aggregatedDB1BList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group
		printline = new DB1BDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getTkGroup(), baseline.getOpGroup(), baseline.getTkCarrier(), baseline.getItinID(), baseline.getMarketID(), baseline.getYear(), baseline.getQuarterID(), baseline.getTkChange(), combinedPassengers, combinedMarketFare, baseline.getMarketDistance(), baseline.getNonstopDistance());
		aggregatedDB1BList.add(printline);

		System.out.println("Aggregating the data within year took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return aggregatedDB1BList; 


	}




	/**
	 * This function prints a DB1BDataobject as well as a list of the included Airlines as a csv.
	 * Set year = 0 if you want to print the Airport and Airline list
	 * @returns nothing
	 */

	private void printDB1BDataObjects(LinkedList<DB1BDataObject> _sortedDB1B, int _year, int _quarter) {
		try {
			long time = System.currentTimeMillis();

			int year = _year;
			int quarter = _quarter;

			BufferedWriter bw = null;

			if (year ==0){
				bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+extension)));
			}
			else{
				bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_"+year+"_"+quarter+extension)));
			}
			DB1BDataObject ddo;

			bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!			


			LinkedList<String> foundAirlines = null;
			LinkedList<String> foundAirports = null;
			if (year ==0){
				foundAirlines = new LinkedList<String>();
				foundAirports = new LinkedList<String>();
			}


			for(ListIterator<DB1BDataObject> it = _sortedDB1B.listIterator(); it.hasNext();){
				ddo = it.next();
				bw.write(ddo.toString()+"\n");

				if (year ==0){

					// Abfrage ob Carrier schon in ArrayList, wenn nicht hinzufuegen.
					if(!foundAirlines.contains(ddo.getTkCarrier())){	
						foundAirlines.add(ddo.getTkCarrier());
					}

					// Abfrage ob Airport schon in ArrayList, wenn nicht hinzufuegen.
					if(!foundAirports.contains(ddo.getOrigin())){
						foundAirports.add(ddo.getOrigin());
					}
					if(!foundAirports.contains(ddo.getDestination())){
						foundAirports.add(ddo.getDestination());
					}
				}

			}
			bw.close();

			if (year == 0) {
				// Print Airlines-List
				BufferedWriter bwAirlines = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+"AirlineNameList"+extension)));
				bwAirlines.write("carrier;carrierName\n"); //BEWARE Fixed Header!!

				for(Iterator<String> it = foundAirlines.iterator(); it.hasNext();) {
					bwAirlines.write(it.next()+"\n");
					//System.out.println(it.next());
				}
				bwAirlines.close();

				// Print Airport-List
				BufferedWriter bwAirport = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+"AirportNameList"+extension)));
				bwAirlines.write("carrier;carrierName\n"); //BEWARE Fixed Header!!

				for(Iterator<String> it = foundAirlines.iterator(); it.hasNext();) {
					bwAirlines.write(it.next()+"\n");
					//System.out.println(it.next());
				}
				bwAirport.close();
			}

			System.out.println("...printing all data in one file - file name: "+output + " took "+(System.currentTimeMillis()-time)/1000+" seconds.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	private void printDB1BDataObjectsbyYear(LinkedList<DB1BDataObject> DB1BData) {

		//sort the list by year/origin/destination/carrier
		Collections.sort(DB1BData, new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator() ) ;


		DB1BDataObject currentDDO;

		int year, currentAggregatingYear = DB1BData.get(0).getYear();

		try {



			ListIterator<DB1BDataObject> iterator = DB1BData.listIterator();

			//initialize folder structure for the starting year:
			File saveFolder = new File(dirURL_segmentSave+File.separator+currentAggregatingYear);
			if(!saveFolder.exists()){
				saveFolder.mkdirs();		
			}

			//initialize output file
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentAggregatingYear+extension)));
			bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!			

			while(iterator.hasNext()){
				currentDDO = iterator.next();
				year = currentDDO.getYear();

				if(year != currentAggregatingYear){
					bw.close();
					currentAggregatingYear = year;
					//initialize folder structure for the starting year:
					saveFolder = new File(dirURL_segmentSave+File.separator+year);
					if(!saveFolder.exists()){
						saveFolder.mkdirs();		
					}

					//initialize output file
					System.out.println(year);
					bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+year+extension)));
					bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!			
				}
				bw.write(currentDDO.toString()+"\n");

			}
			bw.close();
			System.out.println("      print by year - file name: " + output);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	


	/**
	 * Creates a subfolder for every carrier and prints the flight data in a single file for every year
	 * @param DB1BData
	 */
	private void printDB1BDataObjectsbyTkCarrier(LinkedList<DB1BDataObject> DB1BData) {

		//sort the list by year/origin/destination/carrier
		Collections.sort(DB1BData, new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator() ) ;


		DB1BDataObject currentDDO;

		int year, currentAggregatingYear = DB1BData.get(0).getYear();
		String carrier, currentCarrier = DB1BData.get(0).getTkCarrier();

		try {



			ListIterator<DB1BDataObject> iterator = DB1BData.listIterator();

			//initialize folder structure for the starting carrier:
			File saveFolder = new File(dirURL_segmentSave+File.separator+"carrier"+File.separator+"carrier_"+currentCarrier);
			if(!saveFolder.exists()){
				saveFolder.mkdirs();		
			}

			//initialize output file for the starting year
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_"+currentAggregatingYear+extension)));
			bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!	

			//initialize output file for all years
			BufferedWriter bwAll = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_allYears"+extension)));
			bwAll.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!

			while(iterator.hasNext()){
				currentDDO = iterator.next();
				carrier = currentDDO.getTkCarrier();
				year = currentDDO.getYear();

				if(carrier.compareTo(currentCarrier) != 0){
					bw.close();
					bwAll.close();
					currentCarrier = carrier;
					currentAggregatingYear = year;
					//initialize folder structure for the carrier:
					saveFolder = new File(dirURL_segmentSave+File.separator+"carrier"+File.separator+"carrier_"+currentCarrier);
					if(!saveFolder.exists()){
						saveFolder.mkdirs();		
					}

					//System.out.println(currentCarrier+"_"+currentAggregatingYear);
					//initialize output file by year
					bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_"+currentAggregatingYear+extension)));
					bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n\n"); //BEWARE Fixed Header!!

					//initialize output file for all years
					bwAll = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_allYears"+extension)));
					bwAll.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!

				}

				if(year != currentAggregatingYear){
					bw.close();
					currentAggregatingYear = year;

					//initialize output file
					bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_"+currentAggregatingYear+extension)));
					bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!
				}


				bw.write(currentDDO.toString()+"\n");
				bwAll.write(currentDDO.toString()+"\n");

			}
			bw.close();
			System.out.println("      printed by year - file name: " + output);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




}
