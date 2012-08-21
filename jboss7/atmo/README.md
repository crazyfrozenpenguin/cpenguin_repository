atmo
=====================

What is it?
-----------

JBoss 7 + Atmosphere Framework example.

This is a sample, deployable Maven 3 project to help you get your foot in the door developing with Atmosphere on JBoss AS 7.1. 

http://riaconnection.wordpress.com/2012/03/19/using-atmosphere-in-jboss-as-7-to-push-data-to-your-html-web-page/


Build and Deploy 
-------------------------

        mvn clean install

		cp ./atmo-ear/target/atmo.ear $JBOSS_HOME/standalone/deployments
		
		
Run the Application 
-------------------------
		
		touch $JBOSS_HOME/standalone/deployments/atmo.ear.dodeploy
		
		$JBOSS_HOME/bin/standalone.sh


Open in browser
-------------------------------------
        
        http://localhost:8080/atmo-web/index.html
