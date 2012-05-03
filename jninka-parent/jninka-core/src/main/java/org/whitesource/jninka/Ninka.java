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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.whitesource.jninka.model.CodeFileAttributions;
import org.whitesource.jninka.model.LicenseAttribution;
import org.whitesource.jninka.model.ScanResults;
import org.whitesource.jninka.progress.ScanProgressMonitor;
import org.whitesource.jninka.resources.ResourceHandler;

/**
 * Ninka
 * Original Authors: Daniel M German and Y. Manabe
 *
 * This program is originally based on the Ninka program
 * published by Daniel M German. at:
 * http://ninka.turingmachine.org/ 
 * available form:
 * https://github.com/dmgerman/ninka
 *
 * Modifications to the original by White Source,
 * which are under the following license:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This patch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this patch.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Ninka {
	
	// logger
	
	static private Logger logger = Logger.getLogger(Ninka.class.getCanonicalName());
	
	// members
	
	private ScanProgressMonitor monitor;
	private ResourceHandler resourceHandler;
	
	private FileFilter dirFilter;
	private FileFilter javaFilter;
	private Set<String> codeFileExtentions;
	
	private ExtComments extComments;
	private Splitter splitter;
	private Filter filter;
	private Senttok senttok;
	private boolean getUnknowns;
	
	// constructor
	
	public Ninka(){
		initCodeFileExtentions();
		dirFilter = getDirectoryFilter();
		javaFilter = getCodeFileFilter();
		resourceHandler = new ResourceHandler();
		extComments = new ExtComments();
		splitter = new Splitter();
		splitter.setDictionary(resourceHandler.getAsStream("splitter.dict"));
		splitter.setAbbrvFile(resourceHandler.getAsStream("splitter.abv"));
		filter = new Filter();
		filter.setCritWords(resourceHandler.getAsStream("criticalword.dict"));
		senttok = new Senttok();
		senttok.setLicSentences(resourceHandler.getAsStream("licensesentence.dict"));
		monitor = new ScanProgressMonitor();
	}
	
	public ScanResults scanFolderRecursive(File folder, boolean getUnknowns){
		ScanResults result = new ScanResults();
		this.getUnknowns = getUnknowns;
		int folderCount = countFoldersRecursive(folder);
		monitor.reset();
		monitor.setParams(folderCount, 30);
		runRecursive(folder, result);
		return result;
	}
	
	private int runRecursive(File directory, ScanResults result){
		int count = 0;
		monitor.progress(1, directory.getAbsolutePath());
		File[] javaFiles = directory.listFiles(javaFilter);
		for(File javaFile : javaFiles){
			run(javaFile, result);
			count++;
		}
		File[] children = directory.listFiles(dirFilter);
		for(File child : children){
			count += runRecursive(child, result);
		}
		return count;
	}
	
	public void run(File codeFile, ScanResults scanResult){
		boolean result;
		try {
			// Stage 1.
			extComments.setInputFile(codeFile.getAbsolutePath());
			result = extComments.process();
			if (result) {
				// Stage 2.
				splitter.setInputInfo(extComments.getOutputInfo());
				result = splitter.process();
				if (result) {
					// Stage 3.
					filter.setInputInfo(splitter.getOutputInfo());
					result = filter.process();
					if (result) {
						// Stage 4.
						// senttok.setInputInfo(filter.getGoodOutputInfo());
						senttok.setTooLong(70);
						List<LicenseAttribution> attributions = senttok.getAttributions(filter.getGoodOutputInfo(), getUnknowns);
						if (attributions.size() > 0) {
							handleHit(codeFile, scanResult, attributions);
						}
					} else {
						logger.severe("filter failed");
					}
				} else {
					logger.severe("splitter failed");
				}
			} else {
				logger.severe("extract-comments failed");
			}
		} catch (Exception e) {
			logger.severe(codeFile.getAbsolutePath() + " - " + e.getMessage());
		}
	}

	private void handleHit(File codeFile, ScanResults scanResult, List<LicenseAttribution> attributions) {
		CodeFileAttributions fileAttributions;
		String pkg;
		fileAttributions = new CodeFileAttributions(attributions, codeFile.getName(), codeFile.lastModified());
		if(isJava(codeFile)){
			pkg = getPkg(codeFile);
			if(pkg != null){
				fileAttributions.setExtra(pkg);
			}
		}
		scanResult.addFinding(fileAttributions);
	}
	
	public ScanProgressMonitor getMonitor(){
		return this.monitor;
	}

	// private methods
	
	private FileFilter getDirectoryFilter(){
		FileFilter result = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		return result;
	}
	
	private FileFilter getCodeFileFilter(){
		FileFilter result = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String extention = getFileExtention(pathname.getName());
				return codeFileExtentions.contains(extention);
			}
		};
		return result;
	}

	private void initCodeFileExtentions(){
		codeFileExtentions = new HashSet<String>();
		Collections.addAll(codeFileExtentions, ".pl", ".pm", ".py", ".jl", ".el", ".java", ".c", ".cpp", ".h", ".cxx", ".c++", ".cc");
	}
	
	private String getFileExtention(String fileName){
		String result = "-";
		if(fileName.contains(".")){
			result = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
		}
		return result;
	}
	
	private int countFoldersRecursive(File dir) {
		int result = 0;
		try{
			File[] directories = dir.listFiles(dirFilter);
			if (directories != null) {
				result = directories.length;
				for (File directory : directories) {
					result += countFoldersRecursive(directory);
				}
			}
		}catch(Exception e){
			logger.log(Level.INFO, e.getMessage());
		}
		return result;
	}
	
	private boolean isJava(File codeFile){
		return codeFile.getName().toLowerCase().endsWith(".java");
	}
	
	private String getPkg(File javafile){
		String result = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(javafile));
			String line;
			while(result == null && reader.ready()){
				line = reader.readLine();
				if(line.startsWith("package")){
					result = getPackageFromLine(line);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String getPackageFromLine(String line){
		String result = null;
		result = line.substring(line.indexOf(' '), line.indexOf(';'));
		return result;
	}
}


