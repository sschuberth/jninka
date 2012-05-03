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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Rami.Sass
 */
public abstract class Processor{
	private ArrayList<String> outputInfo = new ArrayList<String>();	  
	private ArrayList<String> inputInfo = new ArrayList<String>();	
		  	   
	public ArrayList<String> getOutputInfo(){
	    return this.outputInfo;
	}
			   
	public void setOutputInfo(ArrayList<String> lOutputInfo){
		this.outputInfo.clear();
		this.outputInfo.addAll(lOutputInfo);
	}
	   
	public ArrayList<String> getInputInfo(){
		return this.inputInfo;
	}
			   
	public void setInputInfo(ArrayList<String> lInputInfo){
		this.inputInfo.clear();
		this.inputInfo.addAll(lInputInfo);
	}	 
	
	public abstract boolean process();
		
	protected static void writeFile(String filename, ArrayList<String> info)  throws Exception
	{
		try{		
			File outputFile = new File(filename);
			FileWriter out = new FileWriter(outputFile);
		    for (String x : info){
		    	out.write(x + "\n");
		    }									
			out.close();
		} catch(IOException e){
			throw new Exception("Can not write list to file "+ filename + ": " + e.getMessage());
		}
	}	
}		
