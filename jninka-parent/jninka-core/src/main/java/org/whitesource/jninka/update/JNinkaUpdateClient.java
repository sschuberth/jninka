package org.whitesource.jninka.update;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP client for JNinka update requests.
 * 
 * @author tom.shapira
 *
 */
public class JNinkaUpdateClient  {

	/* --- Static members --- */
	
	private static final Logger logger = Logger.getLogger(JNinkaUpdateClient.class.getCanonicalName());
	
	private static final String JNINKA_UPDATE_URL = "http://localhost:8888/Wss/jninkaUpdate";
	
	private static final String JNINKA_VERSION_PARAM = "version";
	
	private static final String ENCODING_UTF8 = "UTF-8";
	
	private static final int TIMEOUT = 60000;
	
	private static final String VERSION_UP_TO_DATE = "VERSION-UP-TO-DATE";
	
	/* --- Public methods --- */
	
	/**
	 * Send an update request to WSS.
	 * 
	 * @return Link to download page or null in case version is up to date or an error occurred.
	 */
	public String checkForUpdate(String currentVersion) {
		String response = "";
		HttpURLConnection connection = null;
		try {
			// init connection
			connection = initConnection();
			
			// create request
			StringBuilder request = new StringBuilder();
			addParamter(request, JNINKA_VERSION_PARAM, currentVersion);
			
			// send request
			response = sendRequest(connection, request);
			if (response.equals(VERSION_UP_TO_DATE)) {
				logger.log(Level.INFO, "Version is up to date");
				response = "";
			} else {
				try {
					// try parsing URL
					new URL(response);
					logger.log(Level.INFO, "Valid download url " + response);
				} catch (MalformedURLException e) {
					response = "";
					logger.log(Level.INFO, "Invalid download url " + response);
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem checking for update", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	    return response;
	}

	/* --- Private methods --- */
	
	private HttpURLConnection initConnection()
			throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(JNINKA_UPDATE_URL);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);
		
		return connection;
	}
	
	/**
	 * Writes the data to the connection and reads response.
	 * 
	 * @param connection
	 * @param request
	 * 
	 * @return 
	 * 
	 * @throws IOException
	 */
	private String sendRequest(HttpURLConnection connection, StringBuilder request)
			throws IOException {
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder result = new StringBuilder();
		
		try {
			logger.log(Level.INFO, "Sending request");
			wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(request.toString());
			wr.flush();
			
			logger.log(Level.INFO, "Reading response");
			rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem sending request or reading response", e);
		} finally {
			// close resources
			closeResource(wr);
			closeResource(rd);
		}
	    
		return result.toString();
	}

	private void closeResource(Closeable resource) throws IOException {
		if (resource != null) {
			resource.close();
		}
	}

	/**
	 * Adds a Http request parameter to the data sent.
	 * 
	 * @param data
	 * @param key
	 * @param value
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private void addParamter(StringBuilder data, String key, String value)
			throws UnsupportedEncodingException {
		data.append(URLEncoder.encode(key, ENCODING_UTF8));
		data.append("=");
		data.append(URLEncoder.encode(value, ENCODING_UTF8));
	}

}
