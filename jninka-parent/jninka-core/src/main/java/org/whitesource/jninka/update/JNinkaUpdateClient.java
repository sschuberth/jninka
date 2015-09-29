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

import org.whitesource.jninka.JNinkaUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private static final String UPDATE_URL_KEY = "wss.url";

    private static final String DEFAULT_UPDATE_URL = "http://saas.whitesourcesoftware.com/jninkaUpdate";

    private static final String JNINKA_VERSION_PARAM = "version";

    private static final String VERSION_UP_TO_DATE = "VERSION-UP-TO-DATE";

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

        // call service
        HttpURLConnection connection = null;
        try {
            connection = initConnection();
            response = sendRequest(connection, JNINKA_VERSION_PARAM + "=" + currentVersion);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error checking for update", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return handleResponse(response);
    }

    /* --- Private methods --- */

    private HttpURLConnection initConnection() throws IOException {
        String updateUrl = System.getProperty(UPDATE_URL_KEY, DEFAULT_UPDATE_URL);
        URL url = new URL(updateUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);

        return connection;
    }

    /**
     * Writes the data to the connection and reads response.
     *
     * @param connection
     * @param path
     *
     * @return
     *
     * @throws IOException
     */
    private String sendRequest(HttpURLConnection connection, String path) throws IOException {
        String response = "";

        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        try {
            logger.log(Level.INFO, "Sending request");
            writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(path);
            writer.flush();

            logger.log(Level.INFO, "Reading response");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            response = sb.toString();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Problem sending request or reading response", e);
        } finally {
            JNinkaUtils.close(writer, logger);
            JNinkaUtils.close(reader, logger);
        }

        return response;
    }

    private String handleResponse(String response) {
        if (VERSION_UP_TO_DATE.equals(response)) {
            logger.log(Level.INFO, "Version is up to date");
            response = "";
        } else {
            try {
                new URL(response); // validates download URL
                logger.log(Level.INFO, "Valid download url " + response);
            } catch (MalformedURLException e) {
                response = "";
                logger.log(Level.INFO, "Invalid download url " + response);
            }
        }

        return response;
    }

}
