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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * @author Rami.Sass
 */
public class Agent {

	/**
	 * Main entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		initLogging();
		
		Logger log = Logger.getLogger(Agent.class.getName());
		log.info("********************            Starting JNinka             ********************");
		
		final AgentPresenter presenter = new AgentPresenter();
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "General error", e);
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				presenter.show();
			}
		});
	}
	
	/* --- Private mehtods --- */

	/**
	 * The method initialize the application logging facilities.
	 */
	private static void initLogging() {
		Logger log = Logger.getLogger("jninka");
		log.setLevel(Level.ALL);
		log.info("Loading logging configuration file ...");

		InputStream configFile = null; 
		try {
			configFile = Agent.class.getResourceAsStream("/logging.properties");
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
