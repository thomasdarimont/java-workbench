package wb.java17;

import java.time.Clock;
import java.time.InstantSource;

public class InstantSourceDemo {

    public static void main(String[] args) {

        InstantSource is = Clock.systemDefaultZone();

        System.out.println(is.instant());
        System.out.println(is.millis());
    }
}
