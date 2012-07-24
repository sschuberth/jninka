/**
 *  Copyright (C) 2012 White Source (www.whitesourcesoftware.com)
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This patch is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this patch.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	
	private static final String UPDATE_URL_KEY = "wss.update.url";
	
	private static final String DEFAULT_UPDATE_URL = "http://saas.whitesourcesoftware.com/jninkaUpdate";
	
	private static final String JNINKA_VERSION_PARAM = "version";
	
	private static final String VERSION_UP_TO_DATE = "VERSION-UP-TO-DATE";
	
	private static final String ENCODING_UTF8 = "UTF-8";
	
	private static final int CONNECTION_TIMEOUT = 60000;
	
	private static final Logger logger = Logger.getLogger(JNinkaUpdateClient.class.getCanonicalName());
	
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
			// create request
			StringBuilder request = new StringBuilder();
			addParamter(request, JNINKA_VERSION_PARAM, currentVersion);
			
			// init connection
			connection = initConnection();
			
			// send request
			response = sendRequest(connection, request);
			
			// handle response
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
		String updateUrl = System.getProperty(UPDATE_URL_KEY, DEFAULT_UPDATE_URL);
		URL url = new URL(updateUrl);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setReadTimeout(CONNECTION_TIMEOUT);
		
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
