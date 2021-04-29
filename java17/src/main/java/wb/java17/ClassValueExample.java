package wb.java17;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClassValueExample {

    public static void main(String[] args) throws InterruptedException {

        var time = Once.of(System::currentTimeMillis);

        System.out.println(time.get());
        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println(time.get());


        var result = Once.of(type ->
                switch (type.getSimpleName()) {
                    case "Integer":
                        yield 1;
                    case "Double":
                        yield 2;
                    default:
                        yield 3;
                });

        System.out.println(result.get(Integer.class));
        System.out.println(result.get(Integer.class));
    }

    static class Once<T> extends ClassValue<T> {

        static final class Any {
        }

        private final Function<Class<?>, T> creator;

        public Once(Function<Class<?>, T> creator) {
            this.creator = creator;
        }

        @Override
        protected T computeValue(Class<?> type) {
            return creator.apply(type);
        }

        public T get() {
            return get(Any.class);
        }

        public static <T> Once<T> of(Function<Class<?>, T> creator) {
            return new Once<>(creator);
        }

        public static <T> Once<T> of(Supplier<T> creator) {
            return new Once<>(any -> creator.get());
        }
    }
}
