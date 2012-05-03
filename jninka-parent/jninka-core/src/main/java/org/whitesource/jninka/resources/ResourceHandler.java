package org.whitesource.jninka.resources;

import java.io.InputStream;

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
public class ResourceHandler {
	
//	public File getResource(String name){
//		File result = null;
//		URL url = this.getClass().getResource(name);
//		try {
//			result = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
	
	public InputStream getAsStream(String name){
		return this.getClass().getResourceAsStream(name);
	}

}
