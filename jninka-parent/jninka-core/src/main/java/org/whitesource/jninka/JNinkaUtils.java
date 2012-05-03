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

/**
 * @author Rami.Sass
 */
public class JNinkaUtils{

	/**
	 * @param coll
	 * @param delimiter
	 * @return
	 * 
	 * @deprecated Consider using StringUtils instead. 
	 */
	public static String joinArrayList(ArrayList<String> coll, String delimiter){
	    if (coll.isEmpty()){
	    	return "";
	    }
	    StringBuilder sb = new StringBuilder();
	 
	    for (String x : coll){
	    	sb.append(x + delimiter);
	    }
	    sb.delete(sb.length()-delimiter.length(), sb.length());	 
	    return sb.toString();
	}	
}
