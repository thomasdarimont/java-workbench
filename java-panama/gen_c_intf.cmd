set JAVA_HOME="C:\Program~2\OpenJDK\jdk-16-panama"
set PATH=%JAVA_HOME%\bin;%PATH%

jextract -d target/classes -t hello -lhelloworld helloworld.h