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
package org.whitesource.jninka.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rami.Sass
 */
public class ScanProgressMonitor {

    /* --- Members --- */

    private int max;

    private int current;

    private int currentStep;

    private int reportInterval;

    private List<ScanProgressListener> listeners;

    /* --- Constructors --- */

    /**
     * Default constructor
     */
    public ScanProgressMonitor() {
        current = 0;
        currentStep = 0;
        listeners = new ArrayList<ScanProgressListener>();
    }

    /* --- Public methods --- */

    /**
     * The method reset this progress monitor.
     */
    public void reset(){
        current = 0;
        currentStep = 0;
    }

    /**
     * The method set the parameters of this progress monitor to the given values.
     *
     * @param max Maximal progress value.
     * @param reportInterval Report interval value.
     */
    public void setParams(int max, int reportInterval) {
        this.max = max;
        this.reportInterval = reportInterval;
    }

    /**
     * The method add the given {@link ScanProgressListener} to the list of listeners.
     *
     * @param listener Listener to add.
     */
    public void addListener(ScanProgressListener listener) {
        this.listeners.add(listener);
    }

    /**
     * The method increment this progress monitor.
     *
     * @param increment Progress increment value.
     * @param details Details message to display.
     */
    public void progress(int increment, String details) {
        current += increment;
        if (current / reportInterval > currentStep) {
            currentStep = current / reportInterval;
            reportProgress(details);
        }
    }

    /* --- Private methods --- */

    /**
     * Report progress to all listeners.
     *
     * @param details
     */
    private void reportProgress(String details) {
        int progress = (100 * current) / max;
        for (ScanProgressListener listener : listeners) {
            listener.progress(progress, details);
        }
    }

}
