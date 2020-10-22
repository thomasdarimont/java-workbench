package wb.java15;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MultiLineStrings {

    public static void main(String[] args) {

        Metadata metadata = Foo.class.getAnnotation(Metadata.class);
        System.out.println(metadata.value());

    }

    @Metadata("""
            TEST
            123
            456
            789
            """)
    class Foo {
    }


    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Metadata {
        String value();
    }
}
