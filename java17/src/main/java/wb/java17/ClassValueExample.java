package wb.java17;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClassValueExample {

    public static void main(String[] args) throws InterruptedException {

        var time = Once.of(System::currentTimeMillis);

        System.out.println(time.get(Object.class));
        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println(time.get(Object.class));
    }

    static class Once<T> extends ClassValue<T> {

        private final Function<Class<?>, T> creator;

        public Once(Function<Class<?>, T> creator) {
            this.creator = creator;
        }

        @Override
        protected T computeValue(Class<?> type) {
            return creator.apply(type);
        }

        public static <T> Once<T> of(Function<Class<?>, T> creator) {
            return new Once<>(creator);
        }

        public static <T> Once<T> of(Supplier<T> creator) {
            return new Once<>(any -> creator.get());
        }
    }
}
