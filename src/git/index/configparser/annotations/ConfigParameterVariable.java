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
package git.index.configparser.annotations;

import git.index.fieldparser.annotations.FieldParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigParameterVariable
{
    /**
     * Key value from config
     * @return config value of annotated variable
     */
    String parameterName() default "";

    /**
     *
     * @return will not parse from config
     */
    boolean ignoredParameter() default false;

    /**
     * this value mean the field should be assigned only by method
     * @return if value presented in config file
     */
    boolean notPresentedInConfig() default false;

    String spliterator01() default "";

    String spliterator02() default "";

    String defaultValue() default "";

    String commentValue() default "";

    /**
     * 'ConfigParameterVariable' extends a methods arguments to
     * <ul>
     *   <li>Signature: {@code public static void methodName(String key, String value, ConfigParser configParser)}</li>
     *   <li>Can accept three parameters - key and value and configParser</li>
     *   <li>Should return {@code void}</li>
     * </ul>
     * <p><b>Example implementation:</b></p>
     *
     * <pre>{@code
     * public static void parseParameterMethod(String key, String value, ConfigParser configParser)
     * {
     *     STATIC_CONFIG_FIELD = new String[] { key, value };
     * }
     * }</pre>
     * @return method name, which will used for parse field
     */
    FieldParser fieldParser() default @FieldParser();
}
