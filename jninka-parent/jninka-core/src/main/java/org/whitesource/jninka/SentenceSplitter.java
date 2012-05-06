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
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Hashtable;

/**
 * @author Rami.Sass
 */
public class SentenceSplitter extends StageProcessor {
	
	/* --- Static members --- */
	
	Logger logger = Logger.getLogger(SentenceSplitter.class.getCanonicalName());
	
	/* --- Members --- */
	
	private InputStream abbrvFile;
	
	private InputStream dictionary;

	private Hashtable<String, Integer> commonTerms = new Hashtable<String, Integer>();
	
	private ArrayList<String> abbreviations = new ArrayList<String>();	 
	
	/* --- Concrete implementation methods --- */
	   
	public boolean process() {
		boolean result = true;
		try {
			ArrayList<String> outputInfo = new ArrayList<String>();
			
			String text = JNinkaUtils.joinArrayList(this.getInputInfo(), "\n");
			// append a "\n" just in case
			text += "\n";

			text = this.preProcessText(text);

			Pattern pat = Pattern.compile("^([^\n]*)\n", Pattern.MULTILINE);
			Matcher m = pat.matcher(text);

			StringBuffer sb = new StringBuffer();

			while (m.find() && (m.groupCount() >= 1)) {
				String curr = m.group(1);
				curr = JNinkaRegullarExpression.escapeForRegex(curr);
				m.appendReplacement(sb, curr);

				// let us count the number of alphabetic chars to check if we
				// are skipping anything we should not
				int count = 0;
				for (int i = 0; i < curr.length(); i++) {
					if (JNinkaRegullarExpression.isMatch(curr.substring(i, i),
							"[A-Za-z]")) {
						count++;
					}
				}
				List<String> sentences = this.splitText(curr);

				int count2 = 0;
				Iterator<String> it = sentences.iterator();
				while (it.hasNext()) {
					String s = (String) it.next();
					for (int i = 0; i < s.length(); i++) {
						if (JNinkaRegullarExpression.isMatch(s.substring(i, i),
								"[A-Za-z]")) {
							count2++;
						}
					}
					s = this.cleanSentence(s);
					s = JNinkaRegullarExpression.unescapeAfterRegex(s);
					outputInfo.add(s);
				}
				if (count != count2) {
					logger.severe("[" + curr + "]");
					it = sentences.iterator();
					while (it.hasNext()) {
						String s = (String) it.next();
						logger.severe(this.cleanSentence(s));
					}
					logger.severe("Number of printable chars does not match!  [" + count + "][" + count2 + "]");
					result = false;
				}
			}
			this.setOutputInfo(outputInfo);

		} catch (Exception e) {
			logger.severe("Error: " + e.getMessage());
			result = false;
		}
		return result;
	}
	
	/* --- Protected methods --- */
	
	protected String cleanSentence(String text){		
		//check for trailing bullets of different types
		text = JNinkaRegullarExpression.applyReplace(text, "^o ", "");				
		text = JNinkaRegullarExpression.applyReplace(text, "^\\s*[0-9]+\\s*[\\-\\)]", "");		
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]+", "");
		text = JNinkaRegullarExpression.applyReplace(text, "[ \t]+$", "");		
		//remove a trailing -
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]*[\\-\\.\\s*] +", "");
		//replace quotes
		text = JNinkaRegullarExpression.applyReplace(text, "\\s+", " ");
		text = JNinkaRegullarExpression.applyReplace(text, "['\"`]+", "<quotes>");
		
		text = JNinkaRegullarExpression.applyReplace(text, ":", "<colon>");
		text = JNinkaRegullarExpression.applyReplace(text, "\\.+$", ".");
		if ( text.matches("\n") ){
			throw new IllegalArgumentException("text cannot be \\n");
		}
		return text;
	}	

	/**
	* Open and read a file, and return the lines in the file as a  hashtable
	 * @throws Exception 
	*/
	protected List<String> splitText(String text) throws Exception{
		//int len = 0;
		List<String> result = new ArrayList<String>();
		String currentSentence = "";
		/*
		this breaks the sentence into
		1. Any text before a separator
		2. The separator [.!?:\n]
		3.
		*/
		String patternText = "^([^\\.\\!\\?\\:\n]*)([\\.\\!\\?\\:\n])(?=(.?))";
		while (JNinkaRegullarExpression.isMatch(text, patternText,Pattern.MULTILINE)){	
			String sentenceMatch = JNinkaRegullarExpression.getGroupValue(text, patternText, 1, Pattern.MULTILINE);
			String punctuation = JNinkaRegullarExpression.getGroupValue(text, patternText, 2, Pattern.MULTILINE);
			String sentence = sentenceMatch + punctuation;
			String after = JNinkaRegullarExpression.getGroupValue(text, patternText, 3, Pattern.MULTILINE);	
			text = JNinkaRegullarExpression.postMatch(text, patternText,Pattern.MULTILINE);//!!!put after all operations

			//if next character is not a space, then we are not in a sentence"
			if (!after.equals(" ") && !after.equals("\t"))
			{
				currentSentence += sentence;
				continue;
			}
			//at this point we know that there is a space after
			if (punctuation.equals(":") 
				||  punctuation.equals("?")
				|| punctuation.equals("!")){
				//let us consider this right here a beginning of a sentence
				result.add(currentSentence + sentence);			
				currentSentence = "";
				continue;
			}
			if (punctuation.equals(".")){
				//we have a bunch of alternatives
				//for the time being just consider a new sentence
						
				/*					
	 				TODO
					simple heuristic... let us check that the next words are not the beginning of a sentence
					in our library
					ENDTODO
				*/
	
				//is the last word an abbreviation? For this the period has to follow the word
				//this expression might have to be updated to take care of special characters  in names :(			
				String patternText2 = "(.?)([^\\p{Punct}\\s]+)$";
				if (JNinkaRegullarExpression.isMatch(sentenceMatch, patternText2)){
					String before = JNinkaRegullarExpression.getGroupValue(sentenceMatch, patternText2, 1);
					String lastWord = JNinkaRegullarExpression.getGroupValue(sentenceMatch, patternText2, 2);					
					//is it an abbreviation
					if (lastWord.length() == 1 ){      
						//single character abbreviations are special...
						//we will assume they never split the sentence if they are capitalized.
						char c = lastWord.charAt(0);
						if ((c >= 'A') && (c <= 'Z')){
							currentSentence += sentence;
							continue;
						}
						logger.info("last word an abbrev " + sentenceMatch + " lastword [" + lastWord + "] before [" + before + "]");
	
						//but some are lowercase!
						if ((c == 'e') || (c == 'i')){
							currentSentence += sentence;
							continue;
						}
						logger.info("2 last word an abbrev " + sentenceMatch + " lastword [" + lastWord + "] before [" + before + "]");
					} else {					
						lastWord = lastWord.toLowerCase();
						//only accept abbreviations if the previous char to the abbrev is space or
						//is empty (beginning of line). This avoids things like .c
						if ( (before.length() > 0) && before.equals(" ") 
							&& this.abbreviations.contains(lastWord)){
							currentSentence += sentence;
							continue;
						} 
//						else {
//							//just keep going, we handle this case below
//						}
					}
				}
				result.add(currentSentence + sentence);					
				currentSentence = "";
				continue;
			}
			logger.severe("We have not dealt with this case");
			throw new Exception();			
		}
		result.add(currentSentence + text);		
		return result;
	}	
	
	/**
	* Open and read a file, and return the lines in the file as a  hashtable
	*/
	protected void loadDictionary() {
		this.commonTerms = new Hashtable<String, Integer>();
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(this.getDictionary()));
			String line;
			while ( (line = reader.readLine()) != null ){
				if (JNinkaRegullarExpression.isMatch(line, "^[A-Z]")){
					this.commonTerms.put(line, 1);
				}
			}
		} catch(IOException e) {
			logger.severe("cannot open dictionary file " + this.getDictionary() + ": " + e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.severe("Error: " + e.getMessage());
			}
		}
		
	}

	/**
	* Open and read a file, and return the lines in the file as a hashtable
	*/
	protected void loadAbbreviations(){
		this.abbreviations = new ArrayList<String>();
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(this.getAbbrvFile()));
			String line;
			while ( (line = reader.readLine()) != null ){
				line = line.toLowerCase();//java=>perl
				this.abbreviations.add(line);
			}
		} catch(IOException e){
			logger.severe("cannot open dictionary file " + this.getAbbrvFile() + ": " + e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.severe("Error: " + e.getMessage());
			}
		}
	}
	
	protected String preProcessText(String text){
		text = JNinkaRegullarExpression.applyReplace(text, "\\+?\\-{3,1000}\\+?", " ", Pattern.MULTILINE); 
		text = JNinkaRegullarExpression.applyReplace(text, "={3,1000}", " ", Pattern.MULTILINE); 
		text = JNinkaRegullarExpression.applyReplace(text, ":{3,1000}", " ", Pattern.MULTILINE); 
		text = JNinkaRegullarExpression.applyReplace(text, "\\*{3,1000}", " ", Pattern.MULTILINE); 				
		
		//some characters are used for prettyprinting but never appear in sentences
		text = JNinkaRegullarExpression.applyReplace(text, "\\|+", " ", Pattern.MULTILINE); 
		text = JNinkaRegullarExpression.applyReplace(text, "\\\\+", " ", Pattern.MULTILINE); 								
		
		//let us deal with /* before we do anything
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]*/\\*", "", Pattern.MULTILINE); //Last Bug!!!!
		text = JNinkaRegullarExpression.applyReplace(text, "\\*\\/[ \t]*$", "", Pattern.MULTILINE); 
		text = JNinkaRegullarExpression.applyReplace(text, "([^:])//", "$1", Pattern.MULTILINE); 				
		
		//Replace /\r\n/ with \n only
		text = JNinkaRegullarExpression.applyReplace(text, "\r\n", "\n");

		//now, try to replace the leading/ending character of each line #/-, at most 3 heading characters
		// and each repeated as many times as necessaary
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]{0,3}[\\*\\#\\/\\;]+", "", Pattern.MULTILINE);
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]{0,3}[\\-]+", "", Pattern.MULTILINE);
		
		text = JNinkaRegullarExpression.applyReplace(text, "[\\*\\#\\/]+[ \t]{0,3}$", "", Pattern.MULTILINE);
		text = JNinkaRegullarExpression.applyReplace(text, "[\\-]+[ \t]{0,3}$", "", Pattern.MULTILINE);				
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]{0,3}[\\*\\#\\/\\;]+", "", Pattern.MULTILINE);
		
		//now, try to replace the ending character of each line if it is * or #
		text = JNinkaRegullarExpression.applyReplace(text, "[\\*\\#]+$", "", Pattern.MULTILINE);

		//at this point we have lines with nothing but spaces, let us get rid of them
		text = JNinkaRegullarExpression.applyReplace(text, "^[ \t]+$", "\n", Pattern.MULTILINE);
										
		//let us try the following trick
		// We first get rid of \t and replace it with ' '
		// we then use \t as a "single line separator" and \n as multiple line.
		// so we can match each with a single character.
		text = JNinkaRegullarExpression.applyReplace(text, "\t", " ");	
				
		text = JNinkaRegullarExpression.applyReplace(text, "\n(?!\n)", "\t");//MIKL - some problem!!!
		text = JNinkaRegullarExpression.applyReplace(text, "\n\n+", "\n");
		
		text += "\n";	
	
		return text;
	}
	
	/* --- Getters / Setters --- */
	
	public void setDictionary(InputStream lDictionary) {
		dictionary = lDictionary;
		// Load in the dictionary and find the common words.
		// Here, we assume the words in upper case are simply names and one
		// word per line - i.e. in same form as /usr/dict/words
		loadDictionary();
	}

	public void setAbbrvFile(InputStream lAbbrvFile) {
		abbrvFile = lAbbrvFile;
		// Same assumptions as for dictionary
		loadAbbreviations();
	}

	public InputStream getDictionary() {
		return dictionary;
	}

	public InputStream getAbbrvFile() {
		return abbrvFile;
	}
	
}
