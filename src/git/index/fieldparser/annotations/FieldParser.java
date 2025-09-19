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
package git.index.fieldparser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldParser
{
    Class<?> classType() default Object.class;

    /**
     * If we using a {@code Map<Integer, Integer> } compiler will remove {@code <Integer, Integer> } and in output we will get a {@code Map<?, ?> }.br>
     * For understand which type of generic values used before compile - need this values
     * @return values, of sub-generics for class
     */
    Class<?>[] genericClasses() default {};

    /**
     * Returns the name of the method that will be used to parse configuration values.
     * <p>The method must meet the following requirements:</p>
     *
     * <ul>
     *   <li>Signature: {@code public static void methodName(String value)}</li>
     *   <li>Should accept one parameters - value (Strings)</li>
     *   <li>Should return {@code void}</li>
     * </ul>
     * <p><b>Example implementation:</b></p>
     *
     * <pre>{@code
     * public static void parseParameterMethod(String value)
     * {
     *     STATIC_CONFIG_FIELD = new String[] { value };
     * }
     * }</pre>
     * @return method name, which will used for parse field
     */
    String parseFieldMethod() default "";
}
