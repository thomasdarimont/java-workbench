package wb.java15;

class ExtensiblePseudoEnums {

    static final TypeHint CUSTOM = "custom"::toString;

    public static void main(String[] args) {
        consumeTypeHint(TypeHint.DEFAULT);
        consumeTypeHint(TypeHint.PII);
        consumeTypeHint(CUSTOM);
    }

    static void consumeTypeHint(TypeHint typeHint) {
        System.out.println(typeHint.typeHint());

        if (typeHint == TypeHint.DEFAULT) {
            System.out.println("handle default");
        } else if (typeHint == TypeHint.PII) {
            System.out.println("handle" +
                    " PII");
        } else if (typeHint == CUSTOM) {
            System.out.println("handle CUSTOM");
        }
    }

    interface TypeHint {

        TypeHint DEFAULT = "default"::toString;

        TypeHint PII = "pii"::toString;

        String typeHint();
    }
}
