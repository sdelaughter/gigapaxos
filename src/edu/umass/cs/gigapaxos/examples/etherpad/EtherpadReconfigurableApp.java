/*
 * Copyright (c) 2015 University of Massachusetts
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Initial developer(s): V. Arun
 */
package edu.umass.cs.gigapaxos.examples.etherpad;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gigapaxos.InterfaceClientMessenger;
import edu.umass.cs.gigapaxos.InterfaceReplicable;
import edu.umass.cs.gigapaxos.InterfaceRequest;
import edu.umass.cs.gigapaxos.examples.PaxosAppRequest;
import edu.umass.cs.gigapaxos.paxospackets.RequestPacket;
import edu.umass.cs.nio.IntegerPacketType;
import edu.umass.cs.nio.InterfaceSSLMessenger;
import edu.umass.cs.reconfiguration.Reconfigurator;
import edu.umass.cs.reconfiguration.examples.AbstractReconfigurablePaxosApp;
import edu.umass.cs.reconfiguration.examples.AppRequest;
import edu.umass.cs.reconfiguration.examples.AppRequest.ResponseCodes;
import edu.umass.cs.reconfiguration.interfaces.InterfaceReconfigurable;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;
import net.gjerull.etherpad.client.EPLiteClient;

/**
 * @author V. Arun
 * 
 *         A simple no-op application example.
 */
public class EtherpadReconfigurableApp extends AbstractReconfigurablePaxosApp<String> implements
		InterfaceReplicable, InterfaceReconfigurable, InterfaceClientMessenger {

	private static final String DEFAULT_INIT_STATE = "";
	
	/*Configure Etherpad variables here*/
	final static String hostName = "http://localhost:9001";
	final static String apiKey = "1c0bf70295313687cfdc2a4b839c1b91386d385418961a6fcef1dee457c92c75";
	final static EPLiteClient client = new EPLiteClient(hostName, apiKey);
	
	/*Must match the delimiter set in the Client*/
	final static String delimiter = ",";

	private class AppData {
		final String name;
		String state = DEFAULT_INIT_STATE;

		AppData(String name, String state) {
			this.name = name;
			this.state = state;
		}

		void setState(String state) {
			this.state = state;
		}

		String getState() {
			return this.state;
		}
	}

	private String myID; // used only for pretty printing
	private final HashMap<String, AppData> appData = new HashMap<String, AppData>();
	// only address based communication needed in app
	private InterfaceSSLMessenger<?, JSONObject> messenger;

	/**
	 * Default constructor used to create app replica via reflection.
	 */
	public EtherpadReconfigurableApp() {
	}

	// Need a messenger mainly to send back responses to the client.
	@Override
	public void setClientMessenger(InterfaceSSLMessenger<?, JSONObject> msgr) {
		this.messenger = msgr;
		this.myID = msgr.getMyID().toString();
	}

	@Override
	public boolean handleRequest(InterfaceRequest request,
			boolean doNotReplyToClient) {
		// execute request here
				//System.out.println("L1 Stop: " + System.currentTimeMillis());
				String requestString = ((AppRequest) request).getValue();
				//System.out.println("Handling requestValue: " + requestString);
				String responseString = "null";
				//long startTime = System.nanoTime();
				//System.out.println("Parse Start: " + System.currentTimeMillis());
				try{
					responseString = parseRequestString(requestString);
				} catch (Exception e) {
					//System.out.println(e.getMessage());
					responseString = e.getMessage();
				}
				//System.out.println("Parse Stop: " + System.currentTimeMillis());
				//long endTime = System.nanoTime();
				//long duration = endTime - startTime;
				//System.out.println("Duration: " + duration);
				
				//System.out.println("Got responseValue: " + responseString);
				
				// set response if request instanceof InterfaceClientRequest
				if (request instanceof RequestPacket)
					((RequestPacket) request).setResponse(responseString);
				if (request instanceof PaxosAppRequest)
					((PaxosAppRequest) request).setResponse(responseString);
				//System.out.println("L6 Start: " + System.currentTimeMillis());
				return true;
	}

	private static final boolean DELEGATE_RESPONSE_MESSAGING = true;

	private boolean processRequest(AppRequest request,
			boolean doNotReplyToClient) {
		if (request.getServiceName() == null)
			return true; // no-op
		if (request.isStop())
			return processStopRequest(request);
		AppData data = this.appData.get(request.getServiceName());
		if (data == null) {
			System.out.println("App-" + myID + " has no record for "
					+ request.getServiceName() + " for " + request);
			return false;
		}
		assert (data != null);
		data.setState(request.getValue());
		this.appData.put(request.getServiceName(), data);
		System.out.println("App-" + myID + " wrote to " + data.name
				+ " with state " + data.getState());
		if (DELEGATE_RESPONSE_MESSAGING)
			this.sendResponse(request);
		else
			sendResponse(request, doNotReplyToClient);
		return true;
	}

	/**
	 * This method exemplifies one way of sending responses back to the client.
	 * A cleaner way of sending a simple, single-message response back to the
	 * client is to delegate it to the replica coordinator, as exemplified below
	 * in {@link #sendResponse(AppRequest)} and supported by gigapaxos.
	 * 
	 * @param request
	 * @param doNotReplyToClient
	 */
	private void sendResponse(AppRequest request, boolean doNotReplyToClient) {
		assert (this.messenger != null && this.messenger.getClientMessenger() != null);
		if (this.messenger == null || doNotReplyToClient)
			return;

		InetSocketAddress sockAddr = request.getSenderAddress();
		try {
			this.messenger.getClientMessenger().sendToAddress(sockAddr,
					request.toJSONObject());
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	private void sendResponse(AppRequest request) {
		// set to whatever response value is appropriate
		request.setResponse(ResponseCodes.ACK.toString());
	}

	// no-op
	private boolean processStopRequest(AppRequest request) {
		return true;
	}

	@Override
	public InterfaceRequest getRequest(String stringified)
			throws RequestParseException {
		AppRequest request = null;
		if (stringified.equals(InterfaceRequest.NO_OP)) {
			return this.getNoopRequest();
		}
		try {
			request = new AppRequest(new JSONObject(stringified));
		} catch (JSONException je) {
			Reconfigurator.getLogger().fine("App-" + 
					myID + " unable to parse request " + stringified);
			throw new RequestParseException(je);
		}
		return request;
	}

	/*
	 * This is a special no-op request unlike any other NoopAppRequest.
	 */
	private InterfaceRequest getNoopRequest() {
		return new AppRequest(null, 0, 0, InterfaceRequest.NO_OP,
				AppRequest.PacketType.DEFAULT_APP_REQUEST, false);
	}

	private static AppRequest.PacketType[] types = {
			AppRequest.PacketType.DEFAULT_APP_REQUEST,
			AppRequest.PacketType.ANOTHER_APP_REQUEST };

	@Override
	public Set<IntegerPacketType> getRequestTypes() {
		return new HashSet<IntegerPacketType>(Arrays.asList(types));
	}

	@Override
	public boolean handleRequest(InterfaceRequest request) {
		return this.handleRequest(request, false);
	}

	@Override
	public String getState(String name) {
		AppData data = this.appData.get(name);
		return data != null ? data.getState() : null;
	}

	@Override
	public boolean updateState(String name, String state) {
		AppData data = this.appData.get(name);
		/*
		 * If no previous state, set epoch to initial epoch, otherwise
		 * putInitialState will be called.
		 */

		if (data == null && state != null) {
			data = new AppData(name, state);
			System.out.println(">>>App-" + myID + " creating " + name
					+ " with state " + state);
		} else if (state == null) {
			if (data != null)
				System.out.println("App-" + myID + " deleting " + name
						+ " with final state " + data.state);
			this.appData.remove(name);
			assert (this.appData.get(name) == null);
		} else if (data != null && state != null) {
			System.out.println("App-" + myID + " updating " + name
					+ " with state " + state);
			data.state = state;
		} else
			// do nothing when data==null && state==null
			;
		if (state != null)
			this.appData.put(name, data);

		return true;
	}
	
	private static String parseRequestString(String requestString) {
		String responseString = "null";
		if(!(requestString.contains(delimiter))){
			//System.out.println("No valid delimiters found in requestString.  Please use " + delimiter + " to separate components");
			//System.out.println("Request was: " + requestString);
			return null;
		}
		String[] components = requestString.split(delimiter);
		/*
		if(components.legth < 1) {
			System.out.println("Warning: Received Empty Request -- Ignoring");
			return null;
		}
		*/
		
		String requestType = components[1];
		if(requestType.equals("createPad")) {
			/*
			if (components.length != 2){
				System.out.println("WARNING: The createPad request type requires exactly 1 argument but received " + (components.length - 1));
				System.out.println("Components received were: " + components);
				return null;
			} else{
			*/
				client.createPad(components[2]);
				responseString = ("Successfully created new pad named: " + components[2]);
			//}
		} else if(requestType.equals("deletePad")) {
				client.deletePad(components[2]);
				responseString = ("Successfully deleted pad: " + components[2]);
		
		} else if(requestType.equals("setText")) {
				client.setText(components[2], components[3]);
				responseString = ("Successfully set text of pad " + components[2] + " to " + components[3]);

		} else if(requestType.equals("getText")) {
				HashMap response = client.getText(components[1]);
				responseString = response.toString();
				
		} else {
			//System.out.println("Warning, invalid request type: " + requestType);
			return responseString;
		}
		return responseString;
	}
}
