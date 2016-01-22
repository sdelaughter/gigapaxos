package edu.umass.cs.gigapaxos.examples.etherpad;

import java.util.HashMap;
import net.gjerull.etherpad.client.EPLiteClient;

public class BasicEtherpadClient {
	
	final static String apiKey = "1c0bf70295313687cfdc2a4b839c1b91386d385418961a6fcef1dee457c92c75";
	final static String hostName = "http://localhost:9001";

	public static void main(String[] args){
		EPLiteClient client = new EPLiteClient(hostName, apiKey);
		int numRequests = Integer.parseInt(args[0]);
		long[] startTimes = new long[numRequests];
		long[] endTimes = new long[numRequests];
		long[] durations = new long[numRequests];
		
		for(int i = 0; i < numRequests; i++) {
			startTimes[i] = System.nanoTime();
			client.setText("foo", "bar");
			endTimes[i] = System.nanoTime();
		}
		
		for(int i = 0; i < numRequests; i++) {
			durations[i] = endTimes[i] - startTimes[i];
		}
		
		long sum = 0;
		for(int i = 0; i < numRequests; i++) {
			sum += durations[i];
		}
		
		long averageDuration = sum / numRequests;
		System.out.println("Average Delay: " + averageDuration + " nanoseconds");
		
	}
}