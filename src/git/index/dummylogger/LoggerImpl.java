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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LoggerImpl
{
    private final String _loggerName;

    private final Function<String, Void> _funcLogInfo;
    private final BiFunction<String, Throwable, Void> _funcLogInfoUndThrowable;
    private final Function<String, Void> _funcLogWarn;
    private final BiFunction<String, Throwable, Void> _funcLogWarnUndThrowable;
    private final Function<String, Void> _funcLogError;
    private final BiFunction<String, Throwable, Void> _funcLogErrorUndThrowable;

    public LoggerImpl(String loggerName)
    {
        _loggerName = loggerName;
        if (initSLF4JLogging())
        {
            Slf4jImpl impl = new Slf4jImpl(_loggerName);
            _funcLogInfo = impl.getFuncLogInfo();
            _funcLogInfoUndThrowable = impl.getFuncLogInfoUndThrowable();
            _funcLogWarn = impl.getFuncLogWarn();
            _funcLogWarnUndThrowable = impl.getFuncLogWarnUndThrowable();
            _funcLogError = impl.getFuncLogError();
            _funcLogErrorUndThrowable = impl.getFuncLogErrorUndThrowable();
        }
        else
        {
            _funcLogInfo = LoggerImpl::infoDummyImpl;
            _funcLogInfoUndThrowable = LoggerImpl::infoDummyImpl;
            _funcLogWarn = LoggerImpl::warnDummyImpl;
            _funcLogWarnUndThrowable = LoggerImpl::warnDummyImpl;
            _funcLogError = LoggerImpl::errorDummyImpl;
            _funcLogErrorUndThrowable = LoggerImpl::errorDummyImpl;
        }
    }

    public LoggerImpl(Class<?> loggerClass)
    {
        this(loggerClass.getSimpleName());
    }

    private boolean initSLF4JLogging()
    {
        try
        {
            Class.forName("org.slf4j.LoggerFactory");
            return true;
        }
        catch (ClassNotFoundException classNotFoundException)
        {
            return false;
        }
    }

    public Void info(String logMessage)
    {
        _funcLogInfo.apply(logMessage);
        return null;
    }

    public Void info(String logMessage, Throwable throwable)
    {
        _funcLogInfoUndThrowable.apply(logMessage, throwable);
        return null;
    }

    public Void warn(String logMessage)
    {
        _funcLogWarn.apply(logMessage);
        return null;
    }

    public Void warn(String logMessage, Throwable throwable)
    {
        _funcLogWarnUndThrowable.apply(logMessage, throwable);
        return null;
    }

    public Void error(String logMessage)
    {
        _funcLogError.apply(logMessage);
        return null;
    }

    public Void error(String logMessage, Throwable throwable)
    {
        _funcLogErrorUndThrowable.apply(logMessage, throwable);
        return null;
    }

    private static Void infoDummyImpl(String logMessage)
    {
        infoDummyImpl(logMessage, null);
        return null;
    }

    private static Void infoDummyImpl(String logMessage, Throwable throwable)
    {
        System.out.println("[" + new Date() + "] " + "[INFO]" + ": " + logMessage + ((throwable == null) ? "" : "\n" + writeThrowableIntoString(throwable)));
        return null;
    }

    private static Void warnDummyImpl(String logMessage)
    {
        warnDummyImpl(logMessage, null);
        return null;
    }

    private static Void warnDummyImpl(String logMessage, Throwable throwable)
    {
        System.out.println("[" + new Date() + "] " + "[WARN]" + ": " + logMessage + ((throwable == null) ? "" : "\n" + writeThrowableIntoString(throwable)));
        return null;
    }

    private static Void errorDummyImpl(String logMessage)
    {
        errorDummyImpl(logMessage, null);
        return null;
    }

    private static Void errorDummyImpl(String logMessage, Throwable throwable)
    {
        System.err.println("[" + new Date() + "] " + "[ERROR]" + ": " + logMessage + ((throwable == null) ? "" : "\n" + writeThrowableIntoString(throwable)));
        return null;
    }

    private static String writeThrowableIntoString(Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
