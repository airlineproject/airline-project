package DB1B;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.map.hash.THashMap;

/**
 * TODO: PUT AIRPORT INFORMATION IN AIRPORT LIST
 * 
 * 
 * 
 * Combine the DB1B - files and save them as a csv.
 * @author lindenblatta
 *
 */
public class FindAndCombineDB1BData {

// Linux File Folders
	//static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/usairports_geoCoordinates.dat";
	//static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	//static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output";
	
	static String dirURL_geo = "E:\\Alliance Competition\\timetables\\openflight\\usairports_geoCoordinates.csv";
	static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";
	
	
	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";

//	static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";


	static String output = "flightdatalist";
	static String extension = ".csv";

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;


	static int startyear = 1993;
	static int endyear =  2010;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE


	public static void main(String[] args){

		new FindAndCombineDB1BData().doAll();

	}



	private void doAll() {
		File file = new File(dirURL_segmentSave);
		if(!file.exists()){
			file.mkdirs();
		}

		//read in data and save in one single CSV
		//also get coordinates of used airports and save thems as csv
		findFlights();

//		// Find the geocordinates for all airports and only print the ones with flight data
//		THashMap<String, String> airportGeoSet = findCoordinates();
//		printAirportGeoDataAsCSV(airportGeoSet, airFlights);
//
//		// save to file (needs the airportGeoSet
//		printflightDataAsCSV(airFlights);
//		//System.out.println(carrierCounter);

	}


	/**
	 * This function reads in the raw data and constructs and saves the data needed in one csv file
	 */
	public void findFlights(){

		ArrayList<String> foundCarriers= new ArrayList<String>();
		ArrayList<String> foundAirports= new ArrayList<String>();

		// Save folder
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+extension)));
			bw.write("year,quarterID,itinID,marketID,origin,destination,airportGroup,tkChange,tkGroup,opGroup,tkCarrier,passengers,marketFare,marketDistance,nonstopDistance\n");

			//open all flight data files and extract.
			for(int j = startyear; j <= endyear; j++){
				for(int i =1; i<=4; i++) {
					long time2 = System.currentTimeMillis();
					BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_segment+File.separator+fileSegment+j+"_"+i+extension)));
					System.out.println("scanning market "+j+" - "+i+" and saving it to the csv...");

					String itinID,  marketID, airportGroup, origin, destination, tkChange, tkGroup, opGroup, passengers, marketFare, marketDistance, nonstopDistance, tkCarrier;

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

					int k = numberOfLinesToReadTestMode;

					while( k!= 0 && line != null){
						--k;

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
						tkChange = tokens[23];
						tkChange = tkChange.substring(0, tkChange.length()-3);
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
						passengers = tokens[31];
						passengers = passengers.substring(0, passengers.length()-3);
						if(debug) System.out.print(passengers+" - ");

						//MarketFare.			
						marketFare = tokens[32];
						marketFare = marketFare.substring(0, marketFare.length()-3);
						if(debug) System.out.print(marketFare+" - ");

						//MarketDistance.			
						marketDistance = tokens[33];
						marketDistance = marketDistance.substring(0, marketDistance.length()-3);
						if(debug) System.out.print(marketDistance+" - ");

						//NonstopDistance.			
						nonstopDistance = tokens[36];
						nonstopDistance = nonstopDistance.substring(0, nonstopDistance.length()-3);
						if(debug) System.out.print(nonstopDistance+"\n");

						// Abfrage ob Carrier schon in ArrayList, wenn nicht hinzuf�gen.

						if(!foundCarriers.contains(tkCarrier)){
							foundCarriers.add(tkCarrier);
						}

						// Abfrage ob Airport schon in ArrayList, wenn nicht hinzuf�gen.

						if(!foundAirports.contains(origin)){
							foundAirports.add(origin);
						}
						
						if(!foundAirports.contains(destination)){
							foundAirports.add(destination);
						}



						// Laufende Zahl des Quartals berechnen und quarterID setzen.
						int quarterInt = Integer.valueOf(i);
						int quarterID = (j-1993)*4+quarterInt-1;

						// Market-Data speichern

						bw.write(j+","+quarterID+","+itinID+","+marketID+","+origin+","+destination+","+airportGroup+","+tkChange+","+tkGroup+","+opGroup+","+tkCarrier+","+passengers+","+marketFare+","+marketDistance+","+nonstopDistance+"\n");

						line = br.readLine();

					}

					br.close();
					
					System.out.println("   ... took "+(System.currentTimeMillis()-time2)/1000+" seconds.");
				}

			}

			bw.close();
			
			// find and print all airports found in the data
			THashMap<String, String> airportGeoSet = findCoordinates();
			BufferedWriter bwgeo = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_Geo"+extension)));

			bwgeo.write("airport;latitude;longitude\n"); //BEWARE Fixed Header!!

			Collections.sort(foundAirports);

			String key=null, value;
			for(Iterator<String> keysIterator = foundAirports.iterator(); keysIterator.hasNext();){
				key = keysIterator.next();
				value = airportGeoSet.get(key);
				//System.out.println(key);

				bwgeo.write(key+";"+value+"\n");
			}
			bwgeo.close();
			System.out.println("saved airport geo data as csv");
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		String airportCode, longitude, latitude;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL_geo)));
			System.out.println("scan "+dirURL_geo);


			String line = br.readLine();

			//for(int i = numberOfLinesToReadTestMode; i > 0; --i){
			while(line != null){
				st = new StringTokenizer(line, ",");

				//jump to airport code and removes "".
				airportCode  = st.nextToken();
				if(debug) System.out.print("   "+airportCode);

				//jump to latitude and removes "".
				latitude = st.nextToken();
				if(debug) System.out.print("   "+latitude);

				//jump to longitude and removes "".
				longitude = st.nextToken();
				if(debug) System.out.println("   "+longitude+"\n");

				//put airportCode as key and lat,long as value in airportGeoSet, hashmap. 
				airportGeoSet.put(airportCode, new String(latitude+";"+longitude));
				//if(debug) System.out.println(airportGeoSet);

				//System.out.println(airportCode+";"+airportGeoSet.get(airportCode));


				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return airportGeoSet;
	}

	
}





