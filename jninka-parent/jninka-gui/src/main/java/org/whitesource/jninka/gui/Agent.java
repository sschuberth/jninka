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
package org.whitesource.jninka.gui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Rami.Sass
 */
public class Agent {

	/* --- Static members --- */
	
	private static final String LOGGING_PROPERTIES = "/logging.properties";
	
	private static final String APPLICATION_PROPERTIES = "/jninka.properties";

	private static String version = null;
	
	private static Logger logger;
	
	/* --- Main --- */
	
	/**
	 * Main entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		initLogging();
		
		logger = Logger.getLogger(Agent.class.getName());
		logger.info("********************            Starting JNinka             ********************");
		
		initProperties();
		
		final AgentPresenter presenter = new AgentPresenter();
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "General error", e);
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				presenter.setVersion(version);
				presenter.show();
				presenter.checkForUpdates();
			}
		});
	}
	
	/* --- Private methods --- */

	/**
	 * The method initializes the application properties.
	 */
	private static void initProperties() {
		logger.info("Loading properties file ...");
		InputStream propertiesFile = null;
		try {
			propertiesFile = Agent.class.getResourceAsStream(APPLICATION_PROPERTIES);
			Properties properties = new Properties();
			properties.load(propertiesFile);
			version = properties.getProperty("version");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading properties file");
		} finally {
			if (propertiesFile != null) {
				try {
					propertiesFile.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "Could not close resource", e);
				}
			}
		}
	}

	/**
	 * The method initialize the application logging facilities.
	 */
	private static void initLogging() {
		Logger log = Logger.getLogger("jninka");
		log.info("Loading logging configuration file ...");

		InputStream configFile = null; 
		try {
			configFile = Agent.class.getResourceAsStream(LOGGING_PROPERTIES);
		    LogManager.getLogManager().readConfiguration(configFile);
		} catch (IOException ex) {
		    System.out.println("WARNING: Could not open configuration file");
		    System.out.println("WARNING: Logging not configured (console output only)");
		} finally {
			if (configFile != null) {
				try {
					configFile.close();
				} catch (IOException e) {
					System.out.println("ERROR: Could not close configuration file");
					e.printStackTrace();
				}
			}
		}
	}
	
	/* --- Constructors --- */
	
	/**
	 * Private default constructor
	 */
	private Agent() {
		// avoid instantiation
	}

}
