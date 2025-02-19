# E2SAR-JAVA
A Java JNI wrapper for [E2SAR](https://github.com/JeffersonLab/E2SAR). 

This project contains two main components
1. The Java API which seeks to reproduce the E2SAR API 
2. The C++ project which creates a shared library which is the bridge between the Java interface and the native E2SAR project

Both components are dependent on each other to function, the high level details of both are given below

## JNIE2sar - C++ JNI Wrapper
### Prerequisites

- CMAKE 
- Java (openjdk 17) needs to be installed to access the JNI header files
-  Either compiling and building [E2SAR](https://github.com/JeffersonLab/E2SAR) from source or a release of [E2SAR-Releases](https://github.com/JeffersonLab/E2SAR/releases/) to be installed. The latter is preferred. 
    - The release contains all dependencies of E2SAR(GRPC,Boost) which otherwise need to be compiled and installed separately. Follow the instructions on the E2SAR repo.

### Building jnie2sar.so

`PKG_CONFIG_PATH` needs to be set for cmake to find `libe2sar`. If the relase .deb or .rpm is installed the default install location will be `/usr/local/lib:/usr/local/lib64`. Similary `LIBRARY_PATH` needs to be set for compilation and `LD_LIBRARY_PATH` would need to be set for linking.

`JAVA_HOME` also needs to be set to find the JNI header files.

Use the follwing commands to set up environment assuming default installation location of E2sar.deb/rpm

```bash
$ export LIBRARY_PATH=/usr/local/lib:/usr/local/lib64 
$ export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib64  
$ export PKG_CONFIG_PATH=/usr/local/lib/pkgconfig:/usr/local/lib64/pkgconfig
$ export JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64
```

The following cmake commands can then be run to build and install `libjnie2sar.so`. The default install location is  /lib. To install in another directory you have to sepecify the `CMAKE_INSTALL_PREFIX`. For example if `-DCMAKE_INSTALL_PREFIX=/usr/local` then `libjnie2sar.so` will be installed in `/usr/local/lib`

```bash
 $ cmake -DCMAKE_INSTALL_PREFIX=/path/to/install -S . -B build
 $ cmake --build build --target install
```
 

## Java API

### Prerequisites
- Built on openJDK 17
- Maven
- links to the shared library which needs to be set with `-Djava.library.path`

### Compiling and Packaging JAR

Once the C++ JNI wrapper has been built and installed, we can compile and package our `E2SAR-JAVA` JAR

To compile use the following command. For developers, the compilation provides the header files (in `target/headers`) of the expected JNI headers which needs to be used in your application. 
```bash
$ mvn clean compile
```
Packaging in maven will run all the tests in `E2SAR-JAVA`, the live tests are only meant to be run on FABRIC so these will fail. To run only unit tests we will specify the package using `-Dtest=org.jlab.hpdf.unit.**` 

Maven surefire also does not parse `-Djava.library.path` directly so you need to wrap it `-DargLine`. Assuming that you have installed `libjnie2sar.so` in `/usr/local/lib`, you can use the following command to package and install `E2SAR-JAVA` to the local maven repository

```bash
$ mvn -DargLine='-Djava.library.path=/usr/local/lib' clean install -Dtest='org.jlab.hpdf.unit.**'
```

## Testing

Unit and live tests have been reproduced from [E2SAR-tests](https://github.com/JeffersonLab/E2SAR/tree/main/test). Both of them require `libjnie2sar.so` installed. Assuming installation path is `/usr/local/lib` for the following commands

Unit Tests can be run on your machine using `mvn -DargLine='-Djava.library.path=/path/to/directory' clean test -Dtest='org.jlab.hpdf.unit.**'`

Live Tests require an instance of UDPLBd running need the `EJFAT_URI` environment variable to be set (e.g `export EJFAT_URI="ejfats://udplbd@192.168.0.3:18347/"`)

There is a [jupyter notebook](scripts/notebooks/EJFAT/E2SAR-release-tester.ipynb) which runs all tests on FABRIC testbed 

## Generating JavaDocs
Javadocs can be created using the following command
`mvn clean javadoc:javadoc`

## Related information
- [E2SAR](https://github.com/JeffersonLab/E2SAR)
- [UDPLBd repo](https://github.com/esnet/udplbd) (aka Control Plane)
- [ejfat-rs repo](https://github.com/esnet/ejfat-rs) (command-line tool for testing)
- [Integrating with EJFAT](https://docs.google.com/document/d/1aUju_pWtHpS0Coesu8dC7HP6LbuKBJZqRYAMSSBtpWQ/edit#heading=h.zbhmzz3u1sna) document