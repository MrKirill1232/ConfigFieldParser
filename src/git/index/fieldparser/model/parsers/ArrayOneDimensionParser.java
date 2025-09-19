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

import git.index.dummylogger.LoggerImpl;
import git.index.fieldparser.FieldParserManager;
import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;
import git.index.fieldparser.model.attributes.FieldAttributes;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

public class ArrayOneDimensionParser implements IFieldParser<Array[]>
{
    private final LoggerImpl _logger;

    private ArrayOneDimensionParser()
    {
        // singleton class
        _logger = new LoggerImpl(getClass());
    }

    @Override
    public Class<Array[]> getParsableClass()
    {
        return Array[].class;
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
        Class<?> arrayComponent;
        if (fieldClassRef.getRawClass().isArray())
        {
            arrayComponent = fieldClassRef.getRawClass().getComponentType();
            if (arrayComponent.isArray())
            {   // two or more dimension array;
                return defaultValue;
            }
        }
        else
        {
            arrayComponent = fieldClassRef.getRawClass();
        }
        IFieldParser<?> fieldParser = FieldParserManager.getInstance().applyParserFromClass(arrayComponent);
        if (fieldParser == null)
        {
            _logger.error("Cannot parse array of " + ("[" + arrayComponent + "]") + " because parser of class " + ("[" + arrayComponent + "]") + " is not supported!");
            return defaultValue;
        }
        String[] splitValueArray = splitValue(value, fieldClassRef.getAttribute(FieldAttributes.SPLIT_PATTERN_01, null), Pattern.compile(";"));
        Object arrayOfObjects = Array.newInstance(arrayComponent, splitValueArray.length);
        FieldClassRef<Object> reference = new FieldClassRef<Object>(arrayComponent);
        for (int index = 0; index < splitValueArray.length; index++)
        {   //
            Object parsedObject = fieldParser.parseValue(splitValueArray[index], reference, null);
            if (arrayComponent.isPrimitive() && (parsedObject == null))
            {
                _logger.error("Cannot add object " + ("['" + splitValueArray[index] + "']") + " into array, because parsed value is null, but class " + ("['" + arrayComponent.getSimpleName() + "']") + " is primitive!");
                continue;
            }
            Array.set(arrayOfObjects, index, fieldParser.parseValue(splitValueArray[index], reference, null));
        }
        return ((G) arrayOfObjects);
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
        if (IFieldParser.getDimensionsOfArray(object.getClass()) != 1)
        {
            return defaultValue;
        }
        Class<?> objectArrayComponent = object.getClass().getComponentType();
        Class<?> referrArrayComponent;
        if (fieldClassRef.getRawClass().isArray())
        {
            referrArrayComponent = fieldClassRef.getRawClass().getComponentType();
        }
        else
        {
            referrArrayComponent = fieldClassRef.getRawClass();
        }
        if (objectArrayComponent != referrArrayComponent)
        {
            return defaultValue;
        }
        return (G) object;
    }

    private static String[] splitValue(String input, Pattern spliterator, Pattern nullValue)
    {
        if (input == null || input.isEmpty())
        {
            return new String[0];
        }
        if (spliterator == null)
        {
            spliterator = nullValue;
        }
        return spliterator.split(input, -1);
    }

    private final static ArrayOneDimensionParser INSTANCE = new ArrayOneDimensionParser();

    public static ArrayOneDimensionParser getInstance()
    {
        return INSTANCE;
    }
}
