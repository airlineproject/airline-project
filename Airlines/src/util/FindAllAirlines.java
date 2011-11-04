package util;

import java.io.*;
import java.util.StringTokenizer;

import gnu.trove.set.hash.THashSet;


public class FindAllAirlines {
	
	static String dirURL = "E:\\Alliance Competition\\timetables\\DOT\\DB1B Market";
	String fileMarket = "Origin_and_Destination_Survey_DB1BMarket_";
	
	static String output = "Airlineliste";
	static String extension = ".csv";
	
	int numberOfLinesToReadInTicketFile = 500;
	
	static int startyear = 1993;
	static int endyear =  1994;
	private static THashSet<String> airlineNameSet;
	
	
	public static void main(String[] args){
		try {
			airlineNameSet = new THashSet<String>();
			//LinkedList<String> airlineNameSet = new LinkedList<String>();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dirURL+File.separator+output+extension)));
			for(int i = startyear; i <= endyear; i++){
				String year = Integer.toString(i)+"_";
				for(int j = 1; j <= 4; j++){
					String quarter = Integer.toString(j);
					new FindAllAirlines().doFinding(year, quarter, bw);	
				}	
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		new FindAllAirlines().doAll();
		
	}

	private void doAll() {
		// TODO Auto-generated method stub
		
		
	}

	public void doFinding(String year, String quarter, BufferedWriter bw) {
		// extract all characteristics of certain variable
		
		
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
				airlineID = String.valueOf(temp);
				
				if (!airlineNameSet.contains(airlineID)){
					//remember this itineryID
					airlineNameSet.add(airlineID);
					bw.write(airlineID+";"+year+quarter+"\n");
					System.out.println(airlineID);
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
