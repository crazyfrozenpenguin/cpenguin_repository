#!/bin/bash

VERSION=1.2.2
LIBS=(core lang-groovy lang-java lang-jruby lang-jython lang-rhino platform testframework)

for NAME in ${LIBS[*]}
do
	printf "Adding vertx-%s-%s.final.jar to local repository...\n" $NAME $VERSION
	mvn install:install-file -DgroupId=com.vertx -DartifactId=vertx-$NAME -Dversion=$VERSION -Dfile=./lib/repository/vertx-$NAME-$VERSION.final.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=./lib/repository
done
#mvn install:install-file -DgroupId=com.vertx -DartifactId=vert.x-core -Dversion=1.2.0 -Dfile=./lib/repository/vert.x-core-1.2.0.final.jar -Dpackaging=jar -DgeneratePom=true

