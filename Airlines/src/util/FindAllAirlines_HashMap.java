package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;


public class FindAllAirlines_HashMap {
	
	static String dirURL = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	String fileMarket = "Origin_and_Destination_Survey_DB1BMarket_";
	
	static String output = "Airlineliste";
	static String extension = ".csv";
	
	int numberOfLinesToReadInTicketFile = 500;
	
	static int startyear = 1993;
	static int endyear =  2011;
	int lastQuartal = 1;
	
	private THashMap<String,String> airlineNameSet;
	
	
	public static void main(String[] args){
		if(args.length == 2){
			startyear = Integer.parseInt(args[0]);
			endyear = Integer.parseInt(args[1]);
		}
	
		new FindAllAirlines_HashMap().doAll();
		
	}

	private void doAll() {

		airlineNameSet = new THashMap<String,String>();
		
		if(airlineNameSet == null) System.out.println("hashmap gleich null in doALL");


		for(int i = startyear; i <= endyear; i++){
			String year = Integer.toString(i)+"_";
			for(int j = 1; (i < endyear && j <= 4) || (i == endyear && j <= lastQuartal); j++){
				
				String quarter = Integer.toString(j);
				doFinding(year, quarter);	
			}	
		}
		
		// TODO Auto-generated method stub
		try {
			//LinkedList<String> airlineNameSet = new LinkedList<String>();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL+File.separator+output+extension)));
	
			Set<String> keys  = airlineNameSet.keySet();
			ArrayList<String> sortedKeys = new ArrayList<String>(airlineNameSet.size());
			for(String key: keys) sortedKeys.add(key);
			
			Collections.sort(sortedKeys);
			
			String key=null, value;
			for(Iterator<String> keysIterator = sortedKeys.iterator(); keysIterator.hasNext();){
				key = keysIterator.next();
				value = airlineNameSet.get(key);
				
				bw.write(value);
			}
			
			
			
//			for(String keyx: keys){
//				
//				value = airlineNameSet.get(keyx);
//				
//				bw.write(keyx+";"+value+"\n");
//			}
			
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void doFinding(String year, String quarter) {
	
		
		
		// extract all characteristics of certain variable
		
		System.out.println("Analyzing file from year "+year+", quarter "+quarter);
		
		StringTokenizer st;
		String airlineID;
		String temp = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(dirURL+File.separator+fileMarket+year+quarter+extension)));
			
			
			String line = br.readLine();
			
			// Jump to second line
			//line = br.readLine();
			
//			for(int i = numberOfLinesToReadInTicketFile; i > 0; --i){
			while(line != null){
				st = new StringTokenizer(line, ",");
				
				//29th word, BEWARE this variable only contains airlines who ticketed on-line itineraries at least once.
				int ii = 29;
				for(int j = ii; j>0; --j){
					temp = st.nextToken();
				}
				
				
				
				temp = temp.substring(1, temp.length()-1);
				airlineID = temp;
				
				if(airlineNameSet == null) System.out.println("hashmap gleich null");

				if(airlineID == null) System.out.println("ID gleich null");

				if (!airlineNameSet.contains(airlineID)){
					//remember this itineryID
					airlineNameSet.put(airlineID, new String(airlineID+";"+year+quarter+"\n"));
					System.out.print("   "+airlineID+";"+year+quarter+"\n");
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
	
	
}
