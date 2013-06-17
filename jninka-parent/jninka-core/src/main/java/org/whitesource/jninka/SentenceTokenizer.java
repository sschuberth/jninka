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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whitesource.jninka.model.LicenseAttribution;

/**
 * @author Rami.Sass
 */
public class SentenceTokenizer {

	/* --- Static members --- */

    public static final Pattern GPL_LIKE_PATTERN = Pattern.compile("GNU|General Public License|GPL");
    public static final Pattern LESSER_GPL_PATTERN = Pattern.compile("(Lesser|Library) GPL/GPL", Pattern.CASE_INSENSITIVE);

    private static Logger logger = Logger.getLogger(SentenceTokenizer.class.getCanonicalName());
	
	/* --- Members --- */
	
	private InputStream licSentences;
	
	private int tooLong = 70;
	
	private List<LicenseSentence> licenseSentences;

	/* --- Public methods --- */
	
    public List<LicenseAttribution> getAttributions(List<String> lines, boolean getUnknown){
    	List<LicenseAttribution> result = new ArrayList<LicenseAttribution>();

		for (String line : lines){
			String saveLine;
			String originalLine = line;
			line = normalizeSentence(line);

			boolean check = false;
			String matchName = "UNKNOWN";
			List<String> params = new ArrayList<String>();

			int distance = 1; // maximum? number
            String before = "";
			String after = "";

            boolean gpl = false;
            boolean gplLater = false;
            String gplVersion = "";
            if (GPL_LIKE_PATTERN.matcher(line).matches()){
                gpl = true;
                Object object[] = normalizeGPL(line);
                line = object[0].toString();
                gplLater = Boolean.parseBoolean(object[1].toString());
                gplVersion = object[2].toString();//Integer.parseInt(object[2].toString());
            }

            saveLine = line;
			boolean saveGPL = gpl;
			String LGPL = "";

            LicenseSentence finalSentence = null;
            Iterator<LicenseSentence> iterator = licenseSentences.iterator();
            while (iterator.hasNext()) {
                LicenseSentence licenseSentence = iterator.next();

                // we need this due to the goto (loop in java) again
				line = saveLine;
				gpl = saveGPL;
				LGPL = "";

				boolean isContinueExternalLoop;
				boolean isCondition;

				while (true) {
                    isCondition = false;
                    Matcher matcher = licenseSentence.pattern.matcher(line);
					if (matcher.find()) {
                        isCondition = true;
						check = true;
                        finalSentence = licenseSentence;
						matchName = licenseSentence.name;

						for (int i = 1; i <= licenseSentence.number; i++){
							params.add(JNinkaRegullarExpression.getGroupValue(matcher, i));
						}

						before = JNinkaRegullarExpression.beforeMatch(licenseSentence.pattern, line);
                        before = JNinkaUtils.abbreviate(before, this.getTooLong());
						after = JNinkaRegullarExpression.postMatch(licenseSentence.pattern, line);
                        after = JNinkaUtils.abbreviate(after, this.getTooLong());

						isContinueExternalLoop = false;
						break;
					} else {
						isCondition = true;
						// let us try again in case it is lesser/library. Do it only once
                        Matcher lgplMatcher = LESSER_GPL_PATTERN.matcher(line);
                        if (gpl	&& lgplMatcher.find()) {
							LGPL = JNinkaRegullarExpression.getGroupValue(lgplMatcher, 1);
							continue;
						}
						if (gpl){
							gpl = false;
							line = saveLine;
							continue;
						}
						isContinueExternalLoop = true;
						break;// dmg
						/*
						 * commented in perl String targetset = regexp;
						 * targetset =
						 * JNinkaRegullarExpression.applyReplace(targetset,
						 * "^(.*)$", "$1"); int tmpdist =
						 * senttok.getLevenshteinDistance(line,
						 * targetset)/Math
						 * .max(targetset.length(),sentence.length()); if (
						 * tmpdist < distance){ mostsimilarname = name;
						 * distance = tmpdist; } isCondition = false;
						 */
					}
					// in case of uncomment upper text??
					// isContinueExternalLoop = false;
				}
				if (!isContinueExternalLoop || !isCondition){
					break;
				}
			}

            // create attribution
            originalLine = JNinkaUtils.abbreviate(originalLine, this.getTooLong());
			if (check){
				// licensesentence name, param1, param2,..
				if (gpl) {
					matchName += "Ver" + gplVersion;
					if (gplLater){
						matchName += "+";
					}
					matchName = LGPL + matchName;
				}
				result.add(new LicenseAttribution(params, finalSentence.id, matchName, finalSentence.subRule, before, after, originalLine));
			} else if (getUnknown) { // UNKNOWN, sentence
                saveLine = JNinkaUtils.abbreviate(saveLine, this.getTooLong());
                result.add(new LicenseAttribution(null, Integer.MIN_VALUE, matchName, "UNKNOWN", Integer.toString(distance), saveLine, originalLine));
            }
		}
			
		return result;
	}
	

	/**
	* Open and read a file, and return the words from file as arraylist
	*/
	public void loadLicenseSentences(){
        licenseSentences = new ArrayList<LicenseSentence>();

		Pattern sentenceFormat = Pattern.compile("(.*?):(.*?):(.*)");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/licensesentence.dict")));
			String line;
			while ((line = reader.readLine()) != null) {
                if (JNinkaUtils.isBlank(line) || line.startsWith("#")) { continue; }
                if (!sentenceFormat.matcher(line).matches()) {
					logger.severe("Illegal format in license expression [" + line + "]");
					throw new IllegalArgumentException("Illegal format in license expression [" + line + "]");
                }
                licenseSentences.add(new LicenseSentence(line));
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot open license sentence file : "+ e.getMessage(), e);
		} finally {
			JNinkaUtils.close(reader, logger);
		}
	}

    /* --- Private methods --- */

    private Object[] normalizeGPL(String line){
	    boolean later = false;
	    String version = "0";
	    	    
	    //do some very quick spelling corrections for english/british words
	    line = JNinkaRegullarExpression.applyReplace(line, "Version 2,? \\(June 1991\\)", "Version 2", Pattern.CASE_INSENSITIVE);
	    line = JNinkaRegullarExpression.applyReplace(line, "Version 2,? dated June 1991", "Version 2", Pattern.CASE_INSENSITIVE);
	    line = JNinkaRegullarExpression.applyReplace(line, "Version 2\\.1,? dated February 1999", "Version 2.1", Pattern.CASE_INSENSITIVE);
	    
	    if (JNinkaRegullarExpression.isMatch(line, ",? or \\(?at your option\\)?,? any later version", Pattern.CASE_INSENSITIVE)){
	    	later = true;
	    	line = JNinkaRegullarExpression.applyReplace(line, ",? or \\(?at your option\\)?,? any later version", "", Pattern.CASE_INSENSITIVE);
		}	
	    if (JNinkaRegullarExpression.isMatch(line, ", or any later version", Pattern.CASE_INSENSITIVE)){
	    	later = true;
	    	line = JNinkaRegullarExpression.applyReplace(line, ", or any later version", "", Pattern.CASE_INSENSITIVE);
	    }
	    if (JNinkaRegullarExpression.isMatch(line, " or (greater|later)", Pattern.CASE_INSENSITIVE)){
	    	later = true;
	    	line = JNinkaRegullarExpression.applyReplace(line, " or (greater|later)", "", Pattern.CASE_INSENSITIVE);
	    }
	    if (JNinkaRegullarExpression.isMatch(line, "or (greater|later) ", Pattern.CASE_INSENSITIVE)){
	    	later = true;
	    	line = JNinkaRegullarExpression.applyReplace(line, "or (greater|later) ", "", Pattern.CASE_INSENSITIVE);
	    }	 
	    
	    if (JNinkaRegullarExpression.isMatch(line, "(version|v\\.?) ([123\\.0]+)", Pattern.CASE_INSENSITIVE)){
	    	version = JNinkaRegullarExpression.getGroupValue(line, "(version|v\\.?) ([123\\.0]+)", 2, Pattern.CASE_INSENSITIVE);
	    	line = JNinkaRegullarExpression.applyReplace(line, "(version|v\\.?) ([123\\.0]+)", "<VERSION>", Pattern.CASE_INSENSITIVE);	    	
	    }	 	    
	    if (JNinkaRegullarExpression.isMatch(line, "GPL ?[v\\-]([123\\.0]+)", Pattern.CASE_INSENSITIVE)){
	    	version = JNinkaRegullarExpression.getGroupValue(line, "GPL ?[v\\-]([123\\.0]+)", 1, Pattern.CASE_INSENSITIVE);
	    	line = JNinkaRegullarExpression.applyReplace(line, "GPL ?[v\\-]([123\\.0]+)", "GPL <VERSION>", Pattern.CASE_INSENSITIVE);
	    }
	    if (JNinkaRegullarExpression.isMatch(line, "v\\.?([123\\.0]+)( *[0-9]+)", Pattern.CASE_INSENSITIVE)){
	    	version = JNinkaRegullarExpression.getGroupValue(line, "v\\.?([123\\.0]+)( *[0-9]+)", 1, Pattern.CASE_INSENSITIVE);
	    	line = JNinkaRegullarExpression.applyReplace(line, "v\\.?([123\\.0]+)( *[0-9]+)", "<VERSION>$2", Pattern.CASE_INSENSITIVE);
	    }

    	line = JNinkaRegullarExpression.applyReplace(line, "(distributable|licensed|released|made available)", "<LICENSED>", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "Library General Public License", "Library General Public License", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "Lesser General Public License", "Lesser General Public License", Pattern.CASE_INSENSITIVE);
    	
    	line = JNinkaRegullarExpression.applyReplace(line, "General Public License", "GPL", Pattern.CASE_INSENSITIVE);    	
    	line = JNinkaRegullarExpression.applyReplace(line, "GPL \\(GPL\\)", "GPL", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "GPL \\(<QUOTES>GPL<QUOTES>\\)", "GPL", Pattern.CASE_INSENSITIVE);
    	
    	line = JNinkaRegullarExpression.applyReplace(line, "GNU ", "", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "under GPL", "under the GPL", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "under Lesser", "under the Lesser", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "under Library", "under the Library", Pattern.CASE_INSENSITIVE);    	
    	
    	
    	line = JNinkaRegullarExpression.applyReplace(line, "of GPL", "of the GPL", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "f Lesser", "of the Lesser", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "of Library", "of the Library", Pattern.CASE_INSENSITIVE);    	

    	line = JNinkaRegullarExpression.applyReplace(line, "(can|may)", "can", Pattern.CASE_INSENSITIVE);    	
    	line = JNinkaRegullarExpression.applyReplace(line, "<VERSION> only", "<VERSION>", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "<VERSION> of the license", "<VERSION>", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "(<VERSION>|GPL),? as published by the Free Software Foundation", "$1", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "(<VERSION>|GPL) \\(as published by the Free Software Foundation\\)", "$1", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "(<VERSION>|GPL),? incorporated herein by reference", "$1", Pattern.CASE_INSENSITIVE);
    	
    	line = JNinkaRegullarExpression.applyReplace(line, "terms and conditions", "terms", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "GPL along with", "GPL with", Pattern.CASE_INSENSITIVE);
    	line = JNinkaRegullarExpression.applyReplace(line, "GPL \\(<VERSION\\)", "GPL <VERSION>", Pattern.CASE_INSENSITIVE);
    	
    	line = JNinkaRegullarExpression.applyReplace(line, " +", " ");
    	line = JNinkaRegullarExpression.applyReplace(line, " +$", "");

        return new Object[]{line, later, version};
	}

    private String normalizeSentence(String line){
	    // do some very quick spelling corrections for english/british words
	    line = JNinkaRegullarExpression.applyReplace(line, "icence", "icense", Pattern.CASE_INSENSITIVE);
	    line = JNinkaRegullarExpression.applyReplace(line, "(\\.|;)$", "");	    	    	    		
		return line;
	}

    /* --- Nested Classes --- */

    private static final class LicenseSentence {
        public int id;
        public String name;
        public String subRule;
        public int number;
        public Pattern pattern;

        public LicenseSentence(String sentence) {
            String[] shards = sentence.split(":");
            if (shards.length < 5 || shards.length > 6){
                throw new IllegalArgumentException("Illegal license sentence format: " + sentence);
            }
            id = Integer.parseInt(shards[0]);
            name = shards[1];
            subRule = shards[2];
            number = Integer.parseInt(shards[3]);
            pattern = Pattern.compile(shards[4], Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        }
    }
	
	/* --- Getters / Setters --- */
		
	public void setTooLong(int tooLong){
		this.tooLong = tooLong;
    }
   
    public int getTooLong(){
        return tooLong;
    }    
}


