package wb.javaloom;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

class VirtualThreadsDemo {

    public static void main(String[] args) throws InterruptedException {

        int carrierThreads = 2;

        var executorService = Executors.newFixedThreadPool(carrierThreads);

        int nTasks = 10;
        CountDownLatch cdl = new CountDownLatch(nTasks);

        Runnable task = () -> VirtualThreadsDemo.sleep(cdl);

        var threadBuilder = Thread.ofVirtual()
                .name("virtual-thread-", 1)
                .scheduler(executorService)
                .factory();

        boolean useVirtualThreads = true;

        for (int i = 0; i < nTasks; i++) {
            if (useVirtualThreads) {
                threadBuilder.newThread(task).start();
            } else {
                executorService.execute(task);
            }
        }

        System.out.println("Waiting for task completion");
        cdl.await();
        executorService.shutdown();
        System.out.println("Terminated");
    }

    static void task(CountDownLatch cdl) {

        System.out.printf("Started %s%n", Thread.currentThread().getName());

        int iters = 0;
        while (!Thread.currentThread().isInterrupted()) {
            iters++;

            if (iters % 10000 == 0) {
                Thread.yield();

                System.out.printf("Running in %s%n", Thread.currentThread().getName());
            }

            if (iters > 100000) {
                break;
            }
        }

        cdl.countDown();
    }

    static void sleep(CountDownLatch cdl) {
        System.out.printf("Going to sleep %s%n", Thread.currentThread().getName());
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Woke up %s%n", Thread.currentThread().getName());
        cdl.countDown();
    }
}
