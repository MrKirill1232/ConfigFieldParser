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
package git.index.fieldparser.model.fieldparser;

import git.index.configparser.annotations.ImmutableVariable;
import git.index.dummylogger.LoggerImpl;
import git.index.fieldparser.FieldParserManager;
import git.index.fieldparser.annotations.FieldParser;
import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFieldParser
{
    protected final LoggerImpl _logger;

    protected final Object _instanceOfFieldParser;

    protected final Map<String, FieldHolder> _fieldMap;
    protected final Map<String, Method> _methodMap;

    protected AbstractFieldParser(Object instanceOfFieldParser)
    {
        _logger = new LoggerImpl(this.getClass());

        _instanceOfFieldParser = instanceOfFieldParser;
        _fieldMap = generateFieldList();
        _methodMap = generateMethodList();
    }

    protected Map<String, FieldHolder> generateFieldList()
    {
        Field[] fieldsArray = _instanceOfFieldParser.getClass().getDeclaredFields();
        Map<String, FieldHolder> fieldMap = new HashMap<>(fieldsArray.length);
        for (Field field : fieldsArray)
        {
            if (
                    // cannot change final field
                    (Modifier.isFinal(field.getModifiers())) ||
                    // cannot change non-visible fields
                    (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())))
            {
                continue;
            }
            FieldHolder fieldHolder = new FieldHolder(field);
            fieldMap.put(field.getName(), fieldHolder);
        }
        return fieldMap;
    }

    protected Map<String, Method> generateMethodList()
    {
        Method[] methodArray = _instanceOfFieldParser.getClass().getMethods();
        Map<String, Method> methodMap = new HashMap<>(methodArray.length);
        for (Method method : methodArray)
        {
            if (
                    // cannot change non-visible fields
                    (Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(method.getModifiers()))
            )
            {
                continue;
            }
            methodMap.put(method.getName(), method);
        }
        return methodMap;
    }

    protected boolean parseValue(String fieldName)
    {
        FieldHolder fieldHolder = _fieldMap.getOrDefault(fieldName, null);
        if (fieldHolder == null)
        {
            return false;
        }
        if (fieldHolder.isImmutableVariable() && fieldHolder.isParsedOnce())
        {
            return false;
        }
        boolean success;
        if (isMethodCallAssign(fieldHolder.getField()))
        {
            success = setValueByMethod(fieldHolder);
        }
        else
        {
            success = setValueIntoField(fieldHolder);
        }
        fieldHolder.setParsedOnce(success);
        return success;
    }

    protected boolean setValueByMethod(FieldHolder fieldHolder)
    {
        String parseFieldMethodName = getMethodNameForAssignationField(fieldHolder.getField());
        Method method = _methodMap.getOrDefault(parseFieldMethodName, null);
        if (method == null)
        {
            return false;
        }
        String rawStringFieldValue = getRawFieldValue(fieldHolder.getField().getName());
        Object[] arguments;
        if (method.getParameterCount() == 0)
        {
            arguments = new Object[0];
        }
        else if (method.getParameterCount() == 1)
        {
            arguments = new Object[] { rawStringFieldValue };
        }
        else
        {
            _logger.error("Cannot use a method " + ("[" + parseFieldMethodName + "]") + ". Reason - cannot handle more than 1 arguments in method invocation.");
            return false;
        }
        boolean originalAccessing = method.isAccessible();
        try
        {
            method.setAccessible(true);
            method.invoke(_instanceOfFieldParser, arguments);
            return true;
        }
        catch (Exception e)
        {
            _logger.error("Cannot invoke method " + ("[" + parseFieldMethodName + "]") + ". Reason - ", e);
            return false;
        }
        finally
        {
            method.setAccessible(originalAccessing);
        }
    }

    protected boolean setValueIntoField(FieldHolder fieldHolder)
    {
        FieldClassRef<Object> fieldClassRef = overrideClassRef(fieldHolder.getField(), fieldHolder.getFieldValue());
        IFieldParser<?> fieldParser = FieldParserManager.getInstance().applyParserFromClass(fieldClassRef.getRawClass());
        if (fieldParser == null)
        {
            _logger.error("Cannot parse a field " + ("[" + fieldHolder.getField().getName() + "]") + " because parser of class " + ("[" + fieldClassRef.getRawClass().getSimpleName() + "]") + " is not supported!");
            return false;
        }
        String rawStringFieldValue = getRawFieldValue(fieldHolder.getField().getName());
        Object defaultValue = fieldHolder.getFieldValue();
        Object parsedValue = overrideParsedValue(fieldHolder.getField(), fieldParser.parseValue(rawStringFieldValue, fieldClassRef, defaultValue), fieldClassRef.getRawClass(), fieldClassRef.getActualClassArguments());
        if ((fieldHolder.getField().getType().isPrimitive()) && (parsedValue == null))
        {
            _logger.error("Cannot parse a field " + ("[" + fieldHolder.getField().getName() + "]") + " because 'FieldParser' by " + (fieldParser.getClass().getSimpleName()) + " drop a 'null' value, when field is a primitive value. Using a default value - " + ("[" + defaultValue + "]") + ".");
            return false;
        }
        boolean originalAccessing = fieldHolder.getField().isAccessible();
        try
        {
            fieldHolder.getField().setAccessible(true);
            fieldHolder.getField().set(_instanceOfFieldParser, parsedValue);
            return true;
        }
        catch (Exception e)
        {
            _logger.error("Cannot set value " + ("[" + String.valueOf(parsedValue) + "]") + " for field " + ("[" + fieldHolder.getField().getName() + "]") + ". Using a default value - " + ("[" + defaultValue + "]") + ". Reason - ", e);
            return false;
        }
        finally
        {
            fieldHolder.getField().setAccessible(originalAccessing);
        }
    }

    /**
     * simple check for understand which method is required to use for set variable:
     * <ul>
     * <li>call <code>setValueAsMethod()</code></li>
     * <li>call <code>setValueAsVariable()</code></li>
     * </ul>
     * простая проверка для понимания какой именно метод использовать для присоения переменной:
     * <ul>
     * <li>вызов <code>setValueAsMethod()</code></li>
     * <li>вызов <code>setValueAsVariable()</code></li>
     * </ul>
     * @return <code>true</code> - required a method call; <code>false</code> - required set as variable;
     */
    protected boolean isMethodCallAssign(Field field)
    {
        return !getMethodNameForAssignationField(field).isEmpty();
    }

    protected String getMethodNameForAssignationField(Field field)
    {
        if (field == null)
        {
            return "";
        }
        FieldParser fieldAnnotation = field.getAnnotation(FieldParser.class);
        if (fieldAnnotation == null)
        {
            return "";
        }
        return fieldAnnotation.parseFieldMethod();
    }

    protected FieldClassRef<Object> overrideClassRef(Field field, Object defaultFieldValue)
    {
        if (field == null)
        {
            return new FieldClassRef<Object>(Object.class);
        }
        Class<?>[] genericsTypeArray;
        FieldParser fieldAnnotation = field.getAnnotation(FieldParser.class);
        if (fieldAnnotation == null)
        {
            genericsTypeArray = new Class[0];
        }
        else
        {
            genericsTypeArray = fieldAnnotation.genericClasses();
        }
        return new FieldClassRef<>(field.getType(), genericsTypeArray);
    }

    protected Object overrideParsedValue(Field field, Object inputParsedObject, Class<?> mainClass, Class<?>... genericTypeArray)
    {
        return inputParsedObject;
    }

    protected abstract String getRawFieldValue(String proceedFieldName);

    public Object getInstanceOfFieldParser()
    {
        return _instanceOfFieldParser;
    }

    protected final static class FieldHolder
    {
        private enum FieldMaskType
        {
            IMMUTABLE,
            PARSED_ONCE,
        }

        private final Field _field;
        private byte _mask;

        private Object _fieldValue;

        private FieldHolder(Field field)
        {
            _field = field;
            try
            {
                _fieldValue = field.get(Object.class);
            }
            catch (IllegalAccessException e)
            {
                _fieldValue = null;
            }
            if (_field.getAnnotation(ImmutableVariable.class) != null)
            {
                _mask = (byte) (_mask | (1 << FieldHolder.FieldMaskType.IMMUTABLE.ordinal()));
            }
        }

        public Field getField()
        {
            return _field;
        }

        public Object getFieldValue()
        {
            return _fieldValue;
        }

        public boolean isImmutableVariable()
        {
            return (_mask & (1 << FieldHolder.FieldMaskType.IMMUTABLE.ordinal())) != 0;
        }

        public boolean isParsedOnce()
        {
            return (_mask & (1 << FieldHolder.FieldMaskType.PARSED_ONCE.ordinal())) != 0;
        }

        public void setParsedOnce(boolean parsedOnce)
        {
            if (parsedOnce == isParsedOnce())
            {
                return;
            }
            if (parsedOnce)
            {
                _mask = (byte) (_mask | (1 << FieldHolder.FieldMaskType.PARSED_ONCE.ordinal()));
            }
            else
            {
                _mask = (byte) (_mask & ~(1 << FieldHolder.FieldMaskType.PARSED_ONCE.ordinal()));
            }
        }
    }
}
