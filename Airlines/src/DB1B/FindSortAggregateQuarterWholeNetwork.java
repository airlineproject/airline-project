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


public class FindSortAggregateQuarterWholeNetwork {

	// Linux File Folders
	//static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/airports_geoCoordinates.dat";
	static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output";

	//static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	//static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";

	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";
	static String output = "flightdatalist_nonstop";	
	static String extension = ".csv";

	static int startyear = 1993;
	static int endyear =  2010;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;

	public static void main(String[] args){

		new FindSortAggregateQuarterWholeNetwork().doAll();

	}



	private void doAll()  {

		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}


		findAndSortQuarters();

		findAndAggregateYear();


	}



	private void findAndSortQuarters() {

		for(int j = startyear; j <= endyear; j++){

			for(int k =1; k<=4; k++) {

				// Read in the data and sort it by AirportGroup
				LinkedList<NonstopDataObject> sortedDB1B = findAndSortDB1BFlights(j,k);

				// Collapse data within Quarter to one observation per airportGroup and save it to csv
				LinkedList<NonstopDataObject> QuarterDataList = aggregateToAirportGroup(sortedDB1B);
				output = "quarteraggregate"+File.separator+"wholeNetwork"+File.separator+"quarteraggregate_sorted";
				printDB1BDataObjects(QuarterDataList,j,k);

				// Split Market data into nonstop-flights and only keep nonstop connections
				LinkedList<NonstopDataObject> QuarterNonstopDataList = makeNonstopAndAggregateToAirportGroup(QuarterDataList);
				output = "quarteraggregate_nonstop"+File.separator+"wholeNetwork"+File.separator+"nonstop_quarteraggregate_sorted";
				printDB1BDataObjects(QuarterNonstopDataList,j,k);

			}

		}

	}


	private void findAndAggregateYear() {

		for(int j = startyear; j <= endyear; j++){

			// Read in the four quarters

			LinkedList<NonstopDataObject> sortedDB1BAllQuarters = new LinkedList<NonstopDataObject>();

			for(int k =1; k<=4; k++) {	

				readInDB1BQuarter(j,k, sortedDB1BAllQuarters);

			}

			// Aggregate and save to csv
			LinkedList<NonstopDataObject> YearDataList = aggregateToAirportGroup(sortedDB1BAllQuarters);
			output = "yearaggregate"+File.separator+"wholeNetwork"+File.separator+"nonstop_yearaggregate_sorted";
			printDB1BDataObjects(YearDataList,j,0);
			
			
			// Split Market data into nonstop-flights and only keep nonstop connections
			LinkedList<NonstopDataObject> YearNonstopDataList = makeNonstopAndAggregateToAirportGroup(YearDataList);
			output = "yearaggregate_nonstop"+File.separator+"wholeNetwork"+File.separator+"nonstop_yearaggregate_sorted";
			printDB1BDataObjects(YearNonstopDataList,j,0);
			
			

		}

	}





	/**
	 * This function reads in the quarterly data and constructs an DB1BNonstopDataObject
	 * which contains all the sorted data.
	 * 
	 * @returns the data in a suitable way
	 */


	public LinkedList<NonstopDataObject>  findAndSortDB1BFlights(int _year, int _quarter){

		LinkedList<NonstopDataObject> sortedDB1B = new LinkedList<NonstopDataObject>();

		String line, temp, origin, destination, airportGroup;

		int  year=_year, quarter=_quarter, quarterID, passengers;		

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

					//Passengers
					temp = tokens[31];
					temp = temp.substring(0, temp.length()-3);
					passengers= Integer.parseInt(temp);
					if(debug) System.out.print(passengers+" - ");

					// Laufende Zahl des Quartals berechnen und quarterID setzen.
					int quarterInt = Integer.valueOf(quarter);
					if(debug) System.out.print(quarterInt+" - ");
					quarterID = (year-1993)*4+quarterInt-1;	
					if(debug) System.out.print(quarterID+"\n");


					sortedDB1B.add(new NonstopDataObject(origin, destination, airportGroup, year, quarterID, passengers));

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
		Collections.sort(sortedDB1B, new AirportGroupComparator());

		System.out.println("...and the sorting took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return sortedDB1B;
	}

	
	

	public LinkedList<NonstopDataObject> aggregateToAirportGroup(LinkedList<NonstopDataObject> _sortedDB1B){

		System.out.println("aggregateToAirportGroup");

		LinkedList<NonstopDataObject> aggregatedNonstopList = new LinkedList<NonstopDataObject>();
		Comparator<NonstopDataObject> comparate = new AirportGroupComparator();
		NonstopDataObject baseline, compareline, printline;
		int combinedPassengers, comparelinePassengers;

		LinkedList<NonstopDataObject> flights = _sortedDB1B;

		long time = System.currentTimeMillis();

		baseline = flights.removeFirst();
		combinedPassengers = baseline.getPassengers();

		while(flights.size()>0){
			compareline = flights.removeFirst();
			comparelinePassengers = compareline.getPassengers();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same

				combinedPassengers += comparelinePassengers;

			}else{
				// Base and compareline are different
				printline = new NonstopDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getYear(), baseline.getQuarterID(), combinedPassengers);
				aggregatedNonstopList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group

		printline = new NonstopDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getYear(), baseline.getQuarterID(), combinedPassengers);
		aggregatedNonstopList.add(printline);

		System.out.println("... Aggregating the data within quarter took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return aggregatedNonstopList; 


	}
	
	
	
	public LinkedList<NonstopDataObject> makeNonstopAndAggregateToAirportGroup(LinkedList<NonstopDataObject> _quarterDataList) {

		System.out.println("aggregateNonstopWithinQuarter");

		LinkedList<NonstopDataObject> aggregatedNonstopList = new LinkedList<NonstopDataObject>();
		Comparator<NonstopDataObject> comparate = new AirportGroupComparator();

		LinkedList<NonstopDataObject> quarterDataList = _quarterDataList;
		LinkedList<NonstopDataObject> nonstopFlights = new LinkedList<NonstopDataObject>();

		NonstopDataObject line, flight;

		String airportGroup, origin, destination;

		long time = System.currentTimeMillis();

		while(quarterDataList.size()>0){

			line = quarterDataList.removeFirst();	

			airportGroup = line.getAirportGroup();

			StringTokenizer st = new StringTokenizer(airportGroup, ":");
			origin = st.nextToken();
			destination = st.nextToken();
			flight = new NonstopDataObject(origin, destination, new String(origin+":"+destination), line.getYear(), line.getQuarterID(), line.getPassengers());
			while(st.hasMoreTokens()){
				origin = destination;
				destination = st.nextToken();

				//Add this origin-destination combination as a new nonstop-flight in nonstopflight
				//Remember the passengers!!
				flight = new NonstopDataObject(origin, destination, new String(origin+":"+destination), line.getYear(), line.getQuarterID(), line.getPassengers());
				nonstopFlights.add(flight);
			}
		}


		Collections.sort(nonstopFlights, new AirportGroupComparator());

		System.out.println("Splitting the data into nonstop-flights took "+(System.currentTimeMillis()-time)/1000+" seconds.");
		time = System.currentTimeMillis();

		int combinedPassengers, comparelinePassengers;		
		NonstopDataObject baseline, compareline, printline;

		baseline = nonstopFlights.removeFirst();
		combinedPassengers = baseline.getPassengers();

		while(nonstopFlights.size()>0){

			compareline = nonstopFlights.removeFirst();
			comparelinePassengers = compareline.getPassengers();

			if(comparate.compare(baseline, compareline)==0){
				// Base and compareline are the same

				combinedPassengers += comparelinePassengers;

			}else{
				// Base and compareline are different
				printline = new NonstopDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getYear(), baseline.getQuarterID(), combinedPassengers);
				aggregatedNonstopList.add(printline);

				baseline = compareline;
				combinedPassengers = compareline.getPassengers();
			}			
		}

		// Add info of the last group

		printline = new NonstopDataObject(baseline.getOrigin(), baseline.getDestination(), baseline.getAirportGroup(), baseline.getYear(), baseline.getQuarterID(), combinedPassengers);
		aggregatedNonstopList.add(printline);

		System.out.println("and aggregating the data within quarter took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return aggregatedNonstopList; 


	}


	public LinkedList<NonstopDataObject>  readInDB1BQuarter(int _year, int _quarter, LinkedList<NonstopDataObject> _sortedDB1BQuarter){

		LinkedList<NonstopDataObject> sortedDB1B = _sortedDB1BQuarter;

		String line, origin, destination, airportGroup;

		StringTokenizer st;

		int  year=_year, quarter=_quarter, quarterID, passengers;		

		long time = System.currentTimeMillis();
		System.out.println("FindAndSortDB1BFlights - Reading in the data of year "+year+" quarter "+quarter+"...");

		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_segment+File.separator+"Output"+File.separator+"quarteraggregate"+File.separator+"wholeNetwork"+File.separator+"quarteraggregate_sorted_"+year+"_"+quarter+extension)));

			line = br.readLine();

			line = br.readLine(); //skip the header

			int i = numberOfLinesToReadTestMode;

			while( i!= 0 && line != null){
				--i;

				st = new StringTokenizer(line, ";");

				//Origin.
				origin = st.nextToken();
				if(debug) System.out.print(origin+" - ");

				//Destination.
				destination = st.nextToken();
				if(debug) System.out.print(destination+" - ");

				//Airport Group.			
				airportGroup = st.nextToken();
				if(debug) System.out.print(airportGroup+" - ");

				//Year
				year = Integer.parseInt(st.nextToken());

				//QuarterID
				quarterID = Integer.parseInt(st.nextToken());

				//Passengers
				passengers= Integer.parseInt(st.nextToken());
				if(debug) System.out.print(passengers+" - ");

				sortedDB1B.add(new NonstopDataObject(origin, destination, airportGroup, year, quarterID, passengers));	

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
		Collections.sort(sortedDB1B, new AirportGroupComparator());

		System.out.println("...and the sorting took "+(System.currentTimeMillis()-time)/1000+" seconds.");

		return sortedDB1B;
	}








	/**
	 * This function prints a DB1BDataobject as well as a list of the included Airlines as a csv.
	 * Set year = 0 if you want to print the Airport and Airline list
	 * @returns nothing
	 */

	private void printDB1BDataObjects(LinkedList<NonstopDataObject> _sortedDB1B, int _year, int _quarter) {
		try {
			long time = System.currentTimeMillis();

			int year = _year;
			int quarter = _quarter;

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_"+year+"_"+quarter+extension)));

			NonstopDataObject ddo;

			bw.write("origin; destination; airportGroup; year; quarterID; passengers\n"); //BEWARE Fixed Header!!			

			for(ListIterator<NonstopDataObject> it = _sortedDB1B.listIterator(); it.hasNext();){
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



}
