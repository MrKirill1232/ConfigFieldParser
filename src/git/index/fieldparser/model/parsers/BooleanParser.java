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
package git.index.fieldparser.model.parsers;

import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;

import java.util.Locale;

public class BooleanParser implements IFieldParser<Boolean>
{
    private BooleanParser()
    {
        // singleton class
    }

    @Override
    public Class<Boolean> getParsableClass()
    {
        return Boolean.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G parseValue(String value, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if ((fieldClassRef == null) || (fieldClassRef.getRawClass() == null))
        {
            return defaultValue;
        }
        String normalizeString = normalizeValue(value);
        if (normalizeString.length() == 1)
        {
            char charAtZero = normalizeString.charAt(0);
            if (charAtZero == 'y' || charAtZero == '1')
            {
                return ((G) ((Boolean) true));
            }
            if (charAtZero == 'n' || charAtZero == '0')
            {
                return ((G) ((Boolean) false));
            }
        }
        else
        {
            if (normalizeString.equals("true") || normalizeString.equals("yes") || normalizeString.equals("on"))
            {
                return ((G) ((Boolean) true));
            }
            if (normalizeString.equals("false") || normalizeString.equals("no") || normalizeString.equals("off"))
            {
                return ((G) ((Boolean) false));
            }
        }
        return defaultValue;
    }

    public static String normalizeValue(String inputString)
    {
        return inputString.toLowerCase(Locale.ROOT);
    }

    private final static BooleanParser INSTANCE = new BooleanParser();

    public static BooleanParser getInstance()
    {
        return INSTANCE;
    }
}
