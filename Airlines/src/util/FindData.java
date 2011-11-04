package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.portable.ValueBase;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class FindData {

	static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/airports_geoCoordinates.dat";
	static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/T100 Segment";
	String fileSegment = "T100_SEGMENT_ALL_CARRIER_";

	static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/T100 Segment/Output";


	static String output = "flightdatalist";
	static String extension = ".csv";

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;


	static int startyear = 2010;
	static int endyear =  2010;


	public FindData(){

	}

	public FindData(int start, int end){
		startyear = start;
		endyear = end;
	}

	public FindData(int start, int end, String outputDir){
		startyear = start;
		endyear = end;
		dirURL_segmentSave = outputDir;
	}



	public static void main(String[] args){

		new FindData().doAll();

	}



	private void doAll() {
		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}

		//main data structure that stores at [x][y] the flights
		//of carrier x in month y
		AirFlightDataStructure airFlights = findFlights();

		// Find the geocordinates for all airports and only print the ones with flight data
		THashMap<String, String> airportGeoSet = findCoordinates();
		printAirportGeoDataAsCSV(airportGeoSet, airFlights);

		// save to file (needs the airportGeoSet
		printflightDataAsCSV(airFlights);
		//System.out.println(carrierCounter);

	}

	private void doAll2(){
		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}

		LinkedList<FlightDataObject> sortedAirFlights = findAndSortFlights();
		output = "flightdatalist_sorted";
		printFlightDataObjects(sortedAirFlights);


		//MONTH AGGREGATE - clean data and print cleanFlightData
		System.out.println("MONTH AGGREGATE");

		LinkedList<FlightDataObject> cleanFlightDataList = aggregateWithinMonth(sortedAirFlights);

		output = "flightdatalist_monthAggregate";	
		printFlightDataObjects(cleanFlightDataList);

		// also save it as a single file for every year in an own folder for every year
		output = "flightdatalist_monthAggregate";
		printFlightDataObjectsbyYear(cleanFlightDataList);


		// also save it as a single file for year in an own folder for every carrier
		output = "flightdatalist_monthAggregate";
		printFlightDataObjectsbyCarrier(cleanFlightDataList);



		//YEAR AGGREGATE - aggregate data over year and print 
		System.out.println("YEAR AGGREGATE");

		LinkedList<FlightDataObject> cleanFlightDataYearList = aggregateWithinYear(cleanFlightDataList);

		output = "flightdatalist_yearAggregate";	
		printFlightDataObjects(cleanFlightDataYearList);

		// also save it as a single file for every year in an own folder for every year
		output = "flightdatalist_yearAggregate";	
		printFlightDataObjectsbyYear(cleanFlightDataYearList);

		//			// also save it as a single file for year in an own folder for every carrier		
		output = "flightdatalist_yearAggregate";	
		printFlightDataObjectsbyCarrier(cleanFlightDataYearList);
		//		
	}



	public String jump(int steps, StringTokenizer st) {
		// uses a tokenizer to jump steps columns to the right.
		// BEWARE string has to be tokenized externally.

		for(; steps>1; --steps){
			st.nextToken();
		}
		return st.nextToken();
	}





	public THashMap<String, String>  findCoordinates() {

		THashMap<String, String> airportGeoSet = new THashMap<String, String>() ; 
		// extract all characteristics of certain variables

		System.out.println("Finding coordinates from Airports");

		StringTokenizer st;
		String airportCode, airportCity, airportName, airportCountry, longitude, latitude;
		String temp = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_geo)));
			System.out.println("scan "+dirURL_geo);


			String line = br.readLine();

			//for(int i = numberOfLinesToReadTestMode; i > 0; --i){
			while(line != null){
				st = new StringTokenizer(line, ",");

				//jump to airport name and removes "".
				temp = jump(2, st);
				airportName = temp.substring(1, temp.length()-1);
				//System.out.print("   "+airportName);
				//jump to airport city and removes "".
				temp = jump(1, st);
				airportCity = temp.substring(1, temp.length()-1);
				//System.out.print("   "+airportCity);
				//jump to airport country and removes "".
				temp = jump(1, st);
				airportCountry = temp.substring(1, temp.length()-1);
				//System.out.print("   "+airportCountry);
				//jump to airport code and removes "".
				temp = jump(1, st);
				airportCode = temp.substring(1, temp.length()-1);
				if(debug) System.out.print("   "+airportCode);

				//jump to latitude and removes "".
				latitude = jump(2, st);
				if(debug) System.out.print("   "+latitude);

				//jump to longitude and removes "".
				longitude = jump(1, st);
				if(debug) System.out.println("   "+longitude+"\n");

				//put airportCode as key and lat,long as value in airportGeoSet, hashmap. 
				airportGeoSet.put(airportCode, new String(airportName+";"+airportCity+";"+airportCountry+";"+latitude+";"+longitude));
				if(debug) System.out.println(airportGeoSet);

				//System.out.println(airportCode+";"+airportGeoSet.get(airportCode));


				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return airportGeoSet;
	}



	/**
	 * This function reads in the raw data and constructs an AirFlightDataStructure object
	 * which contains all the necessary data.
	 * 
	 * @returns the data in a suitable way
	 */
	public AirFlightDataStructure  findFlights(){

		// LinkedListArray FlightDataList erstellen	
		int maxCarrierID = 800;
		int maxMonthID = 253;

		AirFlightDataStructure airFlights = new AirFlightDataStructure(maxCarrierID, maxMonthID);

		for(int j = startyear; j <= endyear; j++){

			//open all flight data files and extract.
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_segment+File.separator+fileSegment+j+extension)));
				System.out.println("scan segment "+j);

				String passengers, carrier, origin, destination, aircraftGroup, month, carrierName;

				String line = br.readLine();

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



					try {
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

						//				System.out.println("This line contained "+counter+" tokens");				
						//				for(String token: tokens) System.out.print(token+" ### ");
						//				System.out.println(line);

						//jump to passengers.
						passengers = tokens[4];
						double passengersInt = Double.parseDouble(passengers);
						//						System.out.println("   "+passengers);

						//jump to carrier.
						carrier = tokens[10];
						//System.out.println("   "+carrier);
						carrier = carrier.substring(1, carrier.length()-1);

						//jump to carrier Name.
						carrierName = tokens[12];
						//System.out.println("   "+carrierName);
						carrierName = carrierName.substring(1, carrierName.length()-1);

						//jump to origin.
						origin = tokens[19];
						origin = origin.substring(1, origin.length()-1);
						//					System.out.println("   "+origin);

						//jump to destination.
						destination = tokens[28];
						destination = destination.substring(1, destination.length()-1);
						//					System.out.println("   "+destination);

						//jump to aircraftgroup.
						aircraftGroup = tokens[37];
						//System.out.println("   "+aircraftGroup+"\n");					


						//jump to month.
						month = tokens[42];
						//					System.out.println("   "+month+"\n");

						//System.out.println("   "+j+" "+month+" "+origin+" "+destination+" "+carrier);


						// Abfrage ob Carrier schon in Hashmap und carrierID setzen.
						String id = airFlights.getIDOfCarrier(carrier);

						int carrierID = Integer.valueOf(id);

						// Laufende Zahl des Monats berechnen und monthID setzen.
						//					int yearInt = Integer.valueOf(year);
						int monthInt = Integer.valueOf(month);
						int monthID = (j-1990)*12+monthInt-1;

						// String an der Stelle [carrierID][monthID] in das LinkedListArray schreiben.
						String data = new String(carrier+";"+carrierName+";"+j+";"+month+";"+origin+";"+destination+";"+passengers+";"+aircraftGroup);
						//					System.out.println(data);

						//Erstelle Hashmap für Carrier und Carrier Name Liste.


						////////////////////////////////////////////
						// Flüge mit Null Passagieren rauswerfen!!!!
						////////////////////////////////////////////
						if(passengersInt > 0){
							airFlights.add(data, carrierID, monthID, origin, destination);
						}


					} catch (NoSuchElementException e) {
						System.out.println("Failure in line : "+line);
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}


					line = br.readLine();
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//System.out.println(carrierCounter);
		//		return flightDataList;
		return airFlights;


	}



	/**
	 * This function reads in the output file from findFlights and constructs an AirFlightDataStructure object
	 * which contains all the sorted data.
	 * 
	 * @returns the data in a suitable way
	 */
	public LinkedList<FlightDataObject>  findAndSortFlights(){

		LinkedList<FlightDataObject> flightDataList = new LinkedList<FlightDataObject>();
		int monthID, monthInt, yearInt, aircraftGroup;
		String passengers, year, carrier, carrierName, origin, destination, month, temp, line = null;				

		long time = System.currentTimeMillis();

		try {

			long time2 = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_segmentSave+File.separator+output+extension)));

			line = br.readLine();

			line = br.readLine(); //Jump to second line to skip header.


			int i = numberOfLinesToReadTestMode;
			StringTokenizer st;

			while( i!= 0 && line != null){
				--i;


				try {
					st = new StringTokenizer(line, ";");

					//System.out.println(line);
					carrier = st.nextToken();
					carrierName = st.nextToken();
					year = st.nextToken();
					month = st.nextToken();
					origin = st.nextToken();
					//System.out.println(origin);
					destination = st.nextToken();
					//System.out.println(destination);
					passengers = st.nextToken();
					//System.out.println(passengers);
					temp = st.nextToken();
					aircraftGroup =  Integer.parseInt(temp);
					//System.out.println(aircraftGroup);



					// Laufende Zahl des Monats berechnen und monthID setzen.
					monthInt = Integer.valueOf(month);
					yearInt = Integer.valueOf(year);
					monthID = (yearInt-1990)*12+monthInt-1;

					flightDataList.add(new FlightDataObject(carrier, carrierName, yearInt, monthInt, monthID, origin, destination, (int)Double.parseDouble(passengers), aircraftGroup));
				} catch (NoSuchElementException e) {
					System.out.println("Failure in line : "+line);
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}



				line = br.readLine();
			}
			System.out.println("Reading the data took "+(System.currentTimeMillis()-time2)/1000+" seconds.");
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		time = System.currentTimeMillis();



		Collections.sort(flightDataList, new OriginDestCarrierYearMonthComparator());


		System.out.println("...and the sorting took "+(System.currentTimeMillis()-time)/1000+" seconds.");


		return flightDataList;


	}


	/**
	 * Aggregates the passenger numbers, such that multiple Carrier-Origin-Dest observations per month now collapse to one  observation.
	 * BEWARE: ONLY WORKS WITH A LIST THAT HAS BEEN SORTED BY OriginDestCarrierYearMonthComparator
	 * 
	 * @param airFlights (sorted list of all Flights - raw)
	 * @return A cleaned list with only one Carrier-Origin-Dest observation per month
	 */
	public LinkedList<FlightDataObject> aggregateWithinMonth(LinkedList<FlightDataObject> _airFlights){

		LinkedList<FlightDataObject> cleanFlightDataList = new LinkedList<FlightDataObject>();
		Comparator<FlightDataObject> comparate = new OriginDestCarrierYearMonthComparator();
		FlightDataObject baseline, compareline, printline;
		int combinedPassengers,comparelinePassengers;

		// Make a copy of the cleanFlightDataList
		LinkedList<FlightDataObject> airFlights = new LinkedList<FlightDataObject>();
		for (Iterator<FlightDataObject> i = _airFlights.iterator(); i.hasNext();) {
			airFlights.add(i.next());
		}

		long time3 = System.currentTimeMillis();

		baseline = airFlights.removeFirst();
		combinedPassengers = baseline.getPassengers();


		while(airFlights.size()>0){
			compareline = airFlights.removeFirst();
			comparelinePassengers = compareline.getPassengers();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same
				combinedPassengers += comparelinePassengers;
				//System.out.println("You failed!");
			}else{
				// Base and compareline are different
				printline = new FlightDataObject(baseline.getCarrier(),baseline.getCarrierName(), baseline.getYear(), baseline.getMonth(), baseline.getRunningMonth(), 
						baseline.getOrigin(), baseline.getDestination(), combinedPassengers, baseline.getAircraftGroup());

				cleanFlightDataList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group
		printline = new FlightDataObject(baseline.getCarrier(), baseline.getCarrierName(), baseline.getYear(), baseline.getMonth(), baseline.getRunningMonth(), 
				baseline.getOrigin(), baseline.getDestination(), combinedPassengers, baseline.getAircraftGroup());
		cleanFlightDataList.add(printline);

		System.out.println("Aggregating the data within month took "+(System.currentTimeMillis()-time3)/1000+" seconds.");

		return cleanFlightDataList; 


	}



	/**
	 * Aggregates the passenger numbers, such that multiple Carrier-Origin-Dest observations per year now collapse to one  observation.
	 * BEWARE: ONLY WORKS WITH A LIST THAT HAS BEEN SORTED BY OriginDestCarrierYearMonthComparator
	 * 
	 * @param airFlights (sorted list of all Flights - raw)
	 * @return A list with only one Carrier-Origin-Dest observation per year
	 */
	public LinkedList<FlightDataObject> aggregateWithinYear(LinkedList<FlightDataObject> _cleanFlightDataList){

		LinkedList<FlightDataObject> cleanFlightDataYearList = new LinkedList<FlightDataObject>();
		Comparator<FlightDataObject> comparate = new OriginDestCarrierYearComparator();
		FlightDataObject baseline, compareline, printline;
		int combinedPassengers,comparelinePassengers;

		Collections.sort(_cleanFlightDataList, comparate);



		ListIterator<FlightDataObject> iterator = _cleanFlightDataList.listIterator();

		long time3 = System.currentTimeMillis();

		baseline = iterator.next();
		combinedPassengers = baseline.getPassengers();

		while(iterator.hasNext()){
			compareline = iterator.next();
			comparelinePassengers = compareline.getPassengers();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same
				combinedPassengers += comparelinePassengers;
				//System.out.println("You failed!");
			}else{
				// Base and compareline are different
				printline = new FlightDataObject(baseline.getCarrier(), baseline.getCarrierName(), baseline.getYear(), baseline.getMonth(), baseline.getRunningMonth(), 
						baseline.getOrigin(), baseline.getDestination(), combinedPassengers, baseline.getAircraftGroup());
				cleanFlightDataYearList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group
		printline = new FlightDataObject(baseline.getCarrier(), baseline.getCarrierName(), baseline.getYear(), baseline.getMonth(), baseline.getRunningMonth(), 
				baseline.getOrigin(), baseline.getDestination(), combinedPassengers, baseline.getAircraftGroup());
		cleanFlightDataYearList.add(printline);

		System.out.println("Aggregating the data within year took "+(System.currentTimeMillis()-time3)/1000+" seconds.");

		return cleanFlightDataYearList; 


	}



	public void printflightDataAsCSV(AirFlightDataStructure airFlightData){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+extension)));

			int maxCarrierID = airFlightData.numberOfDifferentCarriers()-1;
			int maxMonth = airFlightData.getMaxMonthID();

			bw.write("carrier;carrierName;year;month;origin;destination;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

			for(int i=0; i<=maxCarrierID; i++){	

				for(int j=0; j<=maxMonth; j++){

					for(String info: airFlightData.getData(i, j)){				
						//System.out.println(info);
						bw.write(info+"\n");
					}
				}
			}

			bw.close();
			System.out.println("Anzahl der Carrier:"+maxCarrierID);
			System.out.println("Anzahl der Monate:"+maxMonth);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}



	public void printAirportGeoDataAsCSV(THashMap<String,String> airportGeoSet, AirFlightDataStructure _airFlights){
		try {		

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_Geo"+extension)));

			bw.write("airport;airportName;airportCity;airportCountry;latitude;longitude\n"); //BEWARE Fixed Header!!

			ArrayList<String> foundAirports = _airFlights.getFoundAirports();

			Collections.sort(foundAirports);

			String key=null, value;
			for(Iterator<String> keysIterator = foundAirports.iterator(); keysIterator.hasNext();){
				key = keysIterator.next();
				value = airportGeoSet.get(key);
				//System.out.println(key);

				bw.write(key+";"+value+"\n");
			}

			bw.close();
			System.out.println("saved airport geo data as csv");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	private void printFlightDataObjects(LinkedList<FlightDataObject> airFlights) {

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+extension)));
			FlightDataObject fdo;

			bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!			

			THashMap<String,String> foundAirlines = new THashMap<String,String>();

			for(ListIterator<FlightDataObject> it = airFlights.listIterator(); it.hasNext();){
				fdo = it.next();
				if(!foundAirlines.contains(fdo.getCarrier())){	
					foundAirlines.put(fdo.getCarrier(),fdo.getCarrierName());
				}

				bw.write(fdo.toString()+"\n");
			}
			bw.close();

			BufferedWriter bwAirlines = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+"AirlineNameList"+extension)));
			bwAirlines.write("carrier;carrierName\n"); //BEWARE Fixed Header!!

			Iterator<String> iterator = foundAirlines.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				bwAirlines.write(key+";"+foundAirlines.get(key)+"\n");
			}
			bwAirlines.close();

			System.out.println("   print all data in one file - file name: "+output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	/**
	 * Creates a subfolder for every year and prints the flight data in a single file for every year
	 * @param airFlights
	 */
	private void printFlightDataObjectsbyYear(LinkedList<FlightDataObject> airFlights) {

		//sort the list by year/origin/destination/carrier
		Collections.sort(airFlights, new YearOriginDestinationCarrierComparator());


		FlightDataObject currentFDO;

		int year, currentAggregatingYear = airFlights.get(0).getYear();

		try {



			ListIterator<FlightDataObject> iterator = airFlights.listIterator();

			//initialize folder structure for the starting year:
			File saveFolder = new File(dirURL_segmentSave+File.separator+currentAggregatingYear);
			if(!saveFolder.exists()){
				saveFolder.mkdirs();		
			}

			//initialize output file
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentAggregatingYear+extension)));
			bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

			while(iterator.hasNext()){
				currentFDO = iterator.next();
				year = currentFDO.getYear();

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
					bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!
				}
				bw.write(currentFDO.toString()+"\n");

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
	 * @param airFlights
	 */
	private void printFlightDataObjectsbyCarrier(LinkedList<FlightDataObject> airFlights) {

		//sort the list by year/origin/destination/carrier
		Collections.sort(airFlights, new CarrierYearOriginDestinationComparator());


		FlightDataObject currentFDO;

		int year, currentAggregatingYear = airFlights.get(0).getYear();
		String carrier, currentCarrier = airFlights.get(0).getCarrier();

		try {



			ListIterator<FlightDataObject> iterator = airFlights.listIterator();

			//initialize folder structure for the starting carrier:
			File saveFolder = new File(dirURL_segmentSave+File.separator+"carrier"+File.separator+"carrier_"+currentCarrier);
			if(!saveFolder.exists()){
				saveFolder.mkdirs();		
			}

			//initialize output file for the starting year
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_"+currentAggregatingYear+extension)));
			bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

			//initialize output file for all years
			BufferedWriter bwAll = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_allYears"+extension)));
			bwAll.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

			while(iterator.hasNext()){
				currentFDO = iterator.next();
				carrier = currentFDO.getCarrier();
				year = currentFDO.getYear();

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
					bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

					//initialize output file for all years
					bwAll = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_allYears"+extension)));
					bwAll.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!

				}

				if(year != currentAggregatingYear){
					bw.close();
					currentAggregatingYear = year;

					//initialize output file
					bw = new BufferedWriter(new FileWriter(new File(saveFolder+File.separator+output+currentCarrier+"_"+currentAggregatingYear+extension)));
					bw.write("origin;destination;carrier;year;month;passengers;aircraftGroup\n"); //BEWARE Fixed Header!!
				}


				bw.write(currentFDO.toString()+"\n");
				bwAll.write(currentFDO.toString()+"\n");

			}
			bw.close();
			System.out.println("      print by year - file name: " + output);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

