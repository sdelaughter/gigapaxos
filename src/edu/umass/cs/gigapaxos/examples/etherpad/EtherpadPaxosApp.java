package edu.umass.cs.gigapaxos.examples.etherpad;

import java.util.HashMap;
import java.util.Set;
import edu.umass.cs.gigapaxos.examples.etherpad.ReadPort;
import edu.umass.cs.gigapaxos.examples.PaxosAppRequest;
import edu.umass.cs.gigapaxos.interfaces.Replicable;
import edu.umass.cs.gigapaxos.interfaces.Request;
import edu.umass.cs.gigapaxos.paxospackets.RequestPacket;
import edu.umass.cs.nio.interfaces.IntegerPacketType;
import edu.umass.cs.reconfiguration.examples.noop.NoopApp;
import edu.umass.cs.reconfiguration.reconfigurationutils.RequestParseException;
import net.gjerull.etherpad.client.EPLiteClient;
import java.io.*;
import org.json.*;


//import java.io.FileReader;
//import java.util.Iterator;
 
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

/**
 * @author Sam DeLaughter
 *
 * @param args
 * @throws IOException
 * @throws JSONException
 */

public class EtherpadPaxosApp implements Replicable {

	final static String port = getPort();
	final static String hostName = "http://localhost:" + port;
	final static String apiKey = "1c0bf70295313687cfdc2a4b839c1b91386d385418961a6fcef1dee457c92c75";
	final static EPLiteClient client = new EPLiteClient(hostName, apiKey);
	final static String delimiter = ",";

	@Override
	public boolean execute(Request request) {
		//System.out.println(hostName);

		// execute request here
		//System.out.println("L1 Stop: " + System.currentTimeMillis());
		String requestString = ((RequestPacket) request).requestValue;
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

	@Override
	public boolean execute(Request request,
			boolean doNotReplyToClient) {
		// execute request without replying back to client

		// identical to above unless app manages its own messaging
		return this.execute(request);
	}

	@Override
	public String checkpoint(String name) {
		// should return checkpoint state here
		return null;
	}

	@Override
	public boolean restore(String name, String state) {
		// should update checkpoint state here for name
		return true;
	}

	/**
	 * Needed only if app uses request types other than RequestPacket. Refer
	 * {@link NoopApp} for a more detailed example.
	 */
	@Override
	public Request getRequest(String stringified)
			throws RequestParseException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Needed only if app uses request types other than RequestPacket. Refer
	 * {@link NoopApp} for a more detailed example.
	 */
	@Override
	public Set<IntegerPacketType> getRequestTypes() {
		// TODO Auto-generated method stub
		return null;
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
				client.setText(components[2], port); //components[3]);
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

	private static String getPort() {
		String jsonData = readFile("settings.json");
		int port = 9001;
		try{
			JSONObject jobj = new JSONObject(jsonData);
			port = jobj.getInt("port");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return Integer.toString(port);
	}


	private static String readFile(String filename) {
                String result = "";
                try {
                     	BufferedReader br = new BufferedReader(new FileReader(filename));
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();
                        while (line != null) {
                                sb.append(line);
                                line = br.readLine();
                        }
                        result = sb.toString();
                } catch(Exception e) {
                        e.printStackTrace();
                }
                return result;
        }	
}
