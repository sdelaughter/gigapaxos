package edu.umass.cs.gigapaxos.examples.etherpad;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.gigapaxos.interfaces.RequestCallback;
import edu.umass.cs.reconfiguration.ReconfigurableAppClientAsync;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.reconfigurationpackets.CreateServiceName;

/**
 * @author arun
 *
 */
public class EtherpadReconfigurableClient extends ReconfigurableAppClientAsync {

	
	/*Must match the delimiter set in the App*/
	final String delimiter = ",";
	final String requestContent = delimiter + "setText" + delimiter + "foo" + delimiter + "bar";
	
	final static int minDelay = 125;
	final static int maxDelay = 250; 
	
	final static int numNames = 1;
	final static int numRequests = 10;
	static Map<String, Integer> numResponses = new HashMap<String, Integer>();
	
	/**
	 * @throws IOException
	 */
	public EtherpadReconfigurableClient() throws IOException {
		super();
	}

	private void testSendBunchOfRequests(String name)
			throws IOException, JSONException {
	
		long[] startTimes = new long[numRequests];
		long[] endTimes = new long[numRequests];
		long[] durations = new long[numRequests];
		
		System.out.println("Created " + name
				+ " and beginning to send test requests");
		for (int i = 0; i < numRequests; i++) {
			final String requestValue = i + requestContent;
			startTimes[i] = System.nanoTime();
			
			//InetSocketAddress server = etherpadClient.servers[0];
			
			this.sendRequest(new AppRequest(name, requestValue,
					AppRequest.PacketType.DEFAULT_APP_REQUEST, false),
					new RequestCallback() {

						@Override
						public void handleResponse(Request response) {
							int count = numResponses.get(name);
							count++;
							numResponses.put(name, count);
							int requestNumber = Integer.parseInt((requestValue.split(","))[0]);
							endTimes[requestNumber] = System.nanoTime();
							
							if(numResponses.get(name) == numRequests) {
								for(int i = 0; i < numRequests; i++){
									durations[i] = endTimes[i] - startTimes[i];
								}
								long sum = 0;
								for(int i = 0; i < numRequests; i++){
									sum += durations[i];
								}
								long averageDuration = sum / numRequests;
								System.out.println("Average Delay for " + name + ": " + averageDuration + " nanoseconds");
							}
						}
					});
			
			long delayDuration = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);
			try {
				Thread.sleep(delayDuration);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * This simple client creates a bunch of names and sends a bunch of requests
	 * to each of them. Refer to the parent class
	 * {@link ReconfigurableAppClientAsync} for other utility methods available
	 * to this method or to know how to write your own client.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final EtherpadReconfigurableClient client = new EtherpadReconfigurableClient();
		String namePrefix = "group_";
		String initialState = "some_default_initial_state";
		
		for (int i = 0; i < numNames; i++) {
			final String name = namePrefix +
					+ ((int) (Math.random() * Integer.MAX_VALUE));
			numResponses.put(name, 0);
			client.sendRequest(new CreateServiceName(name, initialState),
					new RequestCallback() {

						@Override
						public void handleResponse(Request response) {
							try {
								client.testSendBunchOfRequests(name);
							} catch (IOException | JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}
}
