This is an expermental plug-in to trigger deployments in Octopus Deploy from TeamCity. 

## Building

To build the plugin from code:

 1. Install a Java JDK - I used version 6u38 from Oracle. 
 2. Install Ant from Apache. I used 1.8.4. 
 3. Add the JDK `bin` folder and ant `bin` to your path
 4. Navigate to the source directory, and run `ant`. 

The TeamCity plugin will be packaged and added to a `/source/dist` folder. 

To edit the code, you'll probably want to install IntelliJ community edition. JetBrains provide [instructions for configuring IntelliJ](http://confluence.jetbrains.com/display/TCD7/Bundled+Development+Package) for TeamCity plugin development. 