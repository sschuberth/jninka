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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rami.Sass
 */
@XmlRootElement(name="CodeScan")
public class ScanResults {
	
	/* --- Members --- */
	
	@XmlElement(name="File")
	private List<CodeFileAttributions> findings;
	
	@XmlAttribute(name="scanTime")
	private Date scanTime;
	
	/* --- Constructors --- */
	
	/**
	 * Default constructor
	 */
	public ScanResults(){
		scanTime = new Date();
		findings = new ArrayList<CodeFileAttributions>();
	}
	
	/* --- Public methods  --- */
	
	/**
	 * The method add the given attribution to this scan results.
	 * 
	 * @param attributions Code files attributions to add.
	 */
	public void addFindings(Collection<CodeFileAttributions> attributions){
		findings.addAll(attributions);
	}
	
	/**
	 * The method write this scan results to a XML file.
	 * 
	 * @param target Output file location.
	 */
	public void writeXML(File target){
		try {
			JAXBContext jc = JAXBContext.newInstance(ScanResults.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(this, target);
		} catch (JAXBException e) {
			Logger.getLogger(ScanResults.class.getCanonicalName()).log(Level.SEVERE, "Unable to marshal " + e.getMessage(), e);
		}
	}
	
	/* --- Overridden methods  --- */
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("scanTime = ").append(SimpleDateFormat.getInstance().format(scanTime)).append("\n\n");
		for(CodeFileAttributions attribution : findings){
			sb.append(attribution).append("\n");
		}
		return sb.toString();
	}

	/* --- Getters / Setters --- */
	
	public List<CodeFileAttributions> getfindings(){
		return findings;
	}

}
