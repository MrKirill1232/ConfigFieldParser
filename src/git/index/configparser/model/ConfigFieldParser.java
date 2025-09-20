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
package git.index.configparser.model;

import git.index.configparser.annotations.ConfigParameterVariable;
import git.index.fieldparser.annotations.FieldParser;
import git.index.fieldparser.model.FieldClassRef;
import git.index.fieldparser.model.attributes.FieldAttributes;
import git.index.fieldparser.model.fieldparser.AbstractFieldParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigFieldParser extends AbstractFieldParser
{
    private Map<String, ConfigFieldHolder> _configValues;

    private ConfigParser _configParser;

    private final boolean _linked;

    private int _lastFieldCollectionCount;
    private int _lastSuccessParsedFields;

    public ConfigFieldParser(Object configInstance, boolean accessIntoPrivate, boolean linked)
    {
        super(configInstance, accessIntoPrivate);
        _linked = linked;
    }

    public void setConfigParser(ConfigParser configParser)
    {
        _configParser = configParser;
    }

    public void load()
    {
        if (_configParser == null)
        {
            return;
        }
        if (_linked)
        {
            _configValues = new LinkedHashMap<>();
        }
        else
        {
            _configValues = new HashMap<>();
        }
        for (FieldHolder fieldHolder : _fieldMap.values())
        {
            Field field = fieldHolder.getField();
            if (field == null)
            {   // ;d
                continue;
            }
            ConfigParameterVariable configParameterVariable = field.getAnnotation(ConfigParameterVariable.class);
            if ((configParameterVariable == null) || (configParameterVariable.ignoredParameter()))
            {
                continue;
            }
            ConfigFieldHolder configFieldHolder = parseFieldHolder(fieldHolder, configParameterVariable);
            if (configParameterVariable.notPresentedInConfig() && ((configFieldHolder.getSetParameterMethod() == null) || (configFieldHolder.getSetParameterMethod().isEmpty())))
            {
                _logger.warn("Field " + ("[" + configFieldHolder.getFieldName() + "]") + " marked as 'not presented in config', but it also do not have a declared 'set parameter method'. Use a 'ignoredParameter' if this not a mistake!");
                continue;
            }
            _configValues.put(configFieldHolder.getFieldName(), configFieldHolder);
        }
        tryToBumpOnStartLoadMethod();
        int counter = 0;
        for (ConfigFieldHolder configFieldHolder : _configValues.values())
        {
            if (!parseValue(configFieldHolder.getFieldName()))
            {
                continue;
            }
            counter += 1;
        }
        tryToBumpOnEndLoadMethod();
        _lastFieldCollectionCount = _configValues.size();
        _lastSuccessParsedFields = counter;
        _configValues.clear();
        _configValues = null;
    }

    private ConfigFieldHolder parseFieldHolder(FieldHolder fieldHolder, ConfigParameterVariable configParameterVariable)
    {
        ConfigFieldHolder configFieldHolder = new ConfigFieldHolder(fieldHolder.getField().getName());
        // parameter name in config file
        if (configParameterVariable.parameterName().isEmpty())
        {
            configFieldHolder.setConfigFieldName(configFieldHolder.getFieldName());
        }
        else
        {
            configFieldHolder.setConfigFieldName(configParameterVariable.parameterName());
        }
        if (!configParameterVariable.defaultValue().isEmpty())
        {
            configFieldHolder.setConfigFieldDefaultValue(configParameterVariable.defaultValue());
        }
        if (!configParameterVariable.commentValue().isEmpty())
        {
            configFieldHolder.setConfigFieldDefaultComment(configParameterVariable.commentValue());
        }

        FieldParser fieldParserAnnotation = configParameterVariable.fieldParser();
        if (fieldParserAnnotation != null)
        {
            if (fieldParserAnnotation.parseFieldMethod().isEmpty())
            {
                configFieldHolder.setSetParameterMethod("");
            }
            else
            {
                configFieldHolder.setSetParameterMethod(fieldParserAnnotation.parseFieldMethod());
            }
            if (fieldParserAnnotation.classType() == Object.class)
            {
                configFieldHolder.setFieldClass(fieldHolder.getField().getType());
            }
            else
            {
                configFieldHolder.setFieldClass(fieldParserAnnotation.classType());
            }
            configFieldHolder.setGenericTypeClasses(configParameterVariable.fieldParser().genericClasses());
        }
        else
        {
            configFieldHolder.setSetParameterMethod("");
            configFieldHolder.setFieldClass(fieldHolder.getField().getType());
            configFieldHolder.setGenericTypeClasses(new Class[0]);
        }

        if (!configParameterVariable.spliterator01().isEmpty())
        {
            configFieldHolder.setSpliterator01(configParameterVariable.spliterator01());
        }
        if (!configParameterVariable.spliterator02().isEmpty())
        {
            configFieldHolder.setSpliterator02(configParameterVariable.spliterator02());
        }

        configFieldHolder.setConfigFieldValue(_configParser.getValueByKey(configFieldHolder.getConfigFieldName(), configFieldHolder.getConfigFieldDefaultValue()));

        return configFieldHolder;
    }

    @Override
    protected List<Object> overrideMethodArguments(FieldHolder fieldHolder, MethodHolder methodHolder)
    {
        ConfigFieldHolder configFieldHolder = _configValues.getOrDefault(fieldHolder.getField().getName(), null);
        if (configFieldHolder == null)
        {
            _logger.error("Cannot use a method " + ("[" + getMethodNameForAssignationField(fieldHolder.getField()) + "]") + ". Reason - cannot find field, which call a method assignation.");
            return null;
        }
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
            arguments.add(configFieldHolder.getConfigFieldValue());
        }
        else if (methodHolder.getMethod().getParameterCount() == 2)
        {
            arguments.add(configFieldHolder.getConfigFieldName());
            arguments.add(configFieldHolder.getConfigFieldValue());
        }
        else if (methodHolder.getMethod().getParameterCount() == 3)
        {
            arguments.add(configFieldHolder.getConfigFieldName());
            arguments.add(configFieldHolder.getConfigFieldValue());
            arguments.add(_configParser);
        }
        else
        {
            _logger.error("Cannot use a method " + ("[" + getMethodNameForAssignationField(fieldHolder.getField()) + "]") + ". Reason - cannot handle more than 1 arguments in method invocation.");
            return null;
        }
        return arguments;
    }

    @Override
    protected boolean isMethodCallAssign(Field field)
    {
        if (field == null)
        {
            return false;
        }
        ConfigFieldHolder configFieldHolder = _configValues.getOrDefault(field.getName(), null);
        if (configFieldHolder == null)
        {
            return false;
        }
        return !configFieldHolder.getSetParameterMethod().isEmpty();
    }

    @Override
    protected String getMethodNameForAssignationField(Field field)
    {
        if (field == null)
        {
            return "";
        }
        ConfigFieldHolder configFieldHolder = _configValues.getOrDefault(field.getName(), null);
        if (configFieldHolder == null)
        {
            return "";
        }
        return configFieldHolder.getSetParameterMethod();
    }

    @Override
    protected FieldClassRef<Object> overrideClassRef(Field field, Object defaultFieldValue)
    {
        if (field == null)
        {
            return super.overrideClassRef(field, defaultFieldValue);
        }
        ConfigFieldHolder configFieldHolder = _configValues.getOrDefault(field.getName(), null);
        if (configFieldHolder == null)
        {
            return super.overrideClassRef(field, defaultFieldValue);
        }
        FieldClassRef<Object> fieldClassRef = new FieldClassRef<>(configFieldHolder.getFieldClass(), configFieldHolder.getGenericTypeClasses());
        if (configFieldHolder.getSpliterator01() != null)
        {
            fieldClassRef.withAttribute(FieldAttributes.SPLIT_PATTERN_01, Pattern.compile(configFieldHolder.getSpliterator01()));
        }
        if (configFieldHolder.getSpliterator02() != null)
        {
            fieldClassRef.withAttribute(FieldAttributes.SPLIT_PATTERN_02, Pattern.compile(configFieldHolder.getSpliterator02()));
        }
        return fieldClassRef;
    }

    @Override
    protected String getRawFieldValue(String proceedFieldName)
    {
        ConfigFieldHolder configFieldHolder = _configValues.getOrDefault(proceedFieldName, null);
        if (configFieldHolder == null)
        {
            return null;
        }
        return configFieldHolder.getConfigFieldValue();
    }

    protected void tryToBumpOnStartLoadMethod()
    {
        MethodHolder onStartLoadMethod = _methodMap.getOrDefault("onStartLoad", null);
        if (onStartLoadMethod == null)
        {
            return;
        }
        try
        {
            onStartLoadMethod.getMethodHandle().invokeWithArguments(Collections.emptyList());
        }
        catch (Throwable t)
        {
            _logger.error("Cannot invoke 'onStartLoad' method.", t);
        }
    }

    protected void tryToBumpOnEndLoadMethod()
    {
        MethodHolder onEndLoadMethod = _methodMap.getOrDefault("onEndLoad", null);
        if (onEndLoadMethod == null)
        {
            return;
        }
        try
        {
            onEndLoadMethod.getMethodHandle().invokeWithArguments(Collections.emptyList());
        }
        catch (Throwable t)
        {
            _logger.error("Cannot invoke 'onEndLoad' method.", t);
        }
    }

    public int getLoadedFieldsCount()
    {
        return _lastFieldCollectionCount;
    }

    public int getCountOfSuccessLoadedFields()
    {
        return _lastSuccessParsedFields;
    }

    private static class ConfigFieldHolder
    {   // will destroyed after parsing I guess
        private final String _fieldName;
        private String _configFieldName;
        private String _configFieldValue;
        private String _configFieldDefaultValue;
        private String _configFieldDefaultComment;
        private String _setParameterMethod;

        private String _spliterator01;
        private String _spliterator02;

        private Class<?> _fieldClass;
        private Class<?>[] _genericTypeClasses;

        private ConfigFieldHolder(String fieldName)
        {
            _fieldName = fieldName;
        }

        public String getFieldName()
        {
            return _fieldName;
        }

        public String getConfigFieldName()
        {
            return _configFieldName;
        }

        public void setConfigFieldName(String configFieldName)
        {
            _configFieldName = configFieldName;
        }

        public String getConfigFieldValue()
        {
            return _configFieldValue;
        }

        public void setConfigFieldValue(String configFieldValue)
        {
            _configFieldValue = configFieldValue;
        }

        public String getConfigFieldDefaultValue()
        {
            return _configFieldDefaultValue;
        }

        public void setConfigFieldDefaultValue(String configFieldDefaultValue)
        {
            _configFieldDefaultValue = configFieldDefaultValue;
        }

        public String getConfigFieldDefaultComment()
        {
            return _configFieldDefaultComment;
        }

        public void setConfigFieldDefaultComment(String configFieldDefaultComment)
        {
            _configFieldDefaultComment = configFieldDefaultComment;
        }

        public String getSetParameterMethod()
        {
            return _setParameterMethod;
        }

        public void setSetParameterMethod(String setParameterMethod)
        {
            _setParameterMethod = setParameterMethod;
        }

        public String getSpliterator01()
        {
            return _spliterator01;
        }

        public void setSpliterator01(String spliterator01)
        {
            _spliterator01 = spliterator01;
        }

        public String getSpliterator02()
        {
            return _spliterator02;
        }

        public void setSpliterator02(String spliterator02)
        {
            _spliterator02 = spliterator02;
        }

        public Class<?> getFieldClass()
        {
            return _fieldClass;
        }

        public void setFieldClass(Class<?> fieldClass)
        {
            _fieldClass = fieldClass;
        }

        public Class<?>[] getGenericTypeClasses()
        {
            return _genericTypeClasses;
        }

        public void setGenericTypeClasses(Class<?>[] genericTypeClasses)
        {
            _genericTypeClasses = genericTypeClasses;
        }
    }
}
