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

import git.index.dummylogger.LoggerImpl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractConfigHolder<H>
{
    /**
     * Obtaining a current path of running program
     * Получаем текущий путь запущенной програмы
     */
    private final static File WORKING_PATH;
    static
    {
//        File currentLocation = null;
//        String propertyOsName = System.getProperty("os.name");
//        if ((propertyOsName != null) && (propertyOsName.toLowerCase(Locale.ROOT).startsWith("mac")))
//        {
//            try
//            {
//                currentLocation = new File(AbstractConfigHolder.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//            }
//            catch (URISyntaxException ignored)
//            {
//            }
//        }
//        if (currentLocation == null)
//        {
//            currentLocation = new File(System.getProperty("user.dir"));
//        }
//        WORKING_PATH = currentLocation;
        WORKING_PATH = new File(System.getProperty("user.dir"));
    }

    protected final LoggerImpl _logger;

    private final H _configInstance;
    private final ConfigFieldParser _configParser;

    protected AbstractConfigHolder(boolean parseFieldOneByOne)
    {
        _logger = new LoggerImpl(this.getClass());
        _configInstance = createANewInstance();
        _configParser = new ConfigFieldParser(getInstanceOfConfig(), parseFieldOneByOne);
    }

    protected AbstractConfigHolder()
    {
        this(true);
    }

    public void load()
    {
        _logger.info("Start loading config " + ("[" + "'" + getAttachedConfig().getSimpleName() + "'" + "]") + "...");
        ConfigParser configParser = new ConfigParser(new File(WORKING_PATH, getConfigPath()), getCharacterSet());
        configParser.load();
        _configParser.setConfigParser(configParser);
        _configParser.load();
        _configParser.setConfigParser(null);
        _logger.info("Loaded config " + ("[" + "'" + getAttachedConfig().getSimpleName() + "'" + "]") + ". " + "Loaded " + ("[" + _configParser.getCountOfSuccessLoadedFields() + "]") + " of " + ("[" + _configParser.getLoadedFieldsCount() + "]") + " fields.");
    }

    private H createANewInstance()
    {
        H configInstance = null;
        try
        {
            configInstance = getAttachedConfig().getConstructor().newInstance();
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | InstantiationException | ExceptionInInitializerError e)
        {
            _logger.error(
                    "While creating a new instance - code throws error. " +
                            "Code task: '" + "create new instance for bumping inner methods for config" + "'. " +
                            "Config class '" + getAttachedConfig().getSimpleName() + "';",
                    e
            );
        }
        if (configInstance == null)
        {
            RuntimeException exception = new RuntimeException("Error while creating new instance of config.");
            _logger.error(
                    "While creating a new instance - code throws error. " +
                            "Code task: '" + "config instance is null" + "'. " +
                            "Config class '" + getAttachedConfig().getSimpleName() + "';",
                    exception
            );
            throw exception;
        }
        return configInstance;
    }

    public abstract String getConfigPath();

    public abstract Class<H> getAttachedConfig();

    public Charset getCharacterSet()
    {
        return StandardCharsets.UTF_8;
    }

    public H getInstanceOfConfig()
    {
        return _configInstance;
    }
}
