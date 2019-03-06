These JARs are required for the Admin Console tests to work correctly. 
As yet there is no ant task to create these so if you have have time 
feel free to make one.  

Steps to create JARs

1. Export JARs directly from Eclipse by right-clicking projects and exporting as non-runnable jars to known location
2. Replace existing JARs in TLE Automated Tests > lib > adminjars

Projects required

1. TLE Administration Console
2. com.tle.platform.equella
3. com.tle.platform.common (Platform)
4. com.tle.platform.swing
5. com.tle.webstart.admin
