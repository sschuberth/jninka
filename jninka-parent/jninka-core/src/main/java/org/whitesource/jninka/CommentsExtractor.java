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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public class CommentsExtractor extends StageProcessor {

    /* --- Static members --- */
    public static final int DEFAULT_COMMENTS_LENGTH = 700;

    private static final Map<Pattern, Integer> commentsLengthMap = new HashMap<Pattern, Integer>();
    static {
        commentsLengthMap.put(JNinkaUtils.PERL_EXT_PATTERN, 400);
        commentsLengthMap.put(JNinkaUtils.LISP_EXT_PATTERN, 400);
        commentsLengthMap.put(JNinkaUtils.PHP_EXT_PATTERN, 400);
        commentsLengthMap.put(JNinkaUtils.JAVA_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
        commentsLengthMap.put(JNinkaUtils.JS_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
        commentsLengthMap.put(JNinkaUtils.C_CPP_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
        commentsLengthMap.put(JNinkaUtils.DOT_NET_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
        commentsLengthMap.put(JNinkaUtils.AS_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
        commentsLengthMap.put(JNinkaUtils.OBJECTIVE_C_EXT_PATTERN, DEFAULT_COMMENTS_LENGTH);
    }

    private static Logger logger = Logger.getLogger(CommentsExtractor.class.getCanonicalName());

    /* --- Members --- */

    private String inputFile = "";

    /* --- Public methods --- */

    public boolean process() {
        boolean result = true;

        if (getInputFile().length() <= 0){
            logger.severe("Failed to retrieve file size info: file " + getInputFile() + " doesn\'t exist or empty.");
            result = false;
        } else {
            BufferedReader reader = null;
            try{
                List<String> outputInfo = new ArrayList<String>();

                int totalLineCount = commentsLength(getInputFile());
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(getInputFile()), StandardCharsets.UTF_8));
                String line;
                int i = totalLineCount > 0 ? 0 : 1;
                while (((line = reader.readLine()) != null) && (i < totalLineCount)) {
                    i++;
                    outputInfo.add(line);
                }

                this.setOutputInfo(outputInfo);
            } catch (IOException e){
                result = false;
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            finally {
                JNinkaUtils.close(reader, logger);
            }
        }

        return result;
    }

    /* --- Private methods --- */

    private int commentsLength(String filepath){
        Integer lineCount = null;

        String ext = JNinkaUtils.fileExtension(filepath);
        if (JNinkaUtils.isBlank(ext)) {
            lineCount = DEFAULT_COMMENTS_LENGTH;
        } else {
            Iterator<Map.Entry<Pattern, Integer>> iterator = commentsLengthMap.entrySet().iterator();
            while (iterator.hasNext() && lineCount == null) {
                Map.Entry<Pattern, Integer> entry = iterator.next();
                if (entry.getKey().matcher(ext).matches()) {
                    lineCount = entry.getValue();
                }
            }
            if (lineCount == null) {
                lineCount = DEFAULT_COMMENTS_LENGTH;
            }
        }

        return lineCount;
    }

    /* --- Getters / Setters --- */

    public String getInputFile(){
        return this.inputFile;
    }

    public void setInputFile(String lInputFile){
        this.inputFile = lInputFile;
    }

}
