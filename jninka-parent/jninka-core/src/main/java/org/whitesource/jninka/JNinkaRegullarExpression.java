package org.whitesource.jninka;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class JNinkaRegullarExpression {
	private static int fakeFlag = -987;
	
	public static String replaceLast(String string, String toReplace, String replacement) 
	{
		int pos = string.lastIndexOf(toReplace);
		if ( pos > -1 ){
			return string.substring(0, pos)
				 + replacement
				 + string.substring(pos + toReplace.length(), string.length());
		} else {
			return string;
		}
	}
	
	public static String applyReplace(String text, String patternText, String replaceText){ 
		return JNinkaRegullarExpression.applyReplace(text, patternText, replaceText, JNinkaRegullarExpression.fakeFlag);		
	}
	
	public static String applyReplace(String text, String patternText, String replaceText, int flag){ 
		String result = "";
		try
		{
			Pattern pat;
			if ( flag != JNinkaRegullarExpression.fakeFlag )
			{
				pat = Pattern.compile(patternText,flag);
			} else {
				pat = Pattern.compile(patternText);
			}
			Matcher m = pat.matcher(text);  
			
			StringBuffer sb = new StringBuffer();
			while ( m.find() ){
				m.appendReplacement(sb, replaceText);
			}
			m.appendTail(sb);
			result = sb.toString();
		} catch(Exception e){
			System.out.println("applyReplace:::"+ e.getMessage());
		}
		return result;		
	}
	
	public static String getGroupValue(String text, String patternText, int group){ 
		return JNinkaRegullarExpression.getGroupValue(text, patternText, group, JNinkaRegullarExpression.fakeFlag);		
	}
	
	public static String getGroupValue(String text, String patternText, int group, int flag){ 
		String result = "";
		try
		{
			Pattern pat;
			if ( flag != JNinkaRegullarExpression.fakeFlag ){
				pat = Pattern.compile(patternText,flag);
			} else {
				pat = Pattern.compile(patternText);
			}
			Matcher m = pat.matcher(text);  
			
			if( m.find() ){
				if ( (group >= 0) && (group <= m.groupCount()) ){
					result = m.group(group);
				} else {
					result = "";			
				}		
			}
		} catch(Exception e){
			System.out.println("getGroupValue:::"+ e.getMessage());
		}
		return result;
		
	}	
	
	public static boolean isMatch(String text, String patternText){ 
		return JNinkaRegullarExpression.isMatch(text, patternText, JNinkaRegullarExpression.fakeFlag);		
	}
	
	public static boolean isMatch(String text, String patternText, int flag){
		boolean result = false;
		try
		{
			Pattern pat;
			if (flag != JNinkaRegullarExpression.fakeFlag)
			{
				pat = Pattern.compile(patternText,flag);
			} else {
				pat = Pattern.compile(patternText);
			}
			Matcher matcher = pat.matcher(text);
	
			result = matcher.find();//matcher.matches() ? true : false;
		} catch(Exception e){
			System.out.println("isMatch:::"+ e.getMessage());
		}
		return result;
	}
	
	public static String escapeForRegex(String text){
		if ( text.contains("$") ){
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
	public static String unescapeAfterRegex(String text){
		return text.replaceAll("__DOLLAR_SIGN__", "\\$");
	}

	public static String beforeMatch(String text, String patternText){ 
		return JNinkaRegullarExpression.beforeMatch(text, patternText, JNinkaRegullarExpression.fakeFlag);		
	}
	
	public static String beforeMatch(String text, String patternText, int flag){
		String result = JNinkaRegullarExpression.beforePostMatch(text, patternText, flag, true);
		return result;
	}

	public static String postMatch(String text, String patternText){ 
		return JNinkaRegullarExpression.postMatch(text, patternText, JNinkaRegullarExpression.fakeFlag);		
	}
	
	public static String postMatch(String text, String patternText, int flag){		
		return JNinkaRegullarExpression.beforePostMatch(text, patternText, flag, false);
	}
	
	private static String beforePostMatch(String text, String patternText, int flag, boolean isBeforeMatch){
		String result = "";
		try
		{
			 Pattern p = Pattern.compile(patternText, flag);			
			 Matcher m = p.matcher(text);
			 StringBuffer sb = new StringBuffer();
			 while ( m.find() ){
			     m.appendReplacement(sb, "");
			     break;
			 }
			 String before = sb.toString();
			 StringBuffer sb2 = new StringBuffer();
			 m.appendTail(sb2);
			 String after = sb2.toString();
			 if( isBeforeMatch ){
				 return before; 
			 } else {
				 return after; 
			 }			
		} catch(Exception e){
			System.out.println("beforePostMatch:::"+ e.getMessage());
		}
		return result;
	}
}
