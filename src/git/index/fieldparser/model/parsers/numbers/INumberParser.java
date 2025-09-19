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
package git.index.fieldparser.model.parsers.numbers;

import git.index.fieldparser.interfaces.IFieldParser;

import java.math.BigDecimal;

public interface INumberParser<T> extends IFieldParser<T>
{
    public default boolean isNumber(String input)
    {
        if (input.isEmpty())
        {
            return false;
        }
        // default value for 'decimal' numbers ;d
        // decimal can have a at least 1 dot
        // integer cannot have a dots in value
        boolean isDotFound = !isDecimal();
        for (int index = 0; index < input.length(); index++)
        {
            char requestCharacter = input.charAt(index);
            if (index == 0 && requestCharacter == '-')
            {
                continue;
            }
            if (requestCharacter == '.')
            {
                if (index == 0)
                {
                    return false;
                }
                if (isDotFound)
                {
                    return false;
                }
                isDotFound = true;
                continue;
            }
            if (!Character.isDigit(requestCharacter))
            {
                return false;
            }
        }
        return true;
    }

    public abstract boolean checkLimit(BigDecimal bigDecimal);

    public abstract boolean isDecimal();

    public default String normalizeValueImpl(String inputString)
    {
        return normalizeValue(inputString);
    }

    public static String normalizeValue(String inputString)
    {
        String trimmedString = inputString
                .replaceAll("&nbsp","")
                .trim();

        StringBuilder stringBuilder = new StringBuilder(trimmedString.length());

        for (int index = 0; index < trimmedString.length(); index++)
        {
            char charByIndex = trimmedString.charAt(index);
            if ((index == 0) && (charByIndex == '-'))
            {
                stringBuilder.append(charByIndex);
                continue;
            }
            switch (charByIndex)
            {
                case '\n':
                case '\t':
                case '\r':
                case '\0':

                case ' ':
                // maybe someone wanna type 880-555-35-35
                case '-':
                // java style 880_555_35_35
                case '_':
                {
                    continue;
                }
                default:
                {
                    break;
                }
            }
            stringBuilder.append(charByIndex);
        }
        return stringBuilder.toString();

//        return inputString
//                .replaceAll("\n", "")
//                .replaceAll("\t", "")
//                .replaceAll("\r", "")
//                .replaceAll("_", "")
//                .replaceAll(" ", "")
//                .replaceAll("\0", "")
//                .replaceAll("&nbsp","")
//                .trim();
    }
}
