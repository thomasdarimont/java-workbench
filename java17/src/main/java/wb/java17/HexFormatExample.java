package wb.java17;

import java.util.HexFormat;

public class HexFormatExample {

    public static void main(String[] args) {
        var fmt = HexFormat.ofDelimiter(", ").withPrefix("0x");
        String hexString = fmt.formatHex("AAAA".getBytes());
        System.out.println(hexString);

        System.out.println(new String(fmt.parseHex(hexString)));
    }
}
