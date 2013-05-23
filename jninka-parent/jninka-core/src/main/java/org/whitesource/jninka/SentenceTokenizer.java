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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.whitesource.jninka.model.LicenseAttribution;

/**
 * @author Rami.Sass
 */
public class SentenceTokenizer {	
	
	/* --- Static members --- */
	
	private static Logger logger = Logger.getLogger(SentenceTokenizer.class.getCanonicalName());
	
	/* --- Members --- */
	
	private InputStream licSentences;
	
	private int tooLong = 70;
	
	private List<String> licenseSentenceList;

	/* --- Public methods --- */
	
    public List<LicenseAttribution> getAttributions(List<String> lines, boolean getUnknown){
    	List<LicenseAttribution> result = new ArrayList<LicenseAttribution>();

		for (String line : lines){
			String saveLine;
			String originalLine = line;

			line = this.normalizeSentence(line);

			boolean check = false;
			Integer id = Integer.MIN_VALUE;
			String matchname = "UNKNOWN";
			ArrayList<String> parm = new ArrayList<String>();

			int distance = 1; // maximum? number
			String mostsimilarname = "UNKNOWN";
			String before = "";
			String after = "";
			boolean gpl = false;
			boolean gplLater = false;
			String gplVersion = "";

			if (this.looksLikeGPL(line)){
				// String old = line;
				gpl = true;

				Object object[] = this.normalizeGPL(line);
				line = object[0].toString();
				gplLater = Boolean.parseBoolean(object[1].toString());
				gplVersion = object[2].toString();//Integer.parseInt(object[2].toString());

				// lineAsGPL = line;
			}

			String subRule = "";
			saveLine = line;
			boolean saveGPL = gpl;
			String LGPL = "";
			for (int ki = 0; ki < licenseSentenceList.size(); ki++){
				String sentence = licenseSentenceList.get(ki);
				String[] separated = sentence.split(":");
				if ((separated.length < 5) || (separated.length > 6)){
					logger.severe("licenseSentenceList file has incorrect format:" + separated.length + "!\n");
					throw new IllegalArgumentException();
				}
				id = Integer.parseInt(separated[0]);
				String name = separated[1];
				subRule = separated[2];
				int number = Integer.parseInt(separated[3]);
				String regexp = separated[4];
				// String option = separated.length == 6 ? separated[5] :
				// "";

				// we need this due to the goto (loop in java) again
				line = saveLine;
				gpl = saveGPL;
				LGPL = "";

				boolean isContinueExternalLoop = true;

				boolean isCondition = false;
				while (true) {
					isCondition = false;
					if (JNinkaRegullarExpression.isMatch(line, regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)) {
						isCondition = true;
						before = JNinkaRegullarExpression.beforeMatch(line, regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
						after = JNinkaRegullarExpression.postMatch(line, regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

						check = true;
						matchname = name;

						for (int i = 1; i <= number; i++){
							String r = JNinkaRegullarExpression.getGroupValue(line, regexp, i, Pattern.CASE_INSENSITIVE	| Pattern.MULTILINE);
							if (r == null) {
								r = "";
							}
							parm.add(r);
						}
						isContinueExternalLoop = false;
						break;
					} else {
						isCondition = true;
						// let us try again in case it is lesser/library
						// do it only once
						if (gpl	&& JNinkaRegullarExpression.isMatch(line, "(Lesser|Library) GPL/GPL", Pattern.CASE_INSENSITIVE)) {
							LGPL = JNinkaRegullarExpression.getGroupValue(line, "(Lesser|Library) GPL/GPL", 1, Pattern.CASE_INSENSITIVE);
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
			if (check){
				// licensesentence name, parm1, parm2,..
				if (gpl) {
					matchname += "Ver" + gplVersion;
					if (gplLater){
						matchname += "+";
					}
					matchname = LGPL + matchname;
				} 
//					else {
//						// nothing in perl code
//					}
				if ((before.length() > this.getTooLong())
						|| (after.length() > this.getTooLong())){
					matchname += "-TOOLONG";
				}

				result.add(new LicenseAttribution(parm, id, matchname, subRule, before, after, originalLine));
			} else {
				// UNKNOWN, sentence
				if (getUnknown) {
					// String outputStr = matchname + ";" + "0;" + mostsimilarname + ";" + distance + ";" + saveLine + ":" + originalLine;
					result.add(new LicenseAttribution(null, Integer.MIN_VALUE, matchname, mostsimilarname, Integer.toString(distance), saveLine, originalLine));
				}
			}
		}
			
		return result;
	}
	
    /* --- Private methods --- */
    
	/**
	* Open and read a file, and return the words from file as arraylist
	*/
	private List<String> loadLicenseSentence(InputStream filepath){
		List<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(filepath));
			String line;
			while ((line = reader.readLine()) != null) {
				if (JNinkaRegullarExpression.isMatch(line, "^\\#")) { continue; }
				if (JNinkaRegullarExpression.isMatch(line, "^ *$")) { continue; }
				if (!JNinkaRegullarExpression.isMatch(line, "(.*?):(.*?):(.*)")) {
					logger.severe("Illegal format in license expression [" + line + "]");
					throw new IllegalArgumentException("Illegal format in license expression [" + line + "]");
				}
				list.add(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "cannot open file " + filepath + ": "+ e.getMessage(), e);
		} finally {
			JNinkaUtils.close(reader, logger);
		}
		
		return list;
	}

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
	    
    	Object object[] = new Object[3];
        object[0] = line;
        object[1] = later;
        object[2] = version;    	
	    return object;
	}

    private boolean looksLikeGPL(String line){
        if (JNinkaRegullarExpression.isMatch(line, "GNU")){ return true; }
		if (JNinkaRegullarExpression.isMatch(line, "General Public License")) { return true; }
		if (JNinkaRegullarExpression.isMatch(line, "GPL")){ return true; }

	    return false;
	}

    private String normalizeSentence(String line){
	    // do some very quick spelling corrections for english/british words
	    line = JNinkaRegullarExpression.applyReplace(line, "icence", "icense", Pattern.CASE_INSENSITIVE);
	    line = JNinkaRegullarExpression.applyReplace(line, "(\\.|;)$", "");	    	    	    		
		return line;
	}
	
	//Source http://www.merriampark.com/ldjava.htm
    private int getLevenshteinDistance(String s, String t)
	{	
		if (s == null || t == null){
			throw new IllegalArgumentException("Strings must not be null");
		}
				
		/*
		The difference between this impl. and the previous is that, rather 
		than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		is the 'current working' distance array that maintains the newest distance cost
		counts as we iterate through the characters of String s.  Each time we increment
		the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		allows us to retain the previous cost counts as required by the algorithm (taking 
		the minimum of the cost count to the left, up one, and diagonally up and to the left
		of the current cost count being calculated).  (Note that the arrays aren't really 
		copied anymore, just switched...this is clearly much better than cloning an array 
		or doing a System.arraycopy() each time  through the outer loop.)

		Effectively, the difference between the two implementations is this one does not 
		cause an out of memory condition when calculating the LD over two very large strings.  		
		 */		
				
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
						+ cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}
		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}
	
	/* --- Getters / Setters --- */
	
	public void setLicSentences(InputStream lLicSentences){
    	licSentences = lLicSentences;
    	licenseSentenceList = loadLicenseSentence(lLicSentences);
    }
   
    public InputStream getLicSentences(){
        return licSentences;
    }	
		
	public void setTooLong(int lTooLong){
		tooLong = lTooLong;
    }
   
    public int getTooLong(){
        return tooLong;
    }    
}


