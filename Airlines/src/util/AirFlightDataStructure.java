package util;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.LinkedList;

public class AirFlightDataStructure {

	private LinkedList<String>[][] airFlightData;
	private ArrayList<String> foundAirports;
	private int carrierCounter;
	private THashMap<String,String> carrierIDSet;
	private int maxMonthID;
	
	
	/**
	 * 
	 * @param numberCarriers
	 * @param maxMonth
	 */
	
	public AirFlightDataStructure(int numberCarriers, int maxMonth){
		airFlightData = new LinkedList[numberCarriers][maxMonth];
		
		for(int x = airFlightData.length-1; x>=0; --x){
			for(int y = airFlightData[x].length-1; y>=0; --y){
				airFlightData[x][y] = new LinkedList<String>();
			}
		}
		
		//declare and initialize Hashmap for CarrierID.
		carrierIDSet = new THashMap<String,String>();
		carrierCounter = 0; //key for hashmap.

		maxMonthID = -1;
		
		foundAirports = new ArrayList<String>();
	}
	
	
	public LinkedList<String>[][] getAirFlightData(){
		return airFlightData;
	}
	
	public ArrayList<String> getFoundAirports(){
		return foundAirports;
	}

	
	public int numberOfDifferentCarriers(){
		return carrierCounter;
	}

	/**
	 * Returns a valid id of that carrier
	 * 
	 * @param carrier for which the id is requested
	 * @returns the id (as a string)
	 */
	public String getIDOfCarrier(String carrier) {
		String id = carrierIDSet.get(carrier);
		if(id == null){
			id = carrierCounter+"";//Integer.toString(carrierCounter);
			carrierIDSet.put(carrier, id);				
			carrierCounter++;
		}
		return id;
	}

	public int getMaxMonthID(){
		return maxMonthID;
	}
	

	public void add(String data, int carrierID, int monthID, String origin, String destination) {
		airFlightData[carrierID][monthID].add(data);
		// Liste der gefundenen Airports füllen; von Origin und Destination.
		if(!foundAirports.contains(origin)){	
			foundAirports.add(origin);
		}

		if(!foundAirports.contains(destination)){
			foundAirports.add(destination);
		}

		maxMonthID = monthID > maxMonthID? monthID : maxMonthID;
		
	}


	public LinkedList<String> getData(int i, int j) {
		return airFlightData[i][j];
	}
	
	
	
}
