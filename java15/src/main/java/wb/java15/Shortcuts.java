package wb.java15;

public class Shortcuts {

    public static void main(String[] args) {

        CharSequence hello = "hello";
        System.out.println(hello.length());
    }

    public int foo(int a, int b) {
        return a + b;
    }


    void m(int x){
        System.out.println(x);
    }

    void m1(int x){
        m(x);
    }

    void m2(int x){
        m(x);
    }
}
