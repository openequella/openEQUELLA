To be able to compile the com.tle.reporting.* plugins you must follow these instructions:

1. In Eclipse, right click on the build.xml file of this plugin and select Run As -> Ant Build..., then choose the "make" target and click Run
2. Refresh this project in Eclipse.
3. Add the Target Platform:
  a. Window -> Preferences -> Plug-in Development -> Target Platform
  b. Click Add
  c. Choose 'Current Target' and click Next
  d. Click Add, choose Directory and click Next
  e. Enter the path to {your codebase}\Source\Plugins\Birt\org.eclipse.birt.osgi\resources\birt\plugins  and click Finish
  f. Click Finish
  
 
