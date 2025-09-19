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
package git.index.fieldparser;

import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.parsers.ArrayOneDimensionParser;
import git.index.fieldparser.model.parsers.ArrayTwoDimensionParser;
import git.index.fieldparser.model.parsers.BooleanParser;
import git.index.fieldparser.model.parsers.CollectionParser;
import git.index.fieldparser.model.parsers.EnumParser;
import git.index.fieldparser.model.parsers.FileParser;
import git.index.fieldparser.model.parsers.PatternParser;
import git.index.fieldparser.model.parsers.StringParser;
import git.index.fieldparser.model.parsers.numbers.NumberByteParser;
import git.index.fieldparser.model.parsers.numbers.NumberDoubleParser;
import git.index.fieldparser.model.parsers.numbers.NumberFloatParser;
import git.index.fieldparser.model.parsers.numbers.NumberIntegerParser;
import git.index.fieldparser.model.parsers.numbers.NumberLongParser;
import git.index.fieldparser.model.parsers.numbers.NumberShortParser;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FieldParserManager
{
    private final Map<Class<?>, IFieldParser<?>> _parsersMap;

    private final Map<Integer, IFieldParser<?>> _arraysParsers;

    private Function<Class<?>, IFieldParser<?>> _parserObtainer;

    private FieldParserManager()
    {
        _parsersMap = new HashMap<>();

        _parsersMap.put(String.class, StringParser.getInstance());
        _parsersMap.put(Pattern.class, PatternParser.getInstance());

        _parsersMap.put(File.class, FileParser.getInstance());
        _parsersMap.put(Enum.class, EnumParser.getInstance());
        _parsersMap.put(Collection.class, CollectionParser.getInstance());

        BooleanParser booleanParser = BooleanParser.getInstance();
        _parsersMap.put(Boolean.class, booleanParser);
        _parsersMap.put(boolean.class, booleanParser);

        NumberByteParser byteParser = NumberByteParser.getInstance();
        _parsersMap.put(Byte.class, byteParser);
        _parsersMap.put(byte.class, byteParser);
        NumberShortParser shortParser = NumberShortParser.getInstance();
        _parsersMap.put(Short.class, shortParser);
        _parsersMap.put(short.class, shortParser);
        NumberFloatParser floatParser = NumberFloatParser.getInstance();
        _parsersMap.put(Float.class, floatParser);
        _parsersMap.put(float.class, floatParser);
        NumberDoubleParser doubleParser = NumberDoubleParser.getInstance();
        _parsersMap.put(Double.class, doubleParser);
        _parsersMap.put(double.class, doubleParser);
        NumberIntegerParser integerParser = NumberIntegerParser.getInstance();
        _parsersMap.put(Integer.class, integerParser);
        _parsersMap.put(int    .class, integerParser);
        NumberLongParser longParser = NumberLongParser.getInstance();
        _parsersMap.put(Long.class, longParser);
        _parsersMap.put(long.class, longParser);

        _arraysParsers = new HashMap<>(5);

        ArrayOneDimensionParser oneDimensionParser = ArrayOneDimensionParser.getInstance();
        _arraysParsers.put(IFieldParser.getDimensionsOfArray(oneDimensionParser.getParsableClass()), oneDimensionParser);
        ArrayTwoDimensionParser twoDimensionParser = ArrayTwoDimensionParser.getInstance();
        _arraysParsers.put(IFieldParser.getDimensionsOfArray(twoDimensionParser.getParsableClass()), twoDimensionParser);

        _parserObtainer = this::getParserByFieldType;
    }

    public void addParserIntoMap(Class<?> parsableClass, IFieldParser<?> fieldParser, boolean replace)
    {
        if (parsableClass == null)
        {
            parsableClass = fieldParser.getParsableClass();
        }
        if (parsableClass.isArray())
        {
            int dimension = IFieldParser.getDimensionsOfArray(parsableClass);
            if (_arraysParsers.containsKey(dimension) && (!replace))
            {
                return;
            }
            _arraysParsers.put(dimension, fieldParser);
        }
        else
        {
            if (_parsersMap.containsKey(parsableClass) && (!replace))
            {
                return;
            }
            _parsersMap.put(parsableClass, fieldParser);
        }
    }

    public IFieldParser<?> applyParserFromClass(Class<?> fieldParserClass)
    {
        return _parserObtainer.apply(fieldParserClass);
    }

    public void overrideApplyFunction(Function<Class<?>, IFieldParser<?>> function)
    {
        if (function == null)
        {
            _parserObtainer = this::getParserByFieldType;
        }
        else
        {
            _parserObtainer = function;
        }
    }

    public IFieldParser<?> getParserByFieldType(Class<?> fieldParserClass)
    {
        if (fieldParserClass == null)
        {
            return null;
        }
        if (fieldParserClass.isArray())
        {
            int dimension = IFieldParser.getDimensionsOfArray(fieldParserClass);
            return _arraysParsers.getOrDefault(dimension, null);
        }
        if (fieldParserClass.isEnum())
        {
            return _parsersMap.getOrDefault(Enum.class, null);
        }
        IFieldParser<?> fieldParser = _parsersMap.getOrDefault(fieldParserClass, null);
        if (fieldParser != null)
        {
            return fieldParser;
        }
        if (Collection.class.isAssignableFrom(fieldParserClass))
        {
            return _parsersMap.getOrDefault(Collection.class, null);
        }
        return null;
    }

    private final static FieldParserManager INSTANCE = new FieldParserManager();

    public final static FieldParserManager getInstance()
    {
        return INSTANCE;
    }
}
