package org.whitesource.jninka.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement
public class CodeFileAttributions {

	private String fileName;
	private String extra;
	private long lastModified;
	private List<LicenseAttribution> attribution;
	
	public CodeFileAttributions(){
		// For marshaling
	}


	public CodeFileAttributions(List<LicenseAttribution> attributions, String fileName, long lastModified) {
		this.attribution = attributions;
		this.fileName = fileName;
		this.setLastModified(lastModified);
	}

	public List<LicenseAttribution> getAttribution() {
		return attribution;
	}

	public void setAttribution(List<LicenseAttribution> attributions) {
		this.attribution = attributions;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setExtra(String extra) {
		this.extra = extra;
	}


	public String getExtra() {
		return extra;
	}
	
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}


	public long getLastModified() {
		return lastModified;
	}
	
	@Override
	public String toString() {
		return fileName + " - " + attribution.size();
	}
}
