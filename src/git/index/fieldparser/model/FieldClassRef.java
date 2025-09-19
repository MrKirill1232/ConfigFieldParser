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
package git.index.fieldparser.model;

import git.index.fieldparser.model.attributes.AttributeKey;

import java.util.HashMap;
import java.util.Map;

public class FieldClassRef<T>
{
    private final Class<?> _mainType;
    private final Class<?>[] _actualClassArguments;

    private final Map<AttributeKey<?>, Object> _attributes;

    public FieldClassRef(Class<?> mainType, Class<?>... subGenerics)
    {
        _mainType = mainType;
        _actualClassArguments = subGenerics;
        _attributes = new HashMap<>();
    }

    public Class<?> getRawClass()
    {
        return _mainType;
    }

    public Class<?>[] getActualClassArguments()
    {
        return _actualClassArguments;
    }

    public <V> FieldClassRef<T> withAttribute(AttributeKey<V> key, V value)
    {
        key.validateValue(value);
        _attributes.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> V getAttribute(AttributeKey<V> key, V def)
    {
        return (V) _attributes.getOrDefault(key, def);
    }
}
