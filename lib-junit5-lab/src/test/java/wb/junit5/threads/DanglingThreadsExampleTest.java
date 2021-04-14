package wb.junit5.threads;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@ExtendWith(DetectDanglingThreadsExtension.class)
public class DanglingThreadsExampleTest {

    @Test
    public void start_join_1_thread() throws Exception {
        Thread th = new Thread(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
        th.setDaemon(true);
        th.start();
        th.join();

        while (Thread.getAllStackTraces().containsKey(th.getName())) {
            System.out.println("Waiting for thread to be removed... " + th.getName());

        }

        for (int i = 0; i < 5; i++) {
            System.gc();
            TimeUnit.MILLISECONDS.toNanos(500);
        }
        System.out.println();
    }

    @Test
    public void start_no_join_1_thread() throws Exception {
        Thread th = new Thread(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
        th.start();
    }
}
