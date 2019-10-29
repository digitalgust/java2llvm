
# java2llvm

An Example Project Show Convert Java Byte Code to LLVM IR assembler , compile standalone executable file   

This project is based on [class2ir](https://github.com/MParygin/class2ir), that based on an old llvm version.   
So I've changed instruction syntax, refactor to stack mode to fix branch problem, and repaired some bugs.   

### Currently:
Generated CentOS_x64 and MacOS executable file, and say "Hello world".    

### Make:
1. Enter directory java2llvm/   
2. Run ***mac_build.sh*** or ***centos_build.sh*** , then you will get a.out here.   

### Requirements:
 java 1.8    
* Centos:    
  CentOS 7.0 x86_64    
  llvm-as / llc / clang  5.0    
  make    
* MacOS    
  MacOS 10.15       
  XCode with cli tools 11.0     


### Known issue:
* No GC ,can add if free time.   
* Maybe some of java instruction can't work, need test   
* some of java instruction behavior simplify , eg. invokevirtual      
* Object memory allocation , like NO inheirt parent class field.     

### change log:
* Add base class java.lang.*, java.io.PrintStream   
* Add String to handle text output, StringBuilder to handle string concat   
* Trace instruction flow , to fix register var scope bug.   




==============
## class2ir readme

This project is the compiler from class files (Java byte code) to LL files (LLVM IR assembler).
Result files can be compiled by llvm-as to standalone binary ELF files.

Features:

* No JDK, no JVM
* Linux x86_64 target arch
* Extreme small size (~10-20 kB ordinary program)
* Use glibc
* Use clang for system object

At this moment project in active development, many things does not work.
