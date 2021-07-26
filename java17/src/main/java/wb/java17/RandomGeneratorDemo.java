package wb.java17;

import java.util.random.RandomGenerator;

public class RandomGeneratorDemo {

    public static void main(String[] args) {

        RandomGenerator.getDefault().nextExponential();
        RandomGenerator.getDefault().doubles().limit(5).forEach(System.out::println);
    }
}
