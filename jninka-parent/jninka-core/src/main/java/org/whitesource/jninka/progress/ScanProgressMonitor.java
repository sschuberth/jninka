package org.whitesource.jninka.progress;

import java.util.ArrayList;
import java.util.List;

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
public class ScanProgressMonitor {

	// private members

	private int max;
	private int current;
	private int currentStep;
	private int reportInterval;

	private List<ScanProgressListener> listeners;

	// constructor

	public ScanProgressMonitor() {
		reset();
		listeners = new ArrayList<ScanProgressListener>();
	}

	// public methods
	
	public void reset(){
		current = 0;
		currentStep = 0;
	}

	public void setParams(int max, int reportInterval) {
		this.max = max;
		this.reportInterval = reportInterval;
	}

	public void addListener(ScanProgressListener listener) {
		this.listeners.add(listener);
	}

	public void progress(int increment, String details) {
		current += increment;
		if (current / reportInterval > currentStep) {
			currentStep = current / reportInterval;
			reportProgress(details);
		}
	}

	// private methods

	private void reportProgress(String details) {
		int progress = (100 * current) / max;
		for (ScanProgressListener listener : listeners) {
			listener.progress(progress, details);
		}
		// logger.info(progress + "%...");
	}
}
