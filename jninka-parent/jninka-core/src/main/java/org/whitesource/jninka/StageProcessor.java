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

import java.util.ArrayList;
import java.util.List;

/**
 * Base, abstract, class for a generic stage in the algorithm.
 * 
 * @author Rami.Sass
 */
public abstract class StageProcessor {
	
	/* --- Members --- */
	
	private List<String> outputInfo = new ArrayList<String>();	  
	
	private List<String> inputInfo = new ArrayList<String>();	
	
	/* --- Abstract methods --- */
	
	public abstract boolean process();
	
	/* --- Getters / Setters --- */
	
	public List<String> getOutputInfo(){
	    return this.outputInfo;
	}
			   
	public void setOutputInfo(List<String> outputInfo){
		outputInfo = new ArrayList<String>(outputInfo);
	}
	   
	public List<String> getInputInfo(){
		return this.inputInfo;
	}
			   
	public void setInputInfo(List<String> inputInfo){
		this.inputInfo = new ArrayList<String>(inputInfo);
	}	 
	
}		
