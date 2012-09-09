Traits
=====================

What is it?
-----------

Traits is a simple properties management application that provides:

1. A REST interface to manage and retrieve the desired properties file or files on a given order
2. A web page to allow users to easily manage the different properties sets

Technologies used
-----------------

Backend: JBoss AS 7.1.1

UI: JQuery, Knockoutjs, Sammy

System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on JBoss Enterprise Application Platform 6 or JBoss AS 7.1. 

A directory where to store the configuration sets.
 
Configure Maven
---------------

If you have not yet done so, you must [Configure Maven](../README.html/#mavenconfiguration) before testing the quickstarts.


Start JBoss Enterprise Application Platform 6 or JBoss AS 7.1
-------------------------

1. Open a command line and navigate to the root of the JBoss server directory.
2. The following shows the command line to start the server with the web profile:

        For Linux:   JBOSS_HOME/bin/standalone.sh -DConfigDir="/path/to/config/dir"
        For Windows: JBOSS_HOME\bin\standalone.bat -DConfigDir="/path/to/config/dir"

 
Build and Deploy the Quickstart
-------------------------

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.html/#buildanddeploy) for complete instructions and additional options._

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type this command to build and deploy the archive:

        mvn clean package jboss-as:deploy

4. This will deploy `target/traits.ear` to the running instance of the server.


Access the application 
---------------------

The application will be running at the following URL <http://localhost:8080/traits/>.


Undeploy the Archive
--------------------

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. When you are finished testing, type this command to undeploy the archive:

        mvn jboss-as:undeploy


Run the Tests 
-------------------------

No tests provided at this state. Arquillian, Selenium, TestNG and JBehave to be provided in a near future.


Run the Quickstart in JBoss Developer Studio or Eclipse
-------------------------------------
You can also start the server and deploy the quickstarts from Eclipse using JBoss tools. For more information, see [Use JBoss Developer Studio or Eclipse to Run the Quickstarts](../README.html/#useeclipse) 


Debug the Application
---------------------

If you want to debug the source code or look at the Javadocs of any library in the project, run either of the following commands to pull them into your local repository. The IDE should then detect them.

        mvn dependency:sources
        mvn dependency:resolve -Dclassifier=javadoc
