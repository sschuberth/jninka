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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public class JNinkaUtils{

    /* --- Constants --- */

    public static final String JAVA_EXT_REGEX = "java|jav|j|jsp|do";
    public static final String JS_EXT_REGEX = "js|jse|html|htm|hta|hbs";
    public static final String PERL_EXT_REGEX = "pl|pm|py";
    public static final String LISP_EXT_REGEX = "jl|el";
    public static final String PHP_EXT_REGEX = "php";
    public static final String C_CPP_EXT_REGEX = "c|cpp|h|hh|hpp|hxx|h__|\\Qh++\\Ecxx|\\Qc++\\E|cc|chh|c__";
    public static final String DOT_NET_EXT_REGEX = "c#|cs|csx|asmx|cls|asp|aspx|jsl";
    public static final String AS_EXT_REGEX = "as|mxml";
    public static final String OBJECTIVE_C_EXT_REGEX = "m|mm";
    public static final Pattern JAVA_EXT_PATTERN = Pattern.compile(JAVA_EXT_REGEX);
    public static final Pattern JS_EXT_PATTERN = Pattern.compile(JS_EXT_REGEX);
    public static final Pattern PERL_EXT_PATTERN = Pattern.compile(PERL_EXT_REGEX);
    public static final Pattern LISP_EXT_PATTERN = Pattern.compile(LISP_EXT_REGEX);
    public static final Pattern PHP_EXT_PATTERN = Pattern.compile(PHP_EXT_REGEX);
    public static final Pattern C_CPP_EXT_PATTERN = Pattern.compile(C_CPP_EXT_REGEX);
    public static final Pattern DOT_NET_EXT_PATTERN = Pattern.compile(DOT_NET_EXT_REGEX);
    public static final Pattern AS_EXT_PATTERN = Pattern.compile(AS_EXT_REGEX);
    public static final Pattern OBJECTIVE_C_EXT_PATTERN = Pattern.compile(OBJECTIVE_C_EXT_REGEX);

    public static final Pattern ALL_EXT_PATTERN = Pattern.compile(
            JAVA_EXT_REGEX+ "|" + JS_EXT_REGEX+ "|" + PERL_EXT_REGEX+ "|" + LISP_EXT_REGEX+ "|" + PHP_EXT_REGEX+ "|" +
            C_CPP_EXT_REGEX+ "|" + DOT_NET_EXT_REGEX+ "|" + AS_EXT_REGEX+ "|" + OBJECTIVE_C_EXT_REGEX);

	/* --- Public static methods --- */
	
	/**
	 * @param coll
	 * @param delimiter
	 * @return
	 * 
	 * @deprecated Consider using StringUtils instead. 
	 */
	public static String joinArrayList(List<String> coll, String delimiter){
	    if (coll.isEmpty()){
	    	return "";
	    }
	    StringBuilder sb = new StringBuilder();
	 
	    for (String x : coll){
	    	sb.append(x).append(delimiter);
	    }
	    sb.delete(sb.length() - delimiter.length(), sb.length());

        return sb.toString();
	}

    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

	public static String fileExtension(String filepath){
		return fileExtension(new File(filepath));
	}

    public static String fileExtension(File file){
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        return (index > 0) ? fileName.substring(index + 1).toLowerCase() : "";
    }

    public static boolean isSourceCode(File path) {
        String ext = fileExtension(path);
        return !isBlank(ext) && ALL_EXT_PATTERN.matcher(ext).matches();
    }

    public static int alphabeticCount(String s) {
        int count = 0;

        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                count++;
            }
        }

        return count;
    }

    public static void close(Closeable io, Logger logger) {
        try {
            if (io != null) {
                io.close();
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
	
	/* --- Constructors --- */
	
	/**
	 * Private default constructor
	 */
	private JNinkaUtils() {
		// avoid instantiation
	}
	
}
