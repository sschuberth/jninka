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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rami.Sass
 */
public class CommentsExtractor extends StageProcessor {	

	/* --- Static members --- */
	
	private static Logger logger = Logger.getLogger(CommentsExtractor.class.getCanonicalName());
	
	/* --- Members --- */
	
	private String inputFile = "";
	
	/* --- Public methods --- */
	
	public boolean process() {	
		boolean result = true;
		
		if (this.getFileSize(this.getInputFile()) <= 0){
			logger.severe("Failed to retrieve file size info: file " + this.getInputFile() + " doesn\'t exist or empty.");
			result = false;
		} else {
			BufferedReader reader = null;
			try{
				List<String> outputInfo = new ArrayList<String>();
				
				int totalLineCount = this.determineCommentsExtractor(this.getInputFile());
				reader = new BufferedReader(new FileReader(this.getInputFile()));
				String line;
				int i = totalLineCount > 0 ? 0 : 1;
				while (((line = reader.readLine()) != null) && (i < totalLineCount)) {
					i++;
					outputInfo.add(line);
				}
				
				this.setOutputInfo(outputInfo);
			} catch (IOException e){
				result = false;
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		
		return result;
	}
	
	/* --- Protected methods --- */
	
	protected int determineCommentsExtractor(String filepath){
		int linecount = 700;
		
		String ext = JNinkaUtils.fileExtension(filepath);
	    if (!ext.equals("")) {
	    	if(Arrays.asList(new String[]{"pl", "pm", "py"}).contains(ext)){
	    		return 400;
	        } else if(Arrays.asList(new String[]{"jl", "el"}).contains(ext)){
	            return 400;
	        } else if (Arrays.asList(new String[]{"java", "c", "cpp", "h", "cxx", "c++", "cc"}).contains(ext)){
	            return 700;
	        }
	    }
	    
	    return linecount;
	}
	
	protected long getFileSize(String filename){
	    File file = new File(filename);
	    
	    if ( !file.exists() || !file.isFile() ){
	      return -1;
	    }		    
	    //Here we get the actual size
	    return file.length();
	}
		
	/* --- Getters / Setters --- */
	
	public void setInputFile(String lInputFile){
    	this.inputFile = lInputFile;
    }
	   
    public String getInputFile(){
        return this.inputFile;
    }
}
