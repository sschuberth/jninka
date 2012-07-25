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

/**
 * @author Rami.Sass
 */
public class SentenceFilter extends StageProcessor{	   		
	
	/* --- Static members --- */
	
	private static Logger logger = Logger.getLogger(SentenceFilter.class.getCanonicalName());
	
	/* --- Members --- */
	
	private InputStream critWords;
	
	private List<String> words;

	private List<String> goodOutputInfo;
	
	private List<String> badOutputInfo;	
	
	/* --- Public methods --- */
	
	public boolean process() {
		goodOutputInfo = new ArrayList<String>();
		badOutputInfo = new ArrayList<String>();

		for (String sentence : this.getInputInfo()) {
			boolean isCheck = false;
			for (int i = 0; i < words.size(); i++) {
				String word = words.get(i);
				isCheck = JNinkaRegullarExpression.isMatch(sentence, "\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
				if (isCheck) {
					break;
				}
			}
			
			if (isCheck) {
				goodOutputInfo.add(sentence);
			} else {
				badOutputInfo.add(sentence);
			}
		}

		return true;
	}
	
	/* --- Protected methods --- */
	
	/**
	* Open and read a file, and return the words from file as a list.
	*/
	protected ArrayList<String> loadWords() {
		ArrayList <String>list = new ArrayList<String>();
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(critWords));
			String line;
			while ( (line = reader.readLine()) != null ){
				if (JNinkaRegullarExpression.isMatch(line, "^\\#")){
					continue;
				}
				line = JNinkaRegullarExpression.applyReplace(line, "\\#.*$", "");
				
				if ( !line.isEmpty() ){
					list.add(line);
				}
			}
		} catch(IOException e){
			logger.log(Level.SEVERE, "Couldn't open " + critWords + " for reading! :" + e.getMessage(), e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		return list;
	}
	
	/* --- Getters / Setters --- */
	
	public void setCritWords(InputStream critWords){
		this.critWords = critWords;
		words = loadWords();
	}
	
	public InputStream getCritWords(){
		return this.critWords;
	}	
	
	public List<String> getGoodOutputInfo(){
		return this.goodOutputInfo;
	}
	
	public void setGoodOutputInfo(List<String> outputInfo){
		this.goodOutputInfo = outputInfo;
	}    
	
	public List<String> getBadOutputInfo(){
		return this.badOutputInfo;
	}
	
	public void setBadOutputInfo(ArrayList<String> outputInfo){
		this.badOutputInfo = outputInfo;
	}
	
}

