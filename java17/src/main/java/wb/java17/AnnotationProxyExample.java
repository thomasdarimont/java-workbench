package wb.java17;

import sun.reflect.annotation.AnnotationParser;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Darimont
 * <p>
 * This needs the following added to the java compiler and launcher options:
 * <pre>
 * --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED
 * </pre>
 */
public class AnnotationProxyExample {

    public static void main(String[] args) {

        System.out.printf("Custom annotation creation: %s%n",
                createAnnotationInstance(Collections.singletonMap("value", "required"), Example.class));

        System.out.printf("Traditional annotation creation: %s%n",
                X.class.getAnnotation(Example.class));
    }

    private static <A extends Annotation> A createAnnotationInstance(Map<String, Object> customValues, Class<A> annotationType) {

        Map<String, Object> values = new HashMap<>();

        //Extract default values from annotation
        for (Method method : annotationType.getDeclaredMethods()) {
            values.put(method.getName(), method.getDefaultValue());
        }

        //Populate required values
        values.putAll(customValues);

        return (A) AnnotationParser.annotationForMap(annotationType, values);
    }

    @Example("required")
    static class X {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Example {
        String value();

        int foo() default 42;

        boolean bar() default true;
    }
}