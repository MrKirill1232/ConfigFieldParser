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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionParser implements IFieldParser<Collection>
{
    private final LoggerImpl _logger;

    private final ConcurrentHashMap<Class<?>, Optional<Constructor<?>>> _constructors;

    private CollectionParser()
    {
        // singleton class
        _logger = new LoggerImpl(this.getClass());
        _constructors = new ConcurrentHashMap<>();
    }

    @Override
    public Class<Collection> getParsableClass()
    {
        return Collection.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G parseValue(String value, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (value == null)
        {   // nvm, we should not parse this
            return defaultValue;
        }
        if ((fieldClassRef == null) || (!getParsableClass().isAssignableFrom(fieldClassRef.getRawClass())))
        {
            return defaultValue;
        }
        Class<?> collectionClass = overrideCollectionClass(fieldClassRef.getRawClass());
        Class<?> genericClass;
        if (fieldClassRef.getActualClassArguments().length == 0)
        {
            genericClass = String.class;
            _logger.warn("Cannot find a generic type of collection " + ("[" + collectionClass.getSimpleName() + "]") + " . Using a " + ("[" + genericClass.getSimpleName() + "]") + " as generic type of collection.");
        }
        else
        {
            genericClass = fieldClassRef.getActualClassArguments()[0];
        }
        IFieldParser<?> fieldParser = FieldParserManager.getInstance().applyParserFromClass(Array[].class);
        if (fieldParser == null)
        {
            _logger.warn("Cannot find parser for 'single array' object.");
            return defaultValue;
        }
        FieldClassRef<Object[]> objectArrayFieldClassRef = new FieldClassRef<Object[]>(genericClass).withAttribute(FieldAttributes.SPLIT_PATTERN_01, fieldClassRef.getAttribute(FieldAttributes.SPLIT_PATTERN_01, null));
        Object[] objectArray = fieldParser.parseValue(value, objectArrayFieldClassRef, null);
        try
        {
            Constructor<?> constructor = findAConstructor(collectionClass);
            if (constructor == null)
            {
                throw new NoSuchMethodException("Cannot create a 'Collection' with class " + ("[" + collectionClass.getSimpleName() + "]") + ", because class do not have a 'zero argument constructor'.");
            }
            Collection<Object> collection = createCollection(collectionClass, constructor);
            if ((objectArray == null) || (objectArray.length == 0))
            {

            }
            else
            {
                Collections.addAll(collection, objectArray);
            }
            return ((G) collection);
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception)
        {
            _logger.error("Cannot create a collection of " + ("[" + collectionClass.getSimpleName() + "]") + ".", exception);
            return defaultValue;
        }
        catch (Exception e)
        {
            _logger.error("Error while creating / adding elements inside collection.", e);
            return defaultValue;
        }
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

    private Class<?> overrideCollectionClass(Class<?> requiredCollection)
    {
        if (requiredCollection == Collection.class)
        {
            requiredCollection = ArrayList.class;
            _logger.warn("Cannot create a new instance of array from 'Collection' class. Override it to " + ("[" + requiredCollection.getSimpleName() + "]") + ".");
        }
        else if (requiredCollection == List.class)
        {
            requiredCollection = ArrayList.class;
            _logger.warn("Cannot create a new instance of array from 'List' class. Override it to " + ("[" + requiredCollection.getSimpleName() + "]") + ".");
        }
        else if (requiredCollection == Set.class)
        {
            requiredCollection = HashSet.class;
            _logger.warn("Cannot create a new instance of array from 'Set' class. Override it to " + ("[" + requiredCollection.getSimpleName() + "]") + ".");
        }
        else if (requiredCollection == Queue.class)
        {
            requiredCollection = ArrayDeque.class;
            _logger.warn("Cannot create a new instance of array from 'Queue' class. Override it to " + ("[" + requiredCollection.getSimpleName() + "]") + ".");
        }
        return requiredCollection;
    }

    private Constructor<?> findAConstructor(Class<?> requiredCollection)
    {
        Optional<Constructor<?>> optionalConstructor = _constructors.getOrDefault(requiredCollection, null);
        if (optionalConstructor != null)
        {
            if (optionalConstructor.isPresent())
            {
                return optionalConstructor.get();
            }
            else
            {
                return null;
            }
        }
        Constructor<?> constructor = null;
        try
        {
            constructor = requiredCollection.getConstructor();
            constructor.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            _logger.error("Cannot find a zero arguments constructor for class " + ("[" + requiredCollection + "]") + ".", e);
        }
        finally
        {
            _constructors.put(requiredCollection, Optional.ofNullable(constructor));
        }
        return constructor;
    }

    @SuppressWarnings("unchecked")
    private static Collection<? super Object> createCollection(Class<?> requiredCollection, Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException
    {
        return (Collection<? super Object>) constructor.newInstance();
    }

    private final static CollectionParser INSTANCE = new CollectionParser();

    public static CollectionParser getInstance()
    {
        return INSTANCE;
    }
}
