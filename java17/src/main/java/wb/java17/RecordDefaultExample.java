package wb.java17;

import java.lang.reflect.Modifier;

public class RecordDefaultExample {

    public static void main(String[] args) {

        var foo = new Foo(42,"Test");
        System.out.println(foo);

        System.out.println(Modifier.isPublic(Foo.class.getModifiers()));
        System.out.println(Modifier.isStatic(Foo.class.getModifiers()));

    }

    record Foo(int var1, String var2) {}
}
