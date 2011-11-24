package DB1B;

import gnu.trove.map.hash.THashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class findHubPassengers {

	/**
	 * TODO:
	 * 
	 * Read in airportGroup and passengers
	 * Skip first two tokens
	 * If there still is a next token left, check if airport is already in THashMap(key=airport, value=passengers)
	 * Add passengers
	 * print Hashmap in csv 
	 * 
	 */

	// Linux File Folders
	//static String dirURL_geo = "/media/AndiUSB500/Alliance Competition/timetables/openflight/airports_geoCoordinates.dat";
	static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output";

	//static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	//static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output_hubpassengers";

	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";
	static String output = "flightdatalist_nonstop";	
	static String extension = ".csv";

	static int startyear = 2000;
	static int endyear =  2000;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;

	public static void main(String[] args){

		new findHubPassengers().doAll();

	}



	private void doAll()  {

		File file = new File(dirURL_segmentSave);

		if(!file.exists()){
			file.mkdirs();
		}


		findAndPrintHubPassengers();

		// TODO: COMBINE QUARTERS


	}



	private void findAndPrintHubPassengers() {

		output = "quarteraggregate_nonstop"+File.separator+"wholeNetwork"+File.separator+"hubpassengers"+File.separator+"flightdatalist_hubpassengers";
		for(int j = startyear; j <= endyear; j++){
			for(int k =1; k<=4; k++) {

				// Read in the data and remember the Hub passengers for every Airport
				THashMap<String, Integer> hubPassengers = findPassengers(j,k);

				// Save in csv
				printHubPassengers(hubPassengers,j,k);


			}
		}

	}


	public THashMap<String, Integer> findPassengers (int _year, int _quarter){

		String line, airportGroup, temp, origin, destination;
		int  year=_year, quarter=_quarter, passengers, combinedPassengers;

		long time = System.currentTimeMillis();
		System.out.println("Reading in the data of year "+year+" quarter "+quarter+"...");

		THashMap<String, Integer> hubPassengers = new THashMap<String, Integer>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(dirURL_segment+File.separator+fileSegment+year+"_"+quarter+extension)));

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

					//Airport Group.			
					airportGroup = tokens[21];
					airportGroup = airportGroup.substring(1 , airportGroup.length()-1);
					if(debug) System.out.print(airportGroup+" - ");

					//Passengers
					temp = tokens[31];
					temp = temp.substring(0, temp.length()-3);
					passengers= Integer.parseInt(temp);
					if(debug) System.out.print(passengers+"\n");

					StringTokenizer st = new StringTokenizer(airportGroup, ":");
					origin = st.nextToken();
					destination = st.nextToken();
					while(st.hasMoreTokens()){
						// The old destination airport is a hub

						// Check if airport is in list yet, if not add airport as key and passengers as value
						if(!hubPassengers.contains(destination)){
							hubPassengers.put(destination,passengers);
							if(debug) System.out.print("new: "+destination+" passengers: "+passengers+"\n");
						}

						// If airport key already in list, add passengers to already known passengers as value
						else{
							combinedPassengers = hubPassengers.get(destination)+ passengers;
							hubPassengers.put(destination, combinedPassengers);	
							if(debug) System.out.print("known: "+destination+" passengers: "+passengers+"\n");
						}

						// get the next airport pair
						origin = destination;	
						destination = st.nextToken();
					}

				} catch (NoSuchElementException e) {
					System.out.println("Failure in line : "+line);
					//e.printStackTrace();
				}

				line = br.readLine();
			}
			System.out.println("... took "+(System.currentTimeMillis()-time)/1000+" seconds.");
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return hubPassengers;
	}

	

	private void printHubPassengers(THashMap<String, Integer> _hubPassengers, int _year, int _quarter) {
		
		int year = _year, quarter = _quarter;

		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+"_"+year+"_"+quarter+extension)));
			bw.write("airport;hubPassengers\n"); //BEWARE Fixed Header!!

			THashMap<String,Integer> hubPassengers = _hubPassengers;

			Iterator<String> iterator = hubPassengers.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				bw.write(key+";"+hubPassengers.get(key)+"\n");
			}
			bw.close();

			System.out.println(" => printed all data in one file - file name: "+output);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}


