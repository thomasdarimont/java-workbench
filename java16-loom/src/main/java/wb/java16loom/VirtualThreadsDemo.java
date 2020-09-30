package wb.java16loom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class VirtualThreadsDemo {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Thread.Builder virtualThreadBuilder = Thread.builder().name("demo-thread", 1).virtual(executorService);
        for (int i = 0; i < 10; i++) {
            Thread thread = virtualThreadBuilder.task(() -> {

                System.out.printf("Started %s%n", Thread.currentThread().getName());

                int iters = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    iters++;

                    if (iters % 10000 == 0) {
                        System.out.printf("Running in %s%n", Thread.currentThread().getName());
                    }
                    Thread.yield();
                }
            }).build();

            thread.start();
        }
    }
}
