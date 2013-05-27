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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public final class JNinkaRegullarExpression {

	/* --- Static members --- */

    private static final int fakeFlag = -987;
	
	/* --- Public static methods --- */

    /**
     * @param text
     * @param patternText
     * @param replaceText
     * @return
     */
    public static String applyReplace(String text, String patternText, String replaceText) {
        return JNinkaRegullarExpression.applyReplace(text, patternText, replaceText, JNinkaRegullarExpression.fakeFlag);
    }

    /**
     * @param text
     * @param patternText
     * @param replaceText
     * @param flag
     * @return
     */
    public static String applyReplace(String text, String patternText, String replaceText, int flag) {
        String result = "";
        try {
            Pattern pat;
            if (flag != JNinkaRegullarExpression.fakeFlag) {
                pat = Pattern.compile(patternText, flag);
            } else {
                pat = Pattern.compile(patternText);
            }
            Matcher m = pat.matcher(text);

            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, replaceText);
            }
            m.appendTail(sb);
            result = sb.toString();
        } catch (Exception e) {
            System.out.println("applyReplace:::" + e.getMessage());
        }
        return result;
    }

    /**
     * @param text
     * @param patternText
     * @param group
     * @param flag
     * @return
     */
    public static String getGroupValue(String text, String patternText, int group, int flag) {
        String result = "";
        try {
            Pattern pat;
            if (flag != JNinkaRegullarExpression.fakeFlag) {
                pat = Pattern.compile(patternText, flag);
            } else {
                pat = Pattern.compile(patternText);
            }
            Matcher m = pat.matcher(text);

            if (m.find()) {
                result = getGroupValue(m, group);
            }
        } catch (Exception e) {
            System.out.println("getGroupValue:::" + e.getMessage());
        }

        return result;
    }

    public static String getGroupValue(Matcher matcher, int group) {
        return (group >= 0 && group <= matcher.groupCount()) ? matcher.group(group) : "";
    }

    /**
     * @param text
     * @param patternText
     * @param flag
     * @return
     */
    public static boolean isMatch(String text, String patternText, int flag) {
        Pattern pat;
        if (flag == JNinkaRegullarExpression.fakeFlag) {
            pat = Pattern.compile(patternText);
        } else {
            pat = Pattern.compile(patternText, flag);
        }

        return pat.matcher(text).find();
    }

    /**
     * @param text
     * @return
     */
    public static String escapeForRegex(String text) {
        if (text.contains("$")) {
            StringBuffer sb = new StringBuffer();
            for (char c : text.toCharArray()) {
                if (c == '$') {
                    sb.append("__DOLLAR_SIGN__");
                } else {
                    sb.append(c);
                }
            }
            text = sb.toString();
        }
        return text;
    }

    public static String unescapeAfterRegex(String text) {
        return text.replaceAll("__DOLLAR_SIGN__", "\\$");
    }

    public static String beforeMatch(Pattern pattern, String text) {
        return JNinkaRegullarExpression.beforePostMatch(pattern, text, true);
    }

    public static String postMatch(Pattern pattern, String text) {
        return JNinkaRegullarExpression.beforePostMatch(pattern, text, false);
    }
	
	/* --- Private static methods --- */

    private static String beforePostMatch(Pattern pattern, String text, boolean isBeforeMatch) {
        String result = "";
        try {
            Matcher m = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            if (m.find()) {
                m.appendReplacement(sb, "");
            }
            String before = sb.toString();
            StringBuffer sb2 = new StringBuffer();
            m.appendTail(sb2);
            String after = sb2.toString();

            result = isBeforeMatch ? before : after ;
        } catch (Exception e) {
            System.out.println("beforePostMatch:::" + e.getMessage());
        }

        return result;
    }

	/* --- Constructors --- */

    /**
     * Private constructor
     */
    private JNinkaRegullarExpression() {
        // avoid instatiaion
    }


}
