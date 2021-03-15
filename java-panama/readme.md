Panama
---

# Setup Panama
```
set JAVA_HOME=c:\Users\tom\dev\tools\java\jdk-16-panama
set PATH=%JAVA_HOME%\bin;%PATH%
```

# Generate
```
jextract -d target/classes ^
         -t hello ^
         -lhelloworld ^
         helloworld.h
```

# HelloWorldPanama
```java
package wb.java16panama;

public class HelloPanama {

    public static void main(String[] args) {
        hello.helloworld_h.helloworld();
    }
}
```

# Build
```
javac -cp target/classes ^
      -d target\classes ^
      src\main\java\wb\java16panama\HelloWorldPanama.java
```

# Run
```
java -cp target\classes ^
     -Dforeign.restricted=permit ^
     --add-modules jdk.incubator.foreign ^
     wb.java16panama.HelloWorldPanama
```