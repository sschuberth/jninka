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
package org.whitesource.jninka;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.whitesource.jninka.model.ScanResults;
import org.whitesource.jninka.progress.ScanProgressListener;

/**
 * @author Rami.Sass
 */
public class Main {

	/**
	 * Main entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("usage: jninka <source-folder> <output-file>");
		} else {
			long time = System.currentTimeMillis();
			
			System.out.println("Source code directory is " + args[0]);
			System.out.println("Scan results file is " + args[1]);
			
			initLogging();
			
			try {
				JNinka ninka = new JNinka();
				ninka.getMonitor().addListener(new SysOutListener());

				System.out.println("Starting scan ...");
				File sourceFolder = new File(args[0]);
				ScanResults scanResults = ninka.scanFolder(sourceFolder, true);
				
				System.out.println(" finished.\nWriting results to file ...");
				scanResults.writeXML(new File(args[1]));
				
				System.out.println("Scan results found " + scanResults.getfindings().size() + " potential files." );
				
				time = (System.currentTimeMillis() - time) / 1000;
				System.out.println("Completed at " + time + " [sec]");
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/* --- Nested classes --- */
	
	/**
	 * Implementation of the interface using {@link System#out} to report progress.
	 */
	static class SysOutListener implements ScanProgressListener {
		@Override
		public void progress(int pct, String details) {
			System.out.print("\rScan progress: " + pct + " %");
		}
	}
	
	/* --- Private methods --- */
	
	/**
	 * The method initialize the application logging facilities.
	 */
	private static void initLogging() {
		Logger log = Logger.getLogger("jninka");
		log.fine("Loading logging configuration file ...");

		InputStream configFile = null; 
		try {
			configFile = Main.class.getResourceAsStream("/logging.properties");
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
	private Main() {
		// avoid instantiation
	}

}
