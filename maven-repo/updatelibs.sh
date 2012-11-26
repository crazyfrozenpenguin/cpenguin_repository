#!/bin/bash

VERSION=1.3.0
LIBS=(core lang-groovy lang-java lang-jruby lang-jython lang-rhino platform testframework)

for NAME in ${LIBS[*]}
do
	printf "Adding vertx-%s-%s.final.jar to local repository...\n" $NAME $VERSION
	mvn install:install-file -DgroupId=com.vertx -DartifactId=vertx-$NAME -Dversion=$VERSION -Dfile=./vertx-$NAME-$VERSION.final.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=./repository
done

