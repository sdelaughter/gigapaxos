package edu.umass.cs.gigapaxos.examples.etherpad;

import java.io.IOException;

import org.json.JSONException;

import edu.umass.cs.gigapaxos.InterfaceClientRequest;
import edu.umass.cs.gigapaxos.InterfaceRequest;
import edu.umass.cs.gigapaxos.PaxosClientAsync;
import edu.umass.cs.gigapaxos.PaxosConfig;
import edu.umass.cs.gigapaxos.RequestCallback;

/**
 * @author arun
 * 
 *         A simple client for NoopApp.
 */
public class NoopClient extends PaxosClientAsync {
	static int numResponses = 0;

	/**
	 * @throws IOException
	 */
	public NoopClient() throws IOException {
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
		NoopClient noopClient = new NoopClient();
		
		final int numRequests = Integer.parseInt(args[0]);
		long[] startTimes = new long[numRequests];
		long[] endTimes = new long[numRequests];
		long[] durations = new long[numRequests];
		
		for(int i = 0; i < numRequests; i++) {
			//startTimes[i] = System.nanoTime();
			final String requestValue = "hello world" + i;
			//System.out.println()
			noopClient.sendRequest(PaxosConfig.application.getSimpleName()+"0",
					requestValue, new RequestCallback() {

						@Override
						public void handleResponse(InterfaceRequest response) {
							long endTime = System.nanoTime();
							numResponses++;
							int j = Integer.parseInt(requestValue.substring(requestValue.length() - 1));
							endTimes[j] = endTime;
							System.out
									.println("Response for request ["
											+ requestValue
											+ "] = "
											+ (response instanceof InterfaceClientRequest ? ((InterfaceClientRequest) response)
													.getResponse() : null));
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
