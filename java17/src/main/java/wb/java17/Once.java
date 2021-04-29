package wb.java17;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public interface Once<T> extends Supplier<T> {

    @SuppressWarnings("unchecked")
    static <T> Once<T> of(Supplier<? extends T> supplier) {
        var mh = new OnceCallSite(supplier).dynamicInvoker();
        return () -> {
            try {
                return (T) mh.invokeExact();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        };
    }

    class OnceCallSite extends MutableCallSite {
        private static final MethodHandle SLOW_PATH;

        static {
            var lookup = MethodHandles.lookup();
            try {
                SLOW_PATH = lookup.findVirtual(OnceCallSite.class, "slowPath", MethodType.methodType(Object.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        private Object value;
        private final Supplier<?> supplier;
        private final ReentrantLock lock = new ReentrantLock();

        private OnceCallSite(Supplier<?> supplier) {
            super(MethodType.methodType(Object.class));
            this.supplier = supplier;
            setTarget(SLOW_PATH.bindTo(this));
        }

        private Object slowPath() {
            lock.lock();
            try {
                Object value = this.value;
                if (value != null) {
                    return value;
                }
                value = Objects.requireNonNull(supplier.get());
                this.value = value;
                setTarget(MethodHandles.constant(Object.class, value));
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    class OnceDemo {
        private static int COUNTER = 0;
        private static final Once<Integer> ONCE = Once.of(() -> COUNTER++);

        public static void main(String[] args) {
            System.out.println(COUNTER);
            System.out.println(OnceDemo.ONCE.get());
            System.out.println(COUNTER);
            System.out.println(OnceDemo.ONCE.get());
            System.out.println(COUNTER);
            System.out.println(OnceDemo.ONCE.get());
            System.out.println(COUNTER);
        }
    }
}