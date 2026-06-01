project-name Application - Maven
=================================

This is a Maven-based Click template project with the following structure:

Maven Standard Directory Layout
-------------------------------

 +---[src/main/java]              Java source files
 |
 +---[src/main/resources]         Application resource files
 |
 +---[src/main/webapp]            Web application root directory
 |    |
 |    +---[admin]                 Admin role pages directory
 |    |
 |    +---[assets]                Web static assets directory
 |    |
 |    +---[META-INF]              Tomcat context.xml directory
 |    |
 |    +---[user]                  User role pages directory
 |    |
 |    +---[WEB-INF]               Protected Web Inf directory
 |         |
 |         +---click.xml          Click configuration file
 |         |
 |         +---menu.xml           Menu configuration file
 |         |
 |         +---web.xml            Web configuration file
 |
 +---[target]                     Build output directory (generated)
 |
 +---pom.xml                       Maven build configuration
 |
 +---README.txt                    This file


Building the Application
------------------------

To build the application WAR file using Maven:

    mvn clean package

To run the application with the embedded Jetty server:

    mvn jetty:run

To deploy to a Tomcat server:

    mvn clean package
    # Copy target/template.war to Tomcat's webapps/ directory