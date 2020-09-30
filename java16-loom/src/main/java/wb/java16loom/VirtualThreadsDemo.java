package wb.java16loom;

import java.util.concurrent.Executors;

class VirtualThreadsDemo {

    public static void main(String[] args) {

        var executorService = Executors.newFixedThreadPool(2);

        var threadBuilder = Thread.builder()
                .name("demo-thread", 1)
                .virtual(executorService)
                .task(VirtualThreadsDemo::task);

        for (int i = 0; i < 10; i++) {
            threadBuilder.build().start();
        }
    }

    static void task() {

        System.out.printf("Started %s%n", Thread.currentThread().getName());

        int iters = 0;
        while (!Thread.currentThread().isInterrupted()) {
            iters++;

            if (iters % 10000 == 0) {
                System.out.printf("Running in %s%n", Thread.currentThread().getName());
            }

            Thread.yield();
        }
    }
}
