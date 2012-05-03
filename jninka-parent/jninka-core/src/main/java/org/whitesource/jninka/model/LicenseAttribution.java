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
package org.whitesource.jninka.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rami.Sass
 */
@XmlRootElement
public class LicenseAttribution {

	// members
	
	@XmlElement
	private List<String> params;
	@XmlElement
	private String matchname;
	@XmlElement
	private String subrule;
	@XmlElement
	private String before;
	@XmlElement
	private String after;
	@XmlElement
	private String originalLine;

	// constructor
	
	public LicenseAttribution(){
		// for marshaling
	}
	
	public LicenseAttribution(List<String> params, String matchname, String subrule, String before, String after, String originalLine) {
		this.params = params;
		this.matchname = matchname;
		this.subrule = subrule;
		this.before = before;
		this.after = after;
		this.originalLine = originalLine;
	}
	
	// getters

	public List<String> getParams() {
		return params;
	}

	public String getMatchname() {
		return matchname;
	}

	public String getSubrule() {
		return subrule;
	}

	public String getBefore() {
		return before;
	}

	public String getAfter() {
		return after;
	}

	public String getOriginalLine() {
		return originalLine;
	}
	
}
