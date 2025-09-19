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
package git.index.dummylogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Slf4jImpl
{
    private final Logger _logger;

    public Slf4jImpl(String loggerName)
    {
        _logger = LoggerFactory.getLogger(loggerName);
    }

    public Function<String, Void> getFuncLogInfo()
    {
        return (String) -> { _logger.info(String); return null; };
    }

    public BiFunction<String, Throwable, Void> getFuncLogInfoUndThrowable()
    {
        return (String, throwable) -> { _logger.info(String, throwable); return null; };
    }

    public Function<String, Void> getFuncLogWarn()
    {
        return (String) -> { _logger.warn(String); return null; };
    }

    public BiFunction<String, Throwable, Void> getFuncLogWarnUndThrowable()
    {
        return (String, throwable) -> { _logger.warn(String, throwable); return null; };
    }

    public Function<String, Void> getFuncLogError()
    {
        return (String) -> { _logger.error(String); return null; };
    }

    public BiFunction<String, Throwable, Void> getFuncLogErrorUndThrowable()
    {
        return (String, throwable) -> { _logger.error(String, throwable); return null; };
    }
}
