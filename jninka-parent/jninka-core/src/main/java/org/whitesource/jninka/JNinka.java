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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
	
	private CommentsExtractor commentsExtractor;

	private SentenceSplitter sentenceSplitter;

	private SentenceFilter sentenceFilter;

	private SentenceTokenizer sentenceTokenizer;

	private boolean getUnknowns;

	private ScanProgressMonitor monitor;

    private boolean initialized;

	/* --- Constructor --- */
	
	/**
	 * Default constructor
	 */
	public JNinka() {
        initialized = false;
        monitor = new ScanProgressMonitor();
	}

	/* --- Public methods --- */

    public void init() {
        logger.info("Initializing JNinka");
        long t = System.currentTimeMillis();

        commentsExtractor = new CommentsExtractor();

        sentenceSplitter = new SentenceSplitter();
        sentenceSplitter.setDictionary(JNinka.class.getResourceAsStream("/splitter.dict"));
        sentenceSplitter.setAbbrvFile(JNinka.class.getResourceAsStream("/splitter.abv"));

        sentenceFilter = new SentenceFilter();
        sentenceFilter.setCritWords(JNinka.class.getResourceAsStream("/criticalword.dict"));

        sentenceTokenizer = new SentenceTokenizer();
        sentenceTokenizer.setLicSentences(JNinka.class.getResourceAsStream("/licensesentence.dict"));

        initialized = true;
        logger.info("JNinka initialization is done in " + (System.currentTimeMillis() - t) + " [msec]");
    }
	
	public ScanResults scanFolder(File folder, boolean getUnknowns){
        if (!initialized) {
            init();
        }

		this.getUnknowns = getUnknowns;

        SortedSet<File> directories = listSubdirectories(folder);
        logger.fine("counted " + directories.size() + " total directories to scan.");

		monitor.reset();
		monitor.setParams(directories.size(), 1);

		ScanResults result = new ScanResults();
        for (File directory : directories) {
            monitor.progress(1, directory.getAbsolutePath());
            File[] files = directory.listFiles();
            if (files == null) { continue; }
            for(File sourceFile : files){
                if (JNinkaUtils.isSourceCode(sourceFile)) {
//                    logger.fine("Scanning " + sourceFile);
                    List<LicenseAttribution> attributions = scanFile(sourceFile);
                    if (!JNinkaUtils.isEmpty(attributions)) {
                        result.addFinding(handleHit(sourceFile, attributions));
                    }
                }
            }
        }

		return result;
	}

	/* --- Private methods --- */

    private List<LicenseAttribution> scanFile(File codeFile){
        List<LicenseAttribution> attributions = null;
		try {
			// Stage 1.
			commentsExtractor.setInputFile(codeFile.getAbsolutePath());
			if (commentsExtractor.process()) {
				// Stage 2.
				sentenceSplitter.setInputInfo(commentsExtractor.getOutputInfo());
				if (sentenceSplitter.process()) {
					// Stage 3.
					sentenceFilter.setInputInfo(sentenceSplitter.getOutputInfo());
					if (sentenceFilter.process()) {
						// Stage 4.
						attributions = sentenceTokenizer.getAttributions(sentenceFilter.getGoodOutputInfo(), getUnknowns);
					} else {
						logger.severe("sentenceFilter failed");
					}
				} else {
					logger.severe("sentenceSplitter failed");
				}
			} else {
				logger.severe("extract-comments failed");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, codeFile.getAbsolutePath() + " - " + e.getMessage(), e);
		}

        return attributions;
	}

    private SortedSet<File> listSubdirectories(File dir) {
        SortedSet<File> folders = new TreeSet<File>();
        folders.add(dir);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    folders.addAll(listSubdirectories(f));
                }
            }
        }

		return folders;
	}

    private CodeFileAttributions handleHit(File codeFile, List<LicenseAttribution> attributions) {
        CodeFileAttributions fileAttributions = new CodeFileAttributions(attributions, codeFile.getName(), codeFile.lastModified());

        String ext = JNinkaUtils.fileExtension(codeFile);
        if(JNinkaUtils.JAVA_EXT_PATTERN.matcher(ext).matches()){
            String pkg = extractPackage(codeFile);
            if(!JNinkaUtils.isBlank(pkg)){
				fileAttributions.setExtra(pkg);
			}
		}

        return fileAttributions;
	}

	private String extractPackage(File javafile){
		String result = null;
		BufferedReader reader = null; 
				
		try {
			reader = new BufferedReader(new FileReader(javafile));
			String line;
			while(result == null && reader.ready()){
				line = reader.readLine();
				if(line != null && line.startsWith("package")){
                    result = line.substring(line.indexOf(' '), line.indexOf(';'));
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading package from file " + e.getMessage(), e);
		} finally {
            JNinkaUtils.close(reader, logger);
		}
		
		return result;
	}
	
	/* --- Getters / Setters --- */
	
	public ScanProgressMonitor getMonitor(){
		return this.monitor;
	}
}


