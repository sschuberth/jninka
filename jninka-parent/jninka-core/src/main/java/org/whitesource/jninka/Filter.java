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
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public class Filter extends Processor{	   		
	
	Logger logger = Logger.getLogger(Filter.class.getCanonicalName());
	
	private InputStream critWords;
	private ArrayList<String> words;

	private ArrayList<String> goodOutputInfo = new ArrayList<String>();	  
	private ArrayList<String> badOutputInfo = new ArrayList<String>();	
	
    public void setCritWords(InputStream lCritWords){
    	this.critWords = lCritWords;
    	words = loadWords(lCritWords);
    }
   
    public InputStream getCritWords(){
        return this.critWords;
    }	

	public ArrayList<String> getGoodOutputInfo(){
	    return this.goodOutputInfo;
	}
			   
	public void setGoodOutputInfo(ArrayList<String> loutputInfo){
		this.goodOutputInfo.clear();
		this.goodOutputInfo.addAll(loutputInfo);
	}    

	public ArrayList<String> getBadOutputInfo(){
	    return this.badOutputInfo;
	}
			   
	public void setBadOutputInfo(ArrayList<String> loutputInfo){
		this.badOutputInfo.clear();
		this.badOutputInfo.addAll(loutputInfo);
	}
	
	public boolean process() {
		boolean result = true;
		try {
			ArrayList<String> goodOutputInfo = new ArrayList<String>();
			ArrayList<String> badOutputInfo = new ArrayList<String>();

			ArrayList<String> sentences = this.getInputInfo();
			for (String sentence : sentences){
				boolean isCheck = false;
				for (int i = 0; i < words.size(); i++) {
					String word = words.get(i);
					isCheck = JNinkaRegullarExpression.isMatch(sentence, "\\b"
							+ word + "\\b", Pattern.CASE_INSENSITIVE);
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

			this.setGoodOutputInfo(goodOutputInfo);
			this.setBadOutputInfo(badOutputInfo);
		} catch (Exception e) {
			logger.severe("Error: " + e.getMessage());
			result = false;
		}
		
		return result;
	}
	
	/**
	* Open and read a file, and return the words from file as arraylist
	*/
	protected ArrayList<String> loadWords(InputStream filepath){
		ArrayList <String>list = new ArrayList<String>();
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(filepath));
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
			reader.close();
		} catch(IOException e){
			logger.severe("Couldn't open " + filepath + " for reading! :" + e.getMessage());
		}
		return list;
	}
}
