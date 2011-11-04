package networkAnalysis;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConnectedComponents {
	
	/**
	 * Gives the number of connected components in the graph
	 * 
	 * @param graph
	 * @return number of connected components
	 */
	static int findNumberOfConnectedComponents(Graph graph){
		boolean[] hasBeenSeenBefore = new boolean[graph.airportCounter];
		ArrayList<Integer>[] airportsConnectedToThisAirport = graph.findSymmetricAdjacencyLists();
		
		
		LinkedList<Integer> currentAirports;
		int currentAirport;
		int componentCounter = 0;
		
		for(int i = 0; i < hasBeenSeenBefore.length; ++i){
			if(!hasBeenSeenBefore[i]){
				componentCounter++;
				currentAirports = new LinkedList<Integer>();
				currentAirports.add(i);
				hasBeenSeenBefore[i] = true;
				
				while(currentAirports.size()>0){
					currentAirport = currentAirports.remove(0);
					for(int neighborOfCurrentAirport : airportsConnectedToThisAirport[currentAirport]){
						if(!hasBeenSeenBefore[neighborOfCurrentAirport]){
							hasBeenSeenBefore[neighborOfCurrentAirport] = true;
							currentAirports.add(neighborOfCurrentAirport);
						}
					}				
				}				
			}
		}
		return componentCounter;		
	}

	static LinkedList<LinkedList<Integer>> findConnectedComponents(Graph graph){
		boolean[] hasBeenSeenBefore = new boolean[graph.airportCounter];
		LinkedList<Integer> currentAirports;
		LinkedList<LinkedList<Integer>> components = new LinkedList<LinkedList<Integer>>();
		ArrayList<Integer>[] airportsConnectedToThisAirport = graph.findSymmetricAdjacencyLists();
		
		
		int currentAirport;
		int componentCounter = 0;
		
		for(int i = 0; i < hasBeenSeenBefore.length; ++i){
			if(!hasBeenSeenBefore[i]){
				componentCounter++;
				currentAirports = new LinkedList<Integer>();
				LinkedList<Integer> component = new LinkedList<Integer>();
				currentAirports.add(i);
				component.add(i);
				hasBeenSeenBefore[i] = true;
				
				while(currentAirports.size()>0){
					currentAirport = currentAirports.remove(0);
					for(int neighborOfCurrentAirport : airportsConnectedToThisAirport[currentAirport]){
						if(!hasBeenSeenBefore[neighborOfCurrentAirport]){
							hasBeenSeenBefore[neighborOfCurrentAirport] = true;
							currentAirports.add(neighborOfCurrentAirport);
							component.add(neighborOfCurrentAirport);
						}
					}				
				}
				
				//I have found all airports belonging to the same connected component, now, add component to the list of components;
				components.add(component);
			}
		}
		return components;		
	}

}
