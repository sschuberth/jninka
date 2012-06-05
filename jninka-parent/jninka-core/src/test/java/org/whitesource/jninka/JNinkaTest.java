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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.whitesource.jninka.model.ScanResults;

public class JNinkaTest {

	@Test
	public void testNewNinka() {
		JNinka jninka = new JNinka();
		assertNotNull("jninka shouldn't be null", jninka);
	}
	
	@Test
	public void testAGPL() {
		JNinka jninka = new JNinka();
		assertNotNull("jninka shouldn't be null", jninka);
		
		File folder = new File(".");
		ScanResults scanResult = jninka.scanFolderRecursive(folder , true);
		assertNotNull("scanResult shouldn't be null", scanResult);
	}
	
}
