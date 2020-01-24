#!/bin/sh

###
## ProM specific
###
PROGRAM=ProM
ROOTDIR=./
DISTDIR=${ROOTDIR}"/ProM69_dist"
LIBDIR=${ROOTDIR}"/ProM69_lib"
MAIN=org.processmining.contexts.cli.CLI

####
## Environment options
###
JAVA=java

###
## Main program
###

CP=./

add() {
	CP=${CP}:$1
}

for jar in $DISTDIR/*.jar
do
    add $jar
done

for lib in $LIBDIR/*.jar
do
	add $lib
done


#set -x

$JAVA \
    -Djava.system.class.loader=org.processmining.framework.util.ProMClassLoader \
    -Djava.util.Arrays.useLegacyMergeSort=true \
    -classpath ${CP} -Djava.library.path=${LIBDIR} -Xmx4G ${MAIN} \
    $1 $2
