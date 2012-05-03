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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Rami.Sass
 */
@XmlRootElement(name="CodeScan")
public class ScanResults {
	
	// members
	@XmlElement(name="File")
	private List<CodeFileAttributions> findings;
	
	// constructor
	
	public ScanResults(){
		findings = new ArrayList<CodeFileAttributions>();
	}
	
	// public methods
	
	public void addFinding(CodeFileAttributions attribution){
		findings.add(attribution);
	}
	
	public List<CodeFileAttributions> getfindings(){
		return findings;
	}
	
	public void writeXML(File target){
		try {
			JAXBContext jc = JAXBContext.newInstance(ScanResults.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.marshal(this, target);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	// override methods
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(CodeFileAttributions attribution : findings){
			sb.append(attribution + "\n");
		}
		return sb.toString();
	}

}
