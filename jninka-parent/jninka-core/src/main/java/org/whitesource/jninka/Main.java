package org.whitesource.jninka;

import java.io.File;

import org.whitesource.jninka.model.ScanResults;
import org.whitesource.jninka.progress.ScanProgressListener;

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
public class Main {

	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				long time = System.currentTimeMillis();
				File sourceFolder = new File(args[0]);
				Ninka ninka = new Ninka();
				ninka.getMonitor().addListener(getListener());
				ScanResults scanResults = ninka.scanFolderRecursive(sourceFolder, true);
				scanResults.writeXML(new File(args[1]));
				System.out.println();
				System.out.println(scanResults);
				time = (System.currentTimeMillis() - time) / 1000;
				System.out.println("Running time: " + time + "s");
			} else {
				System.out.println("usage: jninka <source-folder> <output-xml-file>");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static ScanProgressListener getListener(){
		ScanProgressListener result = new ScanProgressListener() {
			@Override
			public void progress(int pct, String details) {
				System.out.println(pct + "%, " + details);
			}
		};
		return result;
	}

}
