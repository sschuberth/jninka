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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rami.Sass
 */
public class SentenceSplitter extends StageProcessor {

    /* --- Static members --- */

    private static final String SEPARATOR_BREAK_REGEX = "^([^\\.\\!\\?\\:\n]*)([\\.\\!\\?\\:\n])(?=(.?))";
    private static final Pattern SEPARATOR_BREAK_PATTERN = Pattern.compile(SEPARATOR_BREAK_REGEX, Pattern.MULTILINE);
    private static final Pattern LAST_WORD_ABBREVIATION_PATTERN = Pattern.compile("(.?)([^\\p{Punct}\\s]+)$");

    private static Logger logger = Logger.getLogger(SentenceSplitter.class.getCanonicalName());

    /* --- Members --- */

//  private Map<String, Integer> commonTerms = new Hashtable<String, Integer>();

    private List<String> abbreviations = new ArrayList<String>();

    /* --- Concrete implementation methods --- */

    public boolean process() {
        boolean result = true;
        try {
            List<String> outputInfo = new ArrayList<String>();

            String text = JNinkaUtils.joinArrayList(getInputInfo(), "\n");
            // append a "\n" just in case
            text += "\n";

            text = this.preProcessText(text);

            Pattern pat = Pattern.compile("^([^\n]*)\n", Pattern.MULTILINE);
            Matcher m = pat.matcher(text);

            StringBuffer sb = new StringBuffer();

            while (m.find() && (m.groupCount() >= 1)) {
                String curr = m.group(1);
                curr = JNinkaRegularExpression.escapeForRegex(curr);
                m.appendReplacement(sb, curr);

                // let us count the number of alphabetic chars to check if we
                // are skipping anything we should not
                int count = JNinkaUtils.alphabeticCount(curr);

                List<String> sentences = this.splitText(curr);

                int count2 = 0;
                for (String s : sentences) {
                    count2 += JNinkaUtils.alphabeticCount(s);
                    s = cleanSentence(s);
                    s = JNinkaRegularExpression.unescapeAfterRegex(s);
                    outputInfo.add(s);
                }

                if (count != count2) {
                    if (JNinkaUtils.isPrintable(curr)) {
                        logger.severe("[" + curr + "]");
                        for (String s : sentences) {
                            logger.severe(cleanSentence(s));
                        }
                    }
                    result = false;
                    logger.severe("Number of printable chars does not match!  [" + count + "][" + count2 + "]");
                }
            }

            this.setOutputInfo(outputInfo);

        } catch (Exception e) {
            result = false;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return result;
    }

    /**
     * Open and read a file, and return the lines in the file as a hashtable
     */
    public void loadAbbreviations(){
        abbreviations = new ArrayList<String>();

        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(SentenceSplitter.class.getResourceAsStream("/splitter.abv"), StandardCharsets.UTF_8));
            String line;
            while ( (line = reader.readLine()) != null ){
                line = line.toLowerCase();//java=>perl
                abbreviations.add(line);
            }
        } catch(IOException e){
            logger.log(Level.SEVERE, "cannot open abbreviations file: " + e.getMessage(), e);
        } finally {
            JNinkaUtils.close(reader, logger);
        }
    }

    /**
     * Open and read a file, and return the lines in the file as a hashtable
     */
    public void loadDictionary() {
//      commonTerms = new Hashtable<String, Integer>();
//
//      BufferedReader reader = null;
//      try{
//          reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/splitter.dict")));
//          String line;
//          while ( (line = reader.readLine()) != null ){
//              if (JNinkaRegularExpression.isMatch(line, "^[A-Z]")){
//                  commonTerms.put(line, 1);
//              }
//          }
//      } catch(IOException e) {
//          logger.log(Level.SEVERE, "cannot open dictionary file: " + e.getMessage(), e);
//      } finally {
//            JNinkaUtils.close(reader, logger);
//      }
    }

    /* --- Private methods --- */

    private String cleanSentence(String text){
        //check for trailing bullets of different types
        text = JNinkaRegularExpression.applyReplace(text, "^o ", "");
        text = JNinkaRegularExpression.applyReplace(text, "^\\s*[0-9]+\\s*[\\-\\)]", "");
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]+", "");
        text = JNinkaRegularExpression.applyReplace(text, "[ \t]+$", "");
        //remove a trailing -
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]*[\\-\\.\\s*] +", "");
        //replace quotes
        text = JNinkaRegularExpression.applyReplace(text, "\\s+", " ");
        text = JNinkaRegularExpression.applyReplace(text, "['\"`]+", "<quotes>");

        text = JNinkaRegularExpression.applyReplace(text, ":", "<colon>");
        text = JNinkaRegularExpression.applyReplace(text, "\\.+$", ".");
        if ( text.matches("\n") ){
            throw new IllegalArgumentException("text cannot be \\n");
        }
        return text;
    }

    /**
    * Open and read a file, and return the lines in the file as a  hashtable
     * @throws Exception
    */
    private List<String> splitText(String text) throws Exception {
        //int len = 0;
        List<String> result = new ArrayList<String>();
        StringBuilder currentSentence = new StringBuilder();

        /*
        this breaks the sentence into
        1. Any text before a separator
        2. The separator [.!?:\n]
        3.
        */
        Matcher matcher = SEPARATOR_BREAK_PATTERN.matcher(text);
        while(matcher.find()) {
            String sentenceMatch = JNinkaRegularExpression.getGroupValue(matcher, 1);
            String punctuation = JNinkaRegularExpression.getGroupValue(matcher, 2);
            String sentence = sentenceMatch + punctuation;
            String after = JNinkaRegularExpression.getGroupValue(matcher, 3);
            text = JNinkaRegularExpression.postMatch(SEPARATOR_BREAK_PATTERN, text);//!!!put after all operations

            //if next character is not a space, then we are not in a sentence"
            if (!" ".equals(after) && !"\t".equals(after)) {
                currentSentence.append(sentence);
                continue;
            }

            //at this point we know that there is a space after
            if (":".equals(punctuation) || "?".equals(punctuation) || "!".equals(punctuation)){
                //let us consider this right here a beginning of a sentence
                result.add(currentSentence + sentence);
                currentSentence.setLength(0);
                continue;
            }
            if (".".equals(punctuation)){
                //we have a bunch of alternatives
                //for the time being just consider a new sentence

                /*
                    TODO
                    simple heuristic... let us check that the next words are not the beginning of a sentence
                    in our library
                    ENDTODO
                */

                //is the last word an abbreviation? For this the period has to follow the word
                //this expression might have to be updated to take care of special characters in names :(
                Matcher matcher2 = LAST_WORD_ABBREVIATION_PATTERN.matcher(sentenceMatch);
                if (matcher2.matches()) {
                    String before = JNinkaRegularExpression.getGroupValue(matcher2, 1);
                    String lastWord = JNinkaRegularExpression.getGroupValue(matcher2, 2);

                    //is it an abbreviation
                    if (lastWord.length() == 1 ){
                        //single character abbreviations are special...
                        //we will assume they never split the sentence if they are capitalized.
                        char c = lastWord.charAt(0);
                        if ((c >= 'A') && (c <= 'Z')){
                            currentSentence.append(sentence);
                            continue;
                        }
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("last word an abbrev " + sentenceMatch + " lastword [" + lastWord + "] before [" + before + "]");
                        }

                        //but some are lowercase!
                        if ((c == 'e') || (c == 'i')){
                            currentSentence.append(sentence);
                            continue;
                        }
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("2 last word an abbrev " + sentenceMatch + " lastword [" + lastWord + "] before [" + before + "]");
                        }
                    } else {
                        lastWord = lastWord.toLowerCase();
                        //only accept abbreviations if the previous char to the abbrev is space or
                        //is empty (beginning of line). This avoids things like .c
                        if (("".equals(before) || " ".equals(before)) && this.abbreviations.contains(lastWord)) {
                            currentSentence.append(sentence);
                            continue;
                        }
//                      else {
//                          //just keep going, we handle this case below
//                      }
                    }
                }
                result.add(currentSentence + sentence);
                currentSentence.setLength(0);
                continue;
            }

            logger.severe("We have not dealt with this case");
            throw new IllegalStateException();
        }

        result.add(currentSentence + text);

        return result;
    }

    private String preProcessText(String text){
        text = JNinkaRegularExpression.applyReplace(text, "\\+?\\-{3,1000}\\+?", " ", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "={3,1000}", " ", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, ":{3,1000}", " ", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "\\*{3,1000}", " ", Pattern.MULTILINE);

        //some characters are used for prettyprinting but never appear in sentences
        text = JNinkaRegularExpression.applyReplace(text, "\\|+", " ", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "\\\\+", " ", Pattern.MULTILINE);

        //let us deal with /* before we do anything
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]*/\\*", "", Pattern.MULTILINE); //Last Bug!!!!
        text = JNinkaRegularExpression.applyReplace(text, "\\*\\/[ \t]*$", "", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "([^:])//", "$1", Pattern.MULTILINE);

        //Replace /\r\n/ with \n only
        text = JNinkaRegularExpression.applyReplace(text, "\r\n", "\n");

        //now, try to replace the leading/ending character of each line #/-, at most 3 heading characters
        // and each repeated as many times as necessaary
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]{0,3}[\\*\\#\\/\\;]+", "", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]{0,3}[\\-]+", "", Pattern.MULTILINE);

        text = JNinkaRegularExpression.applyReplace(text, "[\\*\\#\\/]+[ \t]{0,3}$", "", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "[\\-]+[ \t]{0,3}$", "", Pattern.MULTILINE);
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]{0,3}[\\*\\#\\/\\;]+", "", Pattern.MULTILINE);

        //now, try to replace the ending character of each line if it is * or #
        text = JNinkaRegularExpression.applyReplace(text, "[\\*\\#]+$", "", Pattern.MULTILINE);

        //at this point we have lines with nothing but spaces, let us get rid of them
        text = JNinkaRegularExpression.applyReplace(text, "^[ \t]+$", "\n", Pattern.MULTILINE);

        //let us try the following trick
        // We first get rid of \t and replace it with ' '
        // we then use \t as a "single line separator" and \n as multiple line.
        // so we can match each with a single character.
        text = JNinkaRegularExpression.applyReplace(text, "\t", " ");

        text = JNinkaRegularExpression.applyReplace(text, "\n(?!\n)", "\t");//MIKL - some problem!!!
        text = JNinkaRegularExpression.applyReplace(text, "\n\n+", "\n");

        text += "\n";

        return text;
    }

}
