import org.apache.axis2.wsdl.WSDL2Java

val axis2Version = "1.6.2"
libraryDependencies ++= Seq(
  "commons-codec"     % "commons-codec"         % "1.7",
  "commons-discovery" % "commons-discovery"     % "0.5",
  "org.apache.axis2"  % "axis2-kernel"          % axis2Version,
  "org.apache.axis2"  % "axis2-adb"             % axis2Version,
  "org.apache.axis2"  % "axis2-transport-http"  % axis2Version,
  "org.apache.axis2"  % "axis2-transport-local" % axis2Version
)

dependsOn(LocalProject("com_tle_platform_common"))
dependsOn(LocalProject("com_tle_platform_swing"))

sourceGenerators in Compile += Def.task {
  val gensrc      = (sourceManaged in Compile).value
  val equellaWSDL = baseDirectory.value / "ant/SoapService51.wsdl"

  WSDL2Java.main(
    Array(
      "-o",
      gensrc.getAbsolutePath,
      "-S",
      ".",
      "-ssi",
      "-p",
      "com.tle.web.remoting.soap",
      "-uri",
      equellaWSDL.getAbsolutePath
    ))
  (gensrc ** "*.java").get
}.taskValue
