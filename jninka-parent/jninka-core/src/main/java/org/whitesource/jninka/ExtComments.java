package org.whitesource.jninka;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Copyright (C) 2012 White Source (www.whitesourcesoftware.com)
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
public class ExtComments extends Processor{	

	// logger
	private static Logger logger = Logger.getLogger(ExtComments.class.getCanonicalName());
	
	// members
	
	private String inputFile = "";
	
	// public methods
	
    public void setInputFile(String lInputFile){
    	this.inputFile = lInputFile;
    }
	   
    public String getInputFile(){
        return this.inputFile;
    }
	
	public boolean process(){	
		boolean result = true;
		try{
			if (this.getFileSize(this.getInputFile()) <= 0){
				logger.severe("Failed to retrieve file size info: file " + this.getInputFile() + " doesn\'t exist or empty.");
				result = false;
			}			
			ArrayList<String> outputInfo = new ArrayList<String>();
					
			int totalLineCount = this.determineCommentsExtractor(this.getInputFile());
			BufferedReader reader = new BufferedReader(new FileReader(this.getInputFile()));
			String line;
			int i = totalLineCount > 0 ? 0 : 1;
			while (((line = reader.readLine()) != null) && (i < totalLineCount)) {
				i++;
				outputInfo.add(line);
			}
			this.setOutputInfo(outputInfo);
			reader.close();
		} catch (Exception e){
			logger.severe("Error: " + e.getMessage());
			result = false;
		}
		return result;
	}
	
	protected int determineCommentsExtractor(String filepath){
		String ext = ExtComments.fileExtension(filepath);
		
	    if ( ext != "" ){
	    	if(Arrays.asList(new String[]{"pl", "pm", "py"}).contains(ext)){
	    		//for the time being, let us just extract the top 400 lines
	    		return 400;
	        } else if(Arrays.asList(new String[]{"jl", "el"}).contains(ext)){
	            return 400;
	        } else if (Arrays.asList(new String[]{"java", "c", "cpp", "h", "cxx", "c++", "cc"}).contains(ext)){
	            return 700;
	        } else {
	            return 700;
	        }
	    } else {
	        return 700;
	    }
	}
	
	protected long getFileSize(String filename){
	    File file = new File(filename);
	    
	    if ( !file.exists() || !file.isFile() ){
	      return -1;
	    }		    
	    //Here we get the actual size
	    return file.length();
	}
		
	protected static String fileExtension(String filepath){
		File file = new File(filepath);
	    String fname = file.getName();
	    int extIndex = fname.lastIndexOf('.');
	    String ext = extIndex == -1 ? "" : fname.substring(extIndex + 1, fname.length());
	    ext = ext.toLowerCase();
	    return ext;
	}	
}
