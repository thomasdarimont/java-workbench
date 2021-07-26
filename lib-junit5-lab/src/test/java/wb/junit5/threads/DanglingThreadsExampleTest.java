package wb.junit5.threads;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@ExtendWith(DetectDanglingThreadsExtension.class)
@Disabled
public class DanglingThreadsExampleTest {

    @Test
    public void start_join_1_thread_should_report_no_dangling_threads() throws Exception {
        Thread th = new Thread(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
        th.start();
        th.join();
    }

    @Test
    public void start_no_join_1_thread_should_report_dangling_threads() {
        Thread th = new Thread(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
        th.start();
    }

    @Test
    public void executor_service_with_shutdown_should_report_no_dangling_threads() throws InterruptedException {

        ExecutorService es = Executors.newFixedThreadPool(2);
        es.submit(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
        es.awaitTermination(1, TimeUnit.SECONDS);
        es.shutdown();
    }

    @Test
    public void executor_service_without_shutdown_should_report_dangling_threads() {

        ExecutorService es = Executors.newFixedThreadPool(2);
        es.submit(() -> LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)));
    }
}
