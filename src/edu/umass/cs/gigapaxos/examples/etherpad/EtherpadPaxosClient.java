package edu.umass.cs.gigapaxos.examples.etherpad;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.PaxosClientAsync;
import edu.umass.cs.gigapaxos.PaxosConfig;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.gigapaxos.interfaces.RequestCallback;

/**
 * @author Sam DeLaughter
 * 
 *         A simple client for EtherpadPaxosApp.
 *         Takes one argument for the number of requests to send.
 */
public class EtherpadPaxosClient extends PaxosClientAsync {
	
	/*final static String request1 = "createPad,paxos_test2";
	final static String request2 = "setText,paxos_test2,bar";
	final static String request3 = "getText,paxos_test2";
	final static String request4 = "deletePad,paxos_test";
	*/
	
	static int numResponses = 0;
	
	/**
	 * @throws IOException
	 */
	public EtherpadPaxosClient() throws IOException {
		super();
	}

	/**
	 * A simple example of asynchronously sending a few requests with a callback
	 * method that is invoked when the request has been executed or is known to
	 * have failed.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws IOException, JSONException {
		EtherpadPaxosClient etherpadClient = new EtherpadPaxosClient();
		
		final int numRequests = Integer.parseInt(args[0]);
		final String requestContent = ",setText,foo,bar";
		final int minDelay = 125;
		final int maxDelay = 250; 
	
		long[] startTimes = new long[numRequests];
		long[] endTimes = new long[numRequests];
		long[] durations = new long[numRequests];
		
		for (int i = 0; i < numRequests; i++) {
			long delayDuration = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);
			
			try {
				Thread.sleep(delayDuration);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			
			final String requestValue = i + requestContent;
			startTimes[i] = System.nanoTime();
			
			/*
			etherpadClient.sendRequest(PaxosConfig.application.getSimpleName()+"0",
					requestValue, new RequestCallback() {
			*/
			//InetSocketAddress server = etherpadClient.servers[0];
			//System.out.println(server);
			//System.out.println("L1 Start: " + System.currentTimeMillis());
			
			etherpadClient.sendRequest(PaxosConfig.application.getSimpleName()+"0",
					requestValue, new RequestCallback() {
						@Override
						public void handleResponse(Request response) {
							//System.out.println("L6 Stop: " + System.currentTimeMillis());
							long endTime =System.nanoTime();
							int requestNumber = Integer.parseInt((requestValue.split(","))[0]);
							endTimes[requestNumber] = endTime;
							numResponses++;
							/*
							System.out.println(endTime + 
												" Response for request ["
												+ requestValue
												+ "] = "
												+ (response instanceof InterfaceClientRequest ? ((InterfaceClientRequest) response)
													.getResponse() : null));
							*/
							if(numResponses == numRequests) {
								for(int i = 0; i < numRequests; i++){
									durations[i] = endTimes[i] - startTimes[i];
								}
								long sum = 0;
								for(int i = 0; i < numRequests; i++){
									sum += durations[i];
								}
								long averageDuration = sum / numRequests;
								System.out.println("Average Delay: " + averageDuration + " nanoseconds");
							}
						}
					});
		}
	}
}
