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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Rami.Sass
 */
@XmlRootElement
public class LicenseAttribution {

	/* --- Members --- */
	
	@XmlElement
	private List<String> params;
	
	@XmlElement
	private Integer id;
	
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

	/* --- Constructors --- */
	
	/**
	 * Default constructor
	 */
	public LicenseAttribution(){
		// for marshaling
	}
	
	/**
	 * Constructor
	 * 
	 * @param params
	 * @param matchname
	 * @param subrule
	 * @param before
	 * @param after
	 * @param originalLine
	 */
	public LicenseAttribution(List<String> params, Integer id, String matchname, String subrule, String before, String after, String originalLine) {
		this.params = params;
		this.id = id;
		this.matchname = matchname;
		this.subrule = subrule;
		this.before = before;
		this.after = after;
		this.originalLine = originalLine;
	}
	
	/* --- Getters / Setters --- */


	public List<String> getParams() {
		return params;
	}

	public Integer getId() {
		return id;
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
