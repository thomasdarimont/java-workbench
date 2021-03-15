package wb.java15;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FixedLengthLineMapperExample {

    public static void main(String[] args) {

        LineMapper lm = new LineMapper().field(2, Data::setField1).field(4, Data::setField2);

        Data data = new Data();
        lm.map(data, "112222");

        System.out.println(data);
    }

    static class Data {

        CharSequence field1;

        CharSequence field2;

        public CharSequence getField1() {
            return field1;
        }

        public void setField1(CharSequence field1) {
            this.field1 = field1;
        }

        public CharSequence getField2() {
            return field2;
        }

        public void setField2(CharSequence field2) {
            this.field2 = field2;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "field1='" + field1 + '\'' +
                    ", field2='" + field2 + '\'' +
                    '}';
        }
    }

    static class LineMapper {

        private final List<Field> fields = new ArrayList<>();

        public LineMapper field(int len, BiConsumer<Data, CharSequence> consumer) {

            fields.add(new Field(len, consumer));

            return this;
        }

        public void map(Data data, String line) {

            StringBuilder sb = new StringBuilder(16);
            int p = 0;
            for (Field field : fields) {
                int len = field.len;
                for (int i = 0; i < len; i++) {
                    sb.insert(i, line.charAt(p + i));
                }
                field.consumer.accept(data, sb.substring(0, len));
                p += len;
            }
        }

        static class Field {
            int len;
            BiConsumer<Data, CharSequence> consumer;

            public Field(int len, BiConsumer<Data, CharSequence> consumer) {
                this.len = len;
                this.consumer = consumer;
            }
        }
    }
}
