## Legal

PATENT NOTICE

This source code is provided for educational purposes only.  It is
a written description of how certain voice encoding/decoding
algorythims could be implemented.  Executable objects compiled or
derived from this package may be covered by one or more patents.
Readers are strongly advised to check for any patent restrictions or
licencing requirements before compiling or using this source code.


## What is it?

A simple java swing gui that allows you to connect to jaero C channels remotely

## How to build it

The easiest way to build it is to install a java jdk (1.8 recommended) and install maven.
run "mvn package" to build the jar file with dependencies. Modify the included bat file to point to your java JDK
and it should work. You will need to download a recent basestation.sqb and standingdata.sqb to be able to see aircraft
types and to use the "mil" only properties file option. A sample properties file is included, modify it to suite
your needs.
