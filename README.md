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

The easiest way to build it is to install a java JDK (1.8 recommended) and install maven (https://maven.apache.org/install.html)
run "mvn package" to build the jar file with dependencies. Modify the included bat file to point to your java JDK
and it should work. You will need to download a recent basestation.sqb and standingdata.sqb to be able to see aircraft
types and to use the "mil" only properties file option. A sample properties file is included, modify it to suite
your needs. It has some details on the various keys. Note that the ZMQ topic is hardcoded to JAERO in this client. Make
sure you use the same topic in each jaero instance (check there are no spaces before or after the topic name in jaero)

Link for standingdata database:

https://www.virtualradarserver.co.uk/Files/StandingData.sqb.gz

Links for basestation database:

https://jetvision.de/resources/sqb_databases/basestation.zip

https://data.flightairmap.com/data/basestation/BaseStation.sqb.zip

http://www.virtualradarserver.co.uk/Files/BaseStation.zip

Right clicking on a line that shows a hex number should open a browser window on adsbexchange.com for that hex

![image](https://user-images.githubusercontent.com/31091871/141615219-525ca349-7940-45a5-bcae-74211e604c8a.png)


