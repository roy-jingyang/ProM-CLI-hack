#!/bin/sh

###
## ProM specific
###
PROGRAM=ProM
ROOTDIR=./prom6/prom69 # set this to the directory where ProM.ini is
CP=./ProM69_dist/ProM-Framework-6.9.97.jar:./ProM69_dist/ProM-Contexts-6.9.56.jar:./ProM69_dist/ProM-Models-6.9.32.jar:./ProM69_dist/ProM-Plugins-6.9.67.jar
LIBDIR=./ProM69_lib
MAIN=org.processmining.contexts.cli.CLI

####
## Environment options
###
JAVA=java

###
## Main program
###

add() {
	CP=${CP}:$1
}


for lib in $LIBDIR/*.jar
do
	add $lib
done

#set -x

CP=${ROOTDIR}:${CP}
LIBDIR=${ROOTDIR}:${LIBDIR}

$JAVA \
    -Djava.system.class.loader=org.processmining.framework.util.ProMClassLoader \
    -Djava.util.Arrays.useLegacyMergeSort=true \
    -classpath ${CP} -Djava.library.path=${LIBDIR} -Xmx4G ${MAIN} \
    $1 $2