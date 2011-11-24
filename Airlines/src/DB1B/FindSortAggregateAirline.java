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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FindSortAggregateAirline {

	// Linux File Folders
	//static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/airports_geoCoordinates.dat";
	//static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	//static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output_nonstop";

	static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";

	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";
	static String output = "nonstop_quarteraggregate_alliances";	
	static String extension = ".csv";

	static int startyear = 1993;
	static int endyear =  2010;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;

	public static void main(String[] args){

		new FindSortAggregateAirline().doAll();

	}



	private void doAll()  {

		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}


		findAndSortQuarters();

		// TODO: AGGREGATE WITHIN YEAR



	}

	


	private void findAndSortQuarters() {

		for(int j = startyear; j <= endyear; j++){
			for(int k =1; k<=4; k++) {

				// Read in the data and sort it by AirportGroup
				LinkedList<DB1BDataObject> sortedDB1B = findAndSortDB1BFlights(j,k);

				// Collapse data within Quarter to one observation per airportGroup and save it to csv
				LinkedList<DB1BDataObject> QuarterDataList = aggregateWithinQuarter(sortedDB1B);
				output = "quarteraggregate"+File.separator+"airline"+File.separator+"quarteraggregate_sorted";
				printDB1BDataObjects(QuarterDataList,j,k);

				// Split Market data into nonstop-flights and only keep nonstop connections
				LinkedList<DB1BDataObject> QuarterNonstopDataList = makeNonstopAndAggregateWithinQuarter(QuarterDataList);
				output = "quarteraggregate_nonstop"+File.separator+"airline"+File.separator+"nonstop_quarteraggregate_sorted";
				printDB1BDataObjects(QuarterNonstopDataList,j,k);


			}
		}

	}


	/**
	 * This function reads in the quarterly data and constructs an DB1BFlightDataObject
	 * which contains all the sorted data.
	 * 
	 * @returns the data in a suitable way
	 */


	public LinkedList<DB1BDataObject>  findAndSortDB1BFlights(int _year, int _quarter){

		LinkedList<DB1BDataObject> sortedDB1B = new LinkedList<DB1BDataObject>();

		String  line, temp, origin, destination, airportGroup, tkGroup, opGroup, tkCarrier, itinID, marketID;

		int year=_year, quarter=_quarter, quarterID, tkChange, passengers, marketFare, marketDistance, nonstopDistance;

		long time = System.currentTimeMillis();
		System.out.println("FindAndSortDB1BFlights - Reading in the data of year "+year+" quarter "+quarter+"...");


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
					tkChange = Integer.parseInt(temp.substring(0, temp.length()-3));
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
					passengers = Integer.parseInt(temp.substring(0, temp.length()-3));
					if(debug) System.out.print(passengers+" - ");

					//MarketFare.			
					temp = tokens[32];
					marketFare = Integer.parseInt(temp.substring(0, temp.length()-3));
					if(debug) System.out.print(marketFare+" - ");

					//MarketDistance.			
					temp = tokens[33];
					marketDistance = Integer.parseInt(temp.substring(0, temp.length()-3));
					if(debug) System.out.print(marketDistance+" - ");

					//NonstopDistance.			
					temp = tokens[36];
					nonstopDistance = Integer.parseInt(temp.substring(0, temp.length()-3));
					if(debug) System.out.print(nonstopDistance+"\n");

					// Laufende Zahl des Quartals berechnen und quarterID setzen.
					quarterID = (year-1993)*4+Integer.valueOf(quarter)-1;	
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

		System.out.println("aggregateWithinQuarter");


		LinkedList<DB1BDataObject> aggregatedDB1BList = new LinkedList<DB1BDataObject>();
		Comparator<DB1BDataObject> comparate = new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator();
		DB1BDataObject baseline, compareline, printline;
		int combinedPassengers, combinedMarketFare, comparelinePassengers, comparelineMarketFare;


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



	public LinkedList<DB1BDataObject> makeNonstopAndAggregateWithinQuarter(LinkedList<DB1BDataObject> _quarterDataList) {

		System.out.println("makeNonstopAndAggregateWithinQuarter");

		LinkedList<DB1BDataObject> aggregatedDB1BList = new LinkedList<DB1BDataObject>();
		Comparator<DB1BDataObject> comparate = new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator();

		DB1BDataObject baseline, compareline, printline, line, flight;
		int combinedPassengers, combinedMarketFare, comparelinePassengers, comparelineMarketFare;

		LinkedList<DB1BDataObject> quarterDataList = _quarterDataList;
		LinkedList<DB1BDataObject> nonstopFlights = new LinkedList<DB1BDataObject>();

		String airportGroup, tkGroup, opGroup, origin, destination;

		long time = System.currentTimeMillis();


		// Decomposite data into nonstop flights
		////////////////////////////////////////

		while(quarterDataList.size()>0){

			line = quarterDataList.removeFirst();	

			airportGroup = line.getAirportGroup();
			tkGroup = line.getTkGroup();
			opGroup = line.getOpGroup();

			StringTokenizer st = new StringTokenizer(airportGroup, ":");
			origin = st.nextToken();
			destination = st.nextToken();

			StringTokenizer stOp = new StringTokenizer(opGroup, ":");
			opGroup = stOp.nextToken();

			StringTokenizer stTk = new StringTokenizer(tkGroup, ":");
			tkGroup = stTk.nextToken();

			flight = new DB1BDataObject(origin, destination, new String(origin+":"+destination), tkGroup, opGroup, line.getTkCarrier(), line.getItinID(), line.getMarketID(), line.getYear(), line.getQuarterID(), line.getTkChange(), line.getPassengers(), line.getMarketFare(), line.getMarketDistance(), line.getNonstopDistance());

			while(st.hasMoreTokens()){
				origin = destination;
				destination = st.nextToken();
				opGroup = stOp.nextToken();
				tkGroup = stTk.nextToken();

				//Add this origin-destination combination as a new nonstop-flight in nonstopflight
				//Remember the passengers!!
				flight = new DB1BDataObject(origin, destination, new String(origin+":"+destination), tkGroup, opGroup, line.getTkCarrier(), line.getItinID(), line.getMarketID(), line.getYear(), line.getQuarterID(), line.getTkChange(), line.getPassengers(), line.getMarketFare(), line.getMarketDistance(), line.getNonstopDistance());
				nonstopFlights.add(flight);

			}
		}

		Collections.sort(nonstopFlights, new TkCarrierOpCarrierYearQuarterIDAirportGroupComparator());

		System.out.println("Splitting the data into nonstop-flights took "+(System.currentTimeMillis()-time)/1000+" seconds.");
		time = System.currentTimeMillis();



		// Aggregate within quarter
		///////////////////////////

		baseline = nonstopFlights.removeFirst();
		combinedPassengers = baseline.getPassengers();
		combinedMarketFare = baseline.getMarketFare();

		while(nonstopFlights.size()>0){

			compareline = nonstopFlights.removeFirst();
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
				combinedMarketFare = compareline.getMarketFare();
			}			
		}

		// Add info of the last group

		printline = new DB1BDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getTkGroup(), baseline.getOpGroup(), baseline.getTkCarrier(), baseline.getItinID(), baseline.getMarketID(), baseline.getYear(), baseline.getQuarterID(), baseline.getTkChange(), combinedPassengers, combinedMarketFare, baseline.getMarketDistance(), baseline.getNonstopDistance());
		aggregatedDB1BList.add(printline);

		System.out.println("and aggregating the data within quarter took "+(System.currentTimeMillis()-time)/1000+" seconds.");

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

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_"+year+"_"+quarter+extension)));

			DB1BDataObject ddo;

			bw.write("origin; destination; airportGroup; tkGroup; opGroup; tkCarrier; itinID; marketID; year; quarterID; tkChange; passengers; marketFare; marketDistance; nonstopDistance\n"); //BEWARE Fixed Header!!					

			for(ListIterator<DB1BDataObject> it = _sortedDB1B.listIterator(); it.hasNext();){
				ddo = it.next();
				bw.write(ddo.toString()+"\n");
			}

			bw.close();

			System.out.println("...printing all data in one file - file name: "+output + " took "+(System.currentTimeMillis()-time)/1000+" seconds.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	public LinkedList<DB1BDataObject> aggregateWithinYear(LinkedList<DB1BDataObject> _sortedDB1B){

		System.out.println("aggregateWithinYear");



		LinkedList<DB1BDataObject> aggregatedDB1BList = new LinkedList<DB1BDataObject>();
		Comparator<DB1BDataObject> comparate = new TkCarrierOpCarrierYearAirportGroupComparator();
		DB1BDataObject baseline, compareline, printline;
		int combinedPassengers, combinedMarketFare, comparelinePassengers, comparelineMarketFare;


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



}
