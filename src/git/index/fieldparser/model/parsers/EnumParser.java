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

import git.index.fieldparser.FieldParserManager;
import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;

public class EnumParser implements IFieldParser<Enum>
{
    private EnumParser()
    {
        // singleton class
    }

    @Override
    public Class<Enum> getParsableClass()
    {
        return Enum.class;
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
        if (!fieldClassRef.getRawClass().isEnum())
        {
            return defaultValue;
        }
        Object[] enumValueArray = fieldClassRef.getRawClass().getEnumConstants();
        IFieldParser<?> fieldParser = FieldParserManager.getInstance().applyParserFromClass(Integer.class);
        if (fieldParser != null)
        {
            Integer ordinalNumber = fieldParser.parseValue(value, new FieldClassRef<Integer>(Integer.class), null);
            if (ordinalNumber != null)
            {
                return ((G) enumValueArray[ordinalNumber]);
            }
        }
        for (Object enumValue : enumValueArray)
        {
            if (!enumValue.toString().equalsIgnoreCase(value))
            {
                continue;
            }
            return ((G) enumValue);
        }
        return defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G castValue(Object object, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (object == null)
        {
            return defaultValue;
        }
        if ((fieldClassRef == null) || (fieldClassRef.getRawClass() == null))
        {
            return defaultValue;
        }
        if (object.getClass() != fieldClassRef.getRawClass())
        {
            return defaultValue;
        }
        return ((G) object);
    }

    private final static EnumParser INSTANCE = new EnumParser();

    public static EnumParser getInstance()
    {
        return INSTANCE;
    }
}
