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
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public class SentenceFilter extends StageProcessor{	   		
	
	/* --- Static members --- */
	
	private static Logger logger = Logger.getLogger(SentenceFilter.class.getCanonicalName());
	
	/* --- Members --- */
	
	private InputStream critWords;
	
	private List<Pattern> wordPatterns;

	private List<String> goodOutputInfo;
	
	private List<String> badOutputInfo;	
	
	/* --- Public methods --- */
	
	public boolean process() {
		goodOutputInfo = new ArrayList<String>();
		badOutputInfo = new ArrayList<String>();

        for (String sentence : getInputInfo()) {
			boolean isCheck = false;
            Iterator<Pattern> iterator = wordPatterns.iterator();
            while (iterator.hasNext() && !isCheck) {
                Pattern pattern = iterator.next();
                isCheck = pattern.matcher(sentence).find();
            }

			if (isCheck) {
				goodOutputInfo.add(sentence);
			} else {
				badOutputInfo.add(sentence);
			}
		}

		return true;
	}
	
	/* --- Private methods --- */
	
	/**
	* Open and read a file, and return the words from file as a list.
	*/
	private void loadWords() {
        wordPatterns = new ArrayList<Pattern>();

		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(critWords));
			String line;
			while ( (line = reader.readLine()) != null ){
                if (JNinkaUtils.isBlank(line) || line.startsWith("#")) { continue; }
                int index = line.indexOf('#');
                if (index > 0) { line = line.substring(0, index); }
                if (!JNinkaUtils.isBlank(line)){
                    wordPatterns.add(Pattern.compile("\\b" + line + "\\b", Pattern.CASE_INSENSITIVE));
                }
			}
		} catch(IOException e){
			logger.log(Level.SEVERE, "Couldn't open " + critWords + " for reading! :" + e.getMessage(), e);
		} finally {
            JNinkaUtils.close(reader, logger);
		}
	}
	
	/* --- Getters / Setters --- */
	
	public void setCritWords(InputStream critWords){
		this.critWords = critWords;
		loadWords();
	}
	
	public InputStream getCritWords(){
		return critWords;
	}	
	
	public List<String> getGoodOutputInfo(){
		return goodOutputInfo;
	}
	
	public void setGoodOutputInfo(List<String> outputInfo){
		this.goodOutputInfo = outputInfo;
	}    
	
	public List<String> getBadOutputInfo(){
		return badOutputInfo;
	}
	
	public void setBadOutputInfo(ArrayList<String> outputInfo){
		this.badOutputInfo = outputInfo;
	}
	
}

