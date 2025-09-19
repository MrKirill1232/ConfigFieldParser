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

import git.index.fieldparser.model.FieldClassRef;

import java.math.BigDecimal;

public class NumberLongParser implements INumberParser<Long>
{
    public final static BigDecimal BIG_DECIMAL_MIN_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    public final static BigDecimal BIG_DECIMAL_MAX_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);

    private NumberLongParser()
    {
        // singleton class
    }

    @Override
    public boolean isNumber(String input)
    {
        if (!INumberParser.super.isNumber(input))
        {
            return false;
        }
        // 9223372036854775807
        // -9223372036854775808
        if (input.length() < 19)
        {
            return true;
        }
        if (input.length() > 19)
        {
            return false;
        }
        // -9223372036854775808
        if (input.charAt(0) != '-')
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDecimal()
    {
        return false;
    }

    @Override
    public boolean checkLimit(BigDecimal bigDecimal)
    {
        return ((bigDecimal.compareTo(BIG_DECIMAL_MIN_VALUE) >= 0) || (bigDecimal.compareTo(BIG_DECIMAL_MAX_VALUE) <= 0));
    }

    @Override
    public Class<Long> getParsableClass()
    {
        return Long.class;
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
        String normalisedValue = normalizeValueImpl(value);
        if (!isNumber(normalisedValue))
        {
            return defaultValue;
        }
        BigDecimal bigDecimal = new BigDecimal(normalisedValue);
        if (!checkLimit(bigDecimal))
        {
            return defaultValue;
        }
        return ((G) ((Long) bigDecimal.longValue()));
    }

    private final static NumberLongParser INSTANCE = new NumberLongParser();

    public static NumberLongParser getInstance()
    {
        return INSTANCE;
    }
}
