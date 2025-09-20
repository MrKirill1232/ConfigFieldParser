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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFieldParser
{
    protected final LoggerImpl _logger;

    protected final Object _instanceOfFieldParser;
    protected final boolean _accessIntoPrivate;

    protected final Map<String, FieldHolder> _fieldMap;
    protected final Map<String, MethodHolder> _methodMap;

    protected AbstractFieldParser(Object instanceOfFieldParser, boolean accessIntoPrivate)
    {
        _logger = new LoggerImpl(this.getClass());

        _instanceOfFieldParser = instanceOfFieldParser;
        _accessIntoPrivate = accessIntoPrivate;

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
                    (Modifier.isFinal(field.getModifiers()))
            )
            {
                continue;
            }
            if ((!_accessIntoPrivate) &&
                    // cannot change non-visible fields
                    (Modifier.isPrivate(field.getModifiers()) || Modifier.isProtected(field.getModifiers())))
            {
                continue;
            }
            try
            {
                MethodHandles.Lookup lookupFieldVar = MethodHandles.lookup();
                MethodHandles.Lookup privateLookupFieldVar = MethodHandles.privateLookupIn(_instanceOfFieldParser.getClass(), lookupFieldVar);
                VarHandle fieldVarHandle = privateLookupFieldVar.unreflectVarHandle(field);

                Object defaultValue;
                try
                {
                    if (Modifier.isStatic(field.getModifiers()))
                    {
                        defaultValue = fieldVarHandle.get();
                    }
                    else
                    {
                        defaultValue = fieldVarHandle.get(_instanceOfFieldParser);
                    }
                }
                catch (Throwable t)
                {
                    defaultValue = null;
                }

                FieldHolder fieldHolder = new FieldHolder(field, fieldVarHandle, defaultValue);
                fieldMap.put(field.getName(), fieldHolder);
            }
            catch (Exception e)
            {
                _logger.error("Cannot get access to field " + ("[" + field.getName() + "]") + ". Is accessing into private fields - " + ("[" + (_accessIntoPrivate ? "YES" : "NO") + "]") + ".", e);
            }
        }
        return fieldMap;
    }

    protected Map<String, MethodHolder> generateMethodList()
    {
        Method[] methodArray = _instanceOfFieldParser.getClass().getDeclaredMethods();
        Map<String, MethodHolder> methodMap = new HashMap<>(methodArray.length);
        for (Method method : methodArray)
        {
            if ((!_accessIntoPrivate) &&
                    // cannot invoke non-visible methods
                    (Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(method.getModifiers())))
            {
                continue;
            }
            try
            {
                MethodHandles.Lookup lookupFieldVar = MethodHandles.lookup();
                MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(_instanceOfFieldParser.getClass(), lookupFieldVar);
                MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());

                MethodHandle methodHandle;

                if (Modifier.isStatic(method.getModifiers()))
                {
                    methodHandle = privateLookup.findStatic(_instanceOfFieldParser.getClass(), method.getName(), methodType);
                }
                else
                {
                    methodHandle = privateLookup.findVirtual(_instanceOfFieldParser.getClass(), method.getName(), methodType);
                    methodHandle = methodHandle.bindTo(_instanceOfFieldParser);
                }
                MethodHolder methodHolder = new MethodHolder(method, methodHandle);
                methodMap.put(method.getName(), methodHolder);
            }
            catch (Exception e)
            {
                _logger.error("Cannot get access to method " + ("[" + method.getName() + "]") + ". Is accessing into private fields - " + ("[" + (_accessIntoPrivate ? "YES" : "NO") + "]") + ".", e);
            }
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
        MethodHolder methodHolder = _methodMap.getOrDefault(parseFieldMethodName, null);
        if (methodHolder == null)
        {
            return false;
        }
        List<Object> arguments = overrideMethodArguments(fieldHolder, methodHolder);
        if (arguments == null)
        {
            return false;
        }
        try
        {
            methodHolder.getMethodHandle().invokeWithArguments(arguments);
        }
        catch (Throwable t)
        {
            _logger.error("Cannot invoke method " + ("[" + parseFieldMethodName + "]") + ". Reason - ", t);
            return false;
        }
        return true;
    }

    protected List<Object> overrideMethodArguments(FieldHolder fieldHolder, MethodHolder methodHolder)
    {
        if ((methodHolder == null) || (methodHolder.getMethod() == null))
        {
            _logger.error("Cannot use a method " + ("[" + getMethodNameForAssignationField(fieldHolder.getField()) + "]") + ". Reason - cannot find method, which declared in field.");
            return null;
        }
        List<Object> arguments = new ArrayList<>(methodHolder.getMethod().getParameterCount());
        if (methodHolder.getMethod().getParameterCount() == 0)
        {
            arguments = Collections.emptyList();
        }
        else if (methodHolder.getMethod().getParameterCount() == 1)
        {
            String rawStringFieldValue = getRawFieldValue(fieldHolder.getField().getName());
            arguments.add(rawStringFieldValue);
        }
        else
        {
            _logger.error("Cannot use a method " + ("[" + getMethodNameForAssignationField(fieldHolder.getField()) + "]") + ". Reason - cannot handle more than 1 arguments in method invocation.");
            return null;
        }
        return arguments;
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
        try
        {
            if (Modifier.isStatic(fieldHolder.getField().getModifiers()))
            {
                fieldHolder.getFieldVarHandle().set(parsedValue);
            }
            else
            {
                fieldHolder.getFieldVarHandle().set(_instanceOfFieldParser, parsedValue);
            }
            return true;
        }
        catch (Exception e)
        {
            _logger.error("Cannot set value " + ("[" + String.valueOf(parsedValue) + "]") + " for field " + ("[" + fieldHolder.getField().getName() + "]") + ". Using a default value - " + ("[" + defaultValue + "]") + ". Reason - ", e);
            return false;
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
        private final VarHandle _fieldVarHandle;
        private byte _mask;

        private final Object _fieldValue;

        private FieldHolder(Field field, VarHandle fieldVarHandle, Object defaultValue)
        {
            _field = field;
            _fieldVarHandle = fieldVarHandle;
            _fieldValue = defaultValue;
            if (_field.getAnnotation(ImmutableVariable.class) != null)
            {
                _mask = (byte) (_mask | (1 << FieldHolder.FieldMaskType.IMMUTABLE.ordinal()));
            }
        }

        public Field getField()
        {
            return _field;
        }

        public VarHandle getFieldVarHandle()
        {
            return _fieldVarHandle;
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

    protected final static class MethodHolder
    {
        private final Method _method;
        private final MethodHandle _methodHandle;

        private MethodHolder(Method method, MethodHandle methodHandle)
        {
            _method = method;
            _methodHandle = methodHandle;
        }

        public Method getMethod()
        {
            return _method;
        }

        public MethodHandle getMethodHandle()
        {
            return _methodHandle;
        }
    }
}
