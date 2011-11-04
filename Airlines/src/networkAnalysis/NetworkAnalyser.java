package networkAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import util.FindData;
import util.FlightDataObject;

public class NetworkAnalyser {




	public static void main(String[] args){

		new NetworkAnalyser().doAll();

	}

	private void doAll() {
		
		FindData findData = new FindData();
	
		LinkedList<FlightDataObject> sortedAirFlights = findData.findAndSortFlights();
		
		System.out.println("found and sorted the data");
		
		LinkedList<FlightDataObject> cleanFlightDataYearList = findData.aggregateWithinYear(sortedAirFlights);
		
		System.out.println("found and aggregated the data within a year ");
		
		Graph graph =   new Graph(cleanFlightDataYearList, -1, -1);
		
		System.out.println("Created the graph ");
		
		System.out.println("the graph has "+ConnectedComponents.findNumberOfConnectedComponents(graph)+" components");
		
		LinkedList<LinkedList<Integer>> components = ConnectedComponents.findConnectedComponents(graph);
		int componentCounter = 1;
		for(LinkedList<Integer> component : components){
			System.out.println("The "+findRightOrdeal(componentCounter)+" component consists of: ");
			++componentCounter;
			for(int airportID: component){
				System.out.println("   "+graph.idToAirport[airportID]);
			}
		}
		
		
		
	}
	
	
	public String findRightOrdeal(int number){
		int rest = number % 10;
		
		switch(rest){
		case 1: return number+"st";
		case 2: return number+"nd";
		case 3: return number+"rd";
		default: return number+"th";
		}
	}
	


}