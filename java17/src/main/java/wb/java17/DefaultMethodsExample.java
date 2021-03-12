package wb.java17;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DefaultMethodsExample {

    public static void main(String[] args) throws Throwable {

        Greeter greeter = (Greeter)Proxy.newProxyInstance(Greeter.class.getClassLoader(), new Class[]{Greeter.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, args);
                }

                return null;
            }
        });

        String greeting = greeter.greet("World");
        System.out.println(greeting);

    }

    interface Greeter {
        default String greet(String name) {
            return "Hello " + name;
        }
    }
}
