Java Workbench
---

A playground for trying out new Java features.

# Setup

To import the example projects, you need to configure the following JDKs:
- Java 8
- Java 11
- Java 15
- Java 16-loom

## Maven Toolchain

This project uses [mavens toolchains](http://maven.apache.org/guides/mini/guide-using-toolchains.html) to use different JDKs to build a particular set of maven modules.
Example configuration files can be found in the [conf](./conf) directory.
 