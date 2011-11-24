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
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class FindAirports {

	// Linux File Folders
	//static String dirURL_segment = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market";
	//static String dirURL_segmentSave = "/media/AndiUSB500/Alliance Competition/timetables/DOT/DB1B Market/Output";

	//Windows File Folders
	static String dirURL_segment = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	static String dirURL_segmentSave = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market\\Output";

	String fileSegment = "Origin_and_Destination_Survey_DB1BMarket_";
	static String output = "airportList";	
	static String extension = ".csv";

	static int startyear = 1993;
	static int endyear =  2010;  // BEWARE; ENDYEAR NEEDS TO HAVE 4 QUARTERS AVAILABLE

	static boolean debug = false;


	//if set to a negative number <0, all lines will be read
	int numberOfLinesToReadTestMode = -1;

	public static void main(String[] args){

		new FindAirports().doAll();

	}


	private void doAll()  {

		THashMap<String, String> foundAirports = findAirports();
		
		printAirports(foundAirports);

	}


	public THashMap<String, String> findAirports() {


		THashMap<String, String> foundAirports = new THashMap<String, String>();

		for(int year = startyear; year <= endyear; year++){
			for(int quarter =1; quarter<=4; quarter++) {

				String line, origin, destination;
				int originNumberOfAirports, originCityNumber, destinationNumberOfAirports, destinationCityNumber;

				long time = System.currentTimeMillis();
				System.out.println("Reading in the data of year "+year+" quarter "+quarter+"...");


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

							//Origin.
							origin = tokens[5];
							origin = origin.substring(1 , origin.length()-1);
							if(debug) System.out.print(origin+" - ");

							//Number of Airports in origin area
							originNumberOfAirports = Integer.parseInt(tokens[6]); 
							if(debug) System.out.print(originNumberOfAirports+" - ");

							// Origin City Number
							originCityNumber = Integer.parseInt(tokens[7]);
							if(debug) System.out.print(originCityNumber+" - ");

							//Destination.
							destination = tokens[13];
							destination = destination.substring(1 , destination.length()-1);
							if(debug) System.out.print(destination+" - ");

							//Number of Airports in destination area
							destinationNumberOfAirports = Integer.parseInt(tokens[14]); 
							if(debug) System.out.print(destinationNumberOfAirports+" - ");

							// Destination City Number
							destinationCityNumber = Integer.parseInt(tokens[15]);
							if(debug) System.out.print(destinationCityNumber+" - ");


							// Abfrage ob Airport schon in LinkedList, wenn nicht hinzufuegen.
							if(!foundAirports.contains(origin)){
								foundAirports.put(origin,new String(originNumberOfAirports+";"+originCityNumber));
							}
							if(!foundAirports.contains(destination)){
								foundAirports.put(destination,new String(destinationNumberOfAirports+";"+destinationCityNumber));
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

			}

		}


		return foundAirports;

	}

	private void printAirports(THashMap<String, String> _foundAirports) {

		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL_segmentSave+File.separator+output+extension)));
			bw.write("city;cityNumberOfAirports;CityNumber\n"); //BEWARE Fixed Header!!

			THashMap<String,String> foundAirports = _foundAirports;

			Iterator<String> iterator = foundAirports.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				bw.write(key+";"+foundAirports.get(key)+"\n");
			}
			bw.close();

			System.out.println(" => printed all data in one file - file name: "+output);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}