package wb.junit5.threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

public class DetectDanglingThreadsExtension implements BeforeEachCallback, AfterEachCallback {

    Set<Thread> threadsBeforeTest;

    @Override
    public void beforeEach(ExtensionContext context) {
        threadsBeforeTest = Thread.getAllStackTraces().keySet();
    }

    @Override
    public void afterEach(ExtensionContext context) {

        Set<Thread> danglingThreads = Thread.getAllStackTraces().keySet();
        danglingThreads.removeAll(threadsBeforeTest);
        danglingThreads.removeIf(t ->  {
            return Thread.State.TERMINATED.equals(t.getState());
        });

        if (danglingThreads.size() > 0) {
            String message = String.format("%s.%s created %s dangling threads: %s",
                    context.getTestClass().orElseThrow().getName(), context.getTestMethod().orElseThrow().getName(), danglingThreads.size(), danglingThreads);
            Assertions.fail(message);
        }
    }
}
