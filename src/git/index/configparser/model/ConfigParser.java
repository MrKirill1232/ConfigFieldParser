/*
 * The MIT License (MIT)
 *
 * Copyright 2025 MrKirill1232 (aka Butolin Kyrylo)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package git.index.configparser.model;

import git.index.dummylogger.LoggerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigParser
{
    private final static Pattern REPLACE_PATTERN_ON_NOTHING = Pattern.compile("\\p{C}");
    private final static Pattern REPLACE_PATTERN_ON_WHITESPACES = Pattern.compile("&(nbsp);");

    private final LoggerImpl _logger;

    private final File _configFilePath;
    private final Charset _characterSet;

    private final Map<String, String> _parsedConfigData;

    public ConfigParser(File configFilePath, Charset characterSet)
    {
        _logger             = new LoggerImpl(getClass());

        _configFilePath     = configFilePath    ;
        _characterSet       = characterSet      ;

        _parsedConfigData   = new HashMap<>()   ;
    }

    public Collection<String> keySet()
    {
        return _parsedConfigData.keySet();
    }

    public Collection<String> values()
    {
        return _parsedConfigData.values();
    }

    public String getValueByKey(String key, String defaultValue)
    {
        return _parsedConfigData.getOrDefault(key, defaultValue);
    }

    public void load()
    {
        if ((_configFilePath == null) || (!_configFilePath.exists()))
        {
            return;
        }
        readFilePerLines();
    }

    private void readFilePerLines()
    {
        int lineNumber = 0;
        try (
                LineHandler lineHandler = new LineHandler(this);
                LineNumberReader lnr = new LineNumberReader(Files.newBufferedReader(_configFilePath.toPath(), _characterSet))
        )
        {
            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                lineNumber += 1;
                line = line.trim();
                lineHandler.handleLine(line);
            }
        }
        catch (FileNotFoundException e)
        {
            _logger.error("File [" + _configFilePath + "] not found" + ".", e);
        }
        catch (Exception e)
        {
            _logger.error("File [" + _configFilePath + "] loading failed. Line number " + "[" + lineNumber + "]" + ".", e);
        }
    }

    private final static class LineHandler implements AutoCloseable
    {
        private final ConfigParser _instanceOfParser;

        private StringBuilder _fullLineOfString;

        private LineHandler(ConfigParser instanceOfParser)
        {
            _instanceOfParser = instanceOfParser;
            _fullLineOfString = new StringBuilder();
        }

        public void handleLine(String inputLine)
        {
            if (inputLine.isEmpty())
            {
                return;
            }
            String replaceLine = replaceInvalidCharacters(inputLine);
            if (replaceLine.isEmpty())
            {
                return;
            }
            // examples:
            // [{\n}]
            // [abobus = abobus{\\}{\n}]
            if (isCommentChar(replaceLine.charAt(0)))
            {   // skip commentary
                return;
            }
            _fullLineOfString.append(replaceLine);
            char lastCharacterOfLine = replaceLine.charAt(replaceLine.length() - 1);
            if (isAppendNextLineChar(lastCharacterOfLine))
            {
                _fullLineOfString.deleteCharAt(_fullLineOfString.length() - 1);
                return;
            }
            String[] splitKeyAndValue = splitKeyAndValue(_fullLineOfString.toString());
            _instanceOfParser._parsedConfigData.put(splitKeyAndValue[0], splitKeyAndValue[1]);
            _fullLineOfString = new StringBuilder();
        }

        @Override
        public void close() throws Exception
        {
            _fullLineOfString.setLength(0);
            _fullLineOfString = null;
        }
    }

    private static boolean isCommentChar(char lookingCharacter)
    {
        return lookingCharacter == '#';
    }

    private static boolean isAppendNextLineChar(char lookingCharacter)
    {   // '\'
        return lookingCharacter == '\\';
    }

    private static String[] splitKeyAndValue(String configLine)
    {
        String[] splitConfigLine = configLine.split("=", 2);
        splitConfigLine[0] = splitConfigLine[0].trim();
        splitConfigLine[1] = splitConfigLine[1].trim();
        return splitConfigLine;
    }

    private static String replaceInvalidCharacters(String line)
    {
        String replaced = line;
        replaced = REPLACE_PATTERN_ON_NOTHING.matcher(replaced).replaceAll("");
        replaced = REPLACE_PATTERN_ON_WHITESPACES.matcher(replaced).replaceAll(" ");
        return replaced;
    }
}
