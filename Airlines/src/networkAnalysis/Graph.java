package networkAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import util.FlightDataObject;

public class Graph {
	
	HashMap<String,Integer> airlineToID;
	String[] idToAirline;
//	String[] idToAirlineName;
	HashMap<String, Integer> airportToID;
	String[] idToAirport;
	
	//is good for asking efficiently: is there a connection between i and j
	ArrayList<Integer>[][] adjacencyMatrix_AirportAirport_Airlines;
	
	
	//Directed edge list of the graph
	ArrayList<OriginDestinationPair>[] flights;
	
	int maxNumberOfAirports = 2700;
	int maxNumberOfAirlines = 600;
	 int airlineCounter, airportCounter;
	
	
	 /**
	  * first void constructor
	  */
	public Graph(){

		initialize(-1, -1);
	}
	
	
	/**
	 * Constructor with known information at instantiation
	 * 
	 * @param flightsData: Careful: if the same airline makes multiple flights from A to B, the airline will be multiple times in the adjacency matrix
	 * @param numberOfAirports: maximal number of different airports in list. If not known, set to -1;
	 * @param numberOfAirlines: maximal number of different airlines in list. If not known, set to -1;
	 */
	public Graph(List<FlightDataObject> flightsData, int numberOfAirports, int numberOfAirlines){	
		
		//initialize
		initialize(numberOfAirports, numberOfAirlines);
				
		//build graph
		buildGraph(flightsData);
		
	}
	
	private void buildGraph(List<FlightDataObject> flightsData) {
		
		FlightDataObject fto;
		Integer airLineID, originID, destinationID;
		String origin, destination, carrier;
		
		for(ListIterator<FlightDataObject> it = flightsData.listIterator(); it.hasNext();){
			//next flight information
			fto = it.next();
			
			//which carrier
			carrier = fto.getCarrier();
			//try to find associated ID in the hashmap
			airLineID = airlineToID.get(carrier);
			
			//if the hashmap does not yet know this key (carrier): 
			if(airLineID == null){
				//assign ID to the carrier
				airLineID = airlineCounter;
				//remember assignment of carrier to airlineID
				airlineToID.put(carrier, airlineCounter);
				//remember the according carrier for this ID
				idToAirline[airlineCounter] = carrier;
				//make sure next carrier gets a new ID
				++airlineCounter;
			}
			
			origin = fto.getOrigin();
			originID = airportToID.get(origin);
			if(originID == null){
				originID = airportCounter;
				airportToID.put(origin, airportCounter);
				idToAirport[airportCounter] = origin;
				++airportCounter;
			}
			
			destination = fto.getDestination();
			destinationID = airportToID.get(destination);
			if(destinationID == null){
				destinationID = airportCounter;
				airportToID.put(destination, airportCounter);
				idToAirport[airportCounter] = destination;
				++airportCounter;				
			}
			
			adjacencyMatrix_AirportAirport_Airlines[originID][destinationID].add(airLineID);
			
			
			
			flights[airLineID].add(new OriginDestinationPair(origin, destination));
			
			
		}
		
		
	}
	
	
	
	public ArrayList<Integer>[] findSymmetricAdjacencyLists(){
		//is good for getting all other airports with which i is connected:
		//contains an edge between two airport if there is at least one airline flying from one to the other or back.
		ArrayList<Integer>[] airportsConnectedToThisAirport = new ArrayList[maxNumberOfAirports];
		for(int i = 0; i < airportsConnectedToThisAirport.length; ++i) airportsConnectedToThisAirport[i] = new ArrayList<Integer>(); 
		
		for(int i = 0; i < adjacencyMatrix_AirportAirport_Airlines.length; ++i){
			for(int j = i+1; j < adjacencyMatrix_AirportAirport_Airlines[i].length; ++j){
				if(adjacencyMatrix_AirportAirport_Airlines[i][j].size() > 0 || adjacencyMatrix_AirportAirport_Airlines[j][i].size() > 0){
					airportsConnectedToThisAirport[i].add(j);
					airportsConnectedToThisAirport[j].add(i);
				}								
			}
		}

		
		
		return airportsConnectedToThisAirport;
		
	}
	
	/**
	 * The airport's list lists all other airports to which at least one flight exists 
	 * @return
	 */
	public ArrayList<Integer>[] findDirectedAdjacencyLists(){
		//is good for getting all other airports with which i is connected:
		//contains an edge between two airport if there is at least one airline flying from one to the other or back.
		ArrayList<Integer>[] airportsConnectedToThisAirport = new ArrayList[maxNumberOfAirports];
		for(int i = 0; i < airportsConnectedToThisAirport.length; ++i) airportsConnectedToThisAirport[i] = new ArrayList<Integer>(); 
		
		for(int i = 0; i < adjacencyMatrix_AirportAirport_Airlines.length; ++i){
			for(int j = i+1; j < adjacencyMatrix_AirportAirport_Airlines[i].length; ++j){
				if(adjacencyMatrix_AirportAirport_Airlines[i][j].size() > 0)
					airportsConnectedToThisAirport[i].add(j);
				if(adjacencyMatrix_AirportAirport_Airlines[j][i].size() > 0)
					airportsConnectedToThisAirport[j].add(i);
										
			}
		}

		
		
		return airportsConnectedToThisAirport;
		
	}
	
	


	private void initialize( int numberOfAirports, int numberOfAirlines) {
		maxNumberOfAirports = numberOfAirports == -1 ? maxNumberOfAirports : numberOfAirports;
		maxNumberOfAirlines = numberOfAirlines == -1 ? maxNumberOfAirlines : numberOfAirlines; 
		
		airlineToID = new HashMap<String, Integer>();
		idToAirline = new String[maxNumberOfAirlines];
		
		airportToID = new HashMap<String, Integer>();
		idToAirport = new String[maxNumberOfAirports];
		
		
		adjacencyMatrix_AirportAirport_Airlines = new ArrayList[maxNumberOfAirports][maxNumberOfAirports];
		for(int i = 0; i < adjacencyMatrix_AirportAirport_Airlines.length; ++i){
			for(int j = 0; j < adjacencyMatrix_AirportAirport_Airlines[i].length; ++j)
				adjacencyMatrix_AirportAirport_Airlines[i][j] = new ArrayList<Integer>();
		}
		
		
		flights = new ArrayList[maxNumberOfAirlines];
		for(int i = 0; i < flights.length; ++i)
			flights[i] = new ArrayList<OriginDestinationPair>();
		
		airlineCounter = 0;
		airportCounter = 0;

	}

	
	
	
	
	
	

}
