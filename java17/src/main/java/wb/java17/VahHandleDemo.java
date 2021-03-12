package wb.java17;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VahHandleDemo {

    public static void main(String[] args) throws InterruptedException {

        Counter ac =
                //new AtomicCounter() //
//                new NaiveCounter() //
                new SynchronizedCounter() //
                ;

        ExecutorService ex = Executors.newFixedThreadPool(2);

        int iters = 10000;

        Runnable counterLoop = () -> {
            for (int i = 0; i < iters; i++) {
                ac.inc();
            }
        };

        ex.submit(counterLoop);
        ex.submit(counterLoop);

        ex.awaitTermination(3, TimeUnit.SECONDS);

        System.out.println(ac.getValue());
    }

    interface Counter {
        int inc();

        int getValue();
    }

    public static class NaiveCounter implements Counter {
        private volatile int value;

        @Override
        public int inc() {
            return value++;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static class SynchronizedCounter implements Counter {
        private volatile int value;

        @Override
        public synchronized int inc() {
            return value++;
        }

        @Override
        public synchronized int getValue() {
            return value;
        }
    }

    public static class AtomicCounter implements Counter {

        private volatile int value;
        private static final VarHandle valueVh;

        static {
            try {
                valueVh = MethodHandles.lookup().findVarHandle(AtomicCounter.class, "value", int.class);
            } catch (ReflectiveOperationException roe) {
                throw new Error(roe);
            }
        }

        public int getValue() {
            return value;
        }

        public int inc() {

            int v;
            do {
                v = (int) valueVh.getVolatile(this);
            } while (!valueVh.compareAndSet(this, v, v + 1));

            return v;
        }
    }
}
