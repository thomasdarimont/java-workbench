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
        danglingThreads.removeIf(this::ignoreThread);

        if (danglingThreads.isEmpty()) {
            return;
        }

        String testClassName = context.getTestClass().orElseThrow().getName();
        String methodName = context.getTestMethod().orElseThrow().getName();
        int count = danglingThreads.size();
        String message = String.format("%s.%s created %s dangling threads: %s", testClassName, methodName, count, danglingThreads);
        Assertions.fail(message);
    }

    private boolean ignoreThread(Thread t) {
        return Thread.State.TERMINATED.equals(t.getState()) || "system".equals(t.getThreadGroup().getName());
    }
}
