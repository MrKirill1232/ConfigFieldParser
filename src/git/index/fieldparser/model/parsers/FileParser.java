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

import java.io.File;

public class FileParser implements IFieldParser<File>
{
    private FileParser()
    {
        // singleton class
    }

    @Override
    public Class<File> getParsableClass()
    {
        return File.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G parseValue(String value, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (!getParsableClass().isAssignableFrom(fieldClassRef.getRawClass()))
        {
            return defaultValue;
        }
        return ((G) new File(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G castValue(Object object, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (object == null)
        {
            return defaultValue;
        }
        if (!getParsableClass().isAssignableFrom(object.getClass()))
        {
            return defaultValue;
        }
        return ((G) object);
    }

    private final static FileParser INSTANCE = new FileParser();

    public static FileParser getInstance()
    {
        return INSTANCE;
    }
}
