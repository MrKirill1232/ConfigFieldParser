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
package git.index.fieldparser.model.attributes;

import java.util.Objects;

public final class AttributeKey<V>
{
    private final String _name;
    private final Class<V> _type;

    private AttributeKey(String name, Class<V> type)
    {
        _name = Objects.requireNonNull(name, "name");
        _type = Objects.requireNonNull(type, "type");
    }

    public static <V> AttributeKey<V> of(String name, Class<V> type)
    {
        return new AttributeKey<>(name, type);
    }

    public String getName()
    {
        return _name;
    }

    public Class<V> getType()
    {
        return _type;
    }

    public void validateValue(Object value)
    {
        if (value != null && !getType().isInstance(value))
        {
            throw new IllegalArgumentException("Attr '" + getName() + "' expects " + getType().getName() + " but got " + value.getClass().getName());
        }
    }

    @Override
    public String toString()
    {
        return "AttributeKey(" + getName() + ":" + getType().getSimpleName() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!this.getClass().isInstance(obj))
        {
            return false;
        }
        AttributeKey<?> compareObject = ((AttributeKey<?>) obj);
        return getName().equals(compareObject.getName()) && getType().equals(compareObject.getType());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getName(), getType());
    }
}