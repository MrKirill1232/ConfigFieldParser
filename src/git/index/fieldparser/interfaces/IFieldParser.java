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
package git.index.fieldparser.interfaces;

import git.index.fieldparser.model.FieldClassRef;

public interface IFieldParser<T>
{
    public abstract Class<T> getParsableClass();

    @SuppressWarnings("unchecked")
    public abstract<G> G parseValue(String value, FieldClassRef<G> fieldClassRef, G defaultValue);

    @SuppressWarnings("unchecked")
    public default <G> G castValue(Object object, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (object == null)
        {
            return defaultValue;
        }
        if (object.getClass() != getParsableClass())
        {
            return defaultValue;
        }
        return ((G) object);
    }

    public static int getDimensionsOfArray(Object array)
    {
        if (array == null)
        {
            return 0;
        }
        int dimensions = 0;
        Class<?> classOfArray;
        if (array.getClass() == Class.class)
        {
            classOfArray = (Class<?>) array;
        }
        else
        {
            classOfArray = array.getClass();
        }
        while (classOfArray.isArray())
        {
            dimensions += 1;
            classOfArray = classOfArray.getComponentType();
        }
        return dimensions;
    }
}
