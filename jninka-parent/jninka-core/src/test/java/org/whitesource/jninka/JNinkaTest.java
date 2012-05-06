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
