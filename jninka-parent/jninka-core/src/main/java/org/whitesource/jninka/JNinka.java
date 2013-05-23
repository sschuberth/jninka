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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.whitesource.jninka.model.CodeFileAttributions;
import org.whitesource.jninka.model.LicenseAttribution;
import org.whitesource.jninka.model.ScanResults;
import org.whitesource.jninka.progress.ScanProgressMonitor;

/**
 * JNinka
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
public class JNinka {
	
	/* --- Static members --- */
	
	private static final Logger logger = Logger.getLogger(JNinka.class.getCanonicalName());
	
	/* --- Members --- */
	
	private ScanProgressMonitor monitor;
	
	private FileFilter dirFilter;
	
	private FileFilter sourceCodeFilter;
	
	private CommentsExtractor extComments;
	
	private SentenceSplitter splitter;
	
	private SentenceFilter filter;
	
	private SentenceTokenizer senttok;
	
	private boolean getUnknowns;
	
	/* --- Constructor --- */
	
	/**
	 * Default constructor
	 */
	public JNinka() {
        dirFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        sourceCodeFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return JNinkaUtils.isSourceCode(pathname);
            }
        };
		
		extComments = new CommentsExtractor();
		
		splitter = new SentenceSplitter();
		splitter.setDictionary(JNinka.class.getResourceAsStream("/splitter.dict"));
		splitter.setAbbrvFile(JNinka.class.getResourceAsStream("/splitter.abv"));
		
		filter = new SentenceFilter();
		filter.setCritWords(JNinka.class.getResourceAsStream("/criticalword.dict"));
		
		senttok = new SentenceTokenizer();
		senttok.setLicSentences(JNinka.class.getResourceAsStream("/licensesentence.dict"));
		
		monitor = new ScanProgressMonitor();
	}
	
	/* --- Public methods --- */
	
	public ScanResults scanFolderRecursive(File folder, boolean getUnknowns){
		// Problem: Folder count does not include parent folder, as oppose to the progress updater, which caused the range of progression
		// to go over 100 (not allowed, throws exception)
		// Solution: Includes parent folder (+1) to overall count.
		int folderCount = countFoldersRecursive(folder) + 1;
		monitor.reset();
		monitor.setParams(folderCount, 1);

		this.getUnknowns = getUnknowns;

		ScanResults result = new ScanResults();
        runRecursive(folder, result);

		return result;
	}
	
	public void run(File codeFile, ScanResults scanResult){
		try {
			// Stage 1.
			extComments.setInputFile(codeFile.getAbsolutePath());
			if (extComments.process()) {
				// Stage 2.
				splitter.setInputInfo(extComments.getOutputInfo());
				if (splitter.process()) {
					// Stage 3.
					filter.setInputInfo(splitter.getOutputInfo());
					if (filter.process()) {
						// Stage 4.
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
			logger.log(Level.SEVERE, codeFile.getAbsolutePath() + " - " + e.getMessage(), e);
		}
	}
	
	/* --- Private methods --- */
	
	private int runRecursive(File directory, ScanResults result){
		int count = 0;
		monitor.progress(1, directory.getAbsolutePath());

        File[] sourceFiles = directory.listFiles(sourceCodeFilter);
		for(File sourceFile : sourceFiles){
			run(sourceFile, result);
			count++;
		}

        File[] children = directory.listFiles(dirFilter);
        for(File child : children){
			count += runRecursive(child, result);
		}

        return count;
	}
	
	private void handleHit(File codeFile, ScanResults scanResult, List<LicenseAttribution> attributions) {
        CodeFileAttributions fileAttributions = new CodeFileAttributions(attributions, codeFile.getName(), codeFile.lastModified());

        if(isJava(codeFile)){
            String pkg = getPkg(codeFile);
            if(!JNinkaUtils.isBlank(pkg)){
				fileAttributions.setExtra(pkg);
			}
		}

		scanResult.addFinding(fileAttributions);
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
		} catch(Exception e){
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return result;
	}
	
	private boolean isJava(File codeFile){
        String ext = JNinkaUtils.fileExtension(codeFile);
        return JNinkaUtils.JAVA_EXT_PATTERN.matcher(ext).matches();
	}
	
	private String getPkg(File javafile){
		String result = null;
		BufferedReader reader = null; 
				
		try {
			reader = new BufferedReader(new FileReader(javafile));
			String line;
			while(result == null && reader.ready()){
				line = reader.readLine();
				if(line != null && line.startsWith("package")){
					result = getPackageFromLine(line);
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading package from file " + e.getMessage(), e);
		} finally {
            JNinkaUtils.close(reader, logger);
		}
		
		return result;
	}
	
	private String getPackageFromLine(String line){
        return line.substring(line.indexOf(' '), line.indexOf(';'));
	}
	
	/* --- Getters / Setters --- */
	
	public ScanProgressMonitor getMonitor(){
		return this.monitor;
	}
}


