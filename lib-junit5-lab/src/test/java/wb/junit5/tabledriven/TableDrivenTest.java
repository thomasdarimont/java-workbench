package wb.junit5.tabledriven;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableDrivenTest {

    @TestFactory
    Stream<DynamicTest> tableDrivenTest() {

        record TestCase(String name, int a, int b, int sum) {

            public void check() {
                assertEquals(sum, a + b, name);
            }
        }

        var testCases = new TestCase[]{
                new TestCase("test1", 1, 2, 3),
                new TestCase("test2", 2, 2, 4),
                new TestCase("test3", 4, 2, 6),
        };

        return Stream.of(testCases)
                .map(tc -> DynamicTest.dynamicTest(tc.name(), tc::check));
    }


    @TestFactory
    Stream<DynamicTest> tableDrivenTestFromAnnotations() {

        record TestCase(String name, int a, int b, int sum, int diff) {

            @Test
            @DisplayName("${name}: ${a} + ${b} = ${sum}")
            public void plus() {
                assertEquals(sum, a + b, name);
            }

            @Test
            @DisplayName("${name}: ${a} - ${b} = ${diff}")
            public void minus() {
                assertEquals(diff, a - b, name);
            }
        }

        var testCases = new TestCase[]{
                new TestCase("test1", 1, 2, 3, -1),
                new TestCase("test2", 2, 2, 4, 0),
                new TestCase("test3", 4, 2, 6, 2),
        };

        return Stream.of(testCases).flatMap(this::reflectiveDynamicTest);
    }

    Stream<DynamicTest> reflectiveDynamicTest(Object record) {
        if (!record.getClass().isRecord()) {
            return null;
        }

        RecordComponent[] recordComponents = record.getClass().getRecordComponents();

        List<DynamicTest> dynamicTests = new ArrayList<>();
        for (Method m : record.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                DisplayName displayName = m.getAnnotation(DisplayName.class);
                String testName = m.getName();
                if (displayName != null) {
                    testName = renderName(displayName.value(), record, recordComponents);
                }

                dynamicTests.add(DynamicTest.dynamicTest(testName, () -> m.invoke(record)));
            }
        }


        return dynamicTests.stream();
    }

    private static String renderName(String value, Object record, RecordComponent[] recordComponents) {
        String name = value;
        for (RecordComponent rc : recordComponents) {
            Object result = null;
            try {
                result = rc.getAccessor().invoke(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
            name = name.replaceAll("\\$\\{" + rc.getName() + "}", String.valueOf(result));
        }
        return name;
    }
}
