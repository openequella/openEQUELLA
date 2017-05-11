addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "2.0.0")

libraryDependencies += "org.jdom" % "jdom2" % "2.0.6"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies ++= Seq(
  "org.apache.axis2" % "axis2-kernel" % "1.6.2",
  "org.apache.axis2" % "axis2-java2wsdl" % "1.6.2",
  "org.apache.axis2" % "axis2-adb" % "1.6.2",
  "org.apache.axis2" % "axis2-jaxbri" % "1.6.2",
  "org.apache.axis2" % "axis2-adb-codegen" % "1.6.2",
  "org.apache.axis2" % "axis2-codegen" % "1.6.2",
  "org.apache.axis2" % "axis2-xmlbeans" % "1.6.2",
  "commons-logging" % "commons-logging" % "1.0.4",
  "commons-discovery" % "commons-discovery" % "0.2"
)

resourceDirectories in Compile += baseDirectory.value / "settings"