addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "2.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.1.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

libraryDependencies ++= Seq(
  "org.jdom" % "jdom2" % "2.0.6",
  "com.typesafe" % "config" % "1.3.1",
  "org.apache.axis2" % "axis2-kernel" % "1.6.2",
  "org.apache.axis2" % "axis2-java2wsdl" % "1.6.2",
  "org.apache.axis2" % "axis2-adb" % "1.6.2",
  "org.apache.axis2" % "axis2-jaxbri" % "1.6.2",
  "org.apache.axis2" % "axis2-adb-codegen" % "1.6.2",
  "org.apache.axis2" % "axis2-codegen" % "1.6.2",
  "org.apache.axis2" % "axis2-xmlbeans" % "1.6.2",
  "commons-logging" % "commons-logging" % "1.0.4",
  "commons-discovery" % "commons-discovery" % "0.2",
  "commons-configuration" % "commons-configuration" % "1.10",
  "commons-beanutils" % "commons-beanutils" % "1.9.3",
  "commons-codec" % "commons-codec" % "1.10",
  "org.slf4j" % "slf4j-nop" % "1.7.21"
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")
