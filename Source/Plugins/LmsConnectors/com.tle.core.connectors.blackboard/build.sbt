import org.apache.axis2.wsdl.WSDL2Java

sourceGenerators in Compile += Def.task {
  val gensrc = (sourceManaged in Compile).value
  val equellaWSDL = baseDirectory.value / "EQUELLA.WS.wsdl"
  val contextWSDL = baseDirectory.value / "Context.WS.wsdl"

  WSDL2Java.main(Array(
    "-o", gensrc.getAbsolutePath, "-S", ".", "-d", "adb",
    "-p", "com.tle.core.connectors.blackboard.webservice",
    "-uri", equellaWSDL.getAbsolutePath
  ))
  WSDL2Java.main(Array(
    "-o", gensrc.getAbsolutePath, "-S", ".", "-d", "adb",
    "-p", "com.blackboard",
    "-uri", contextWSDL.getAbsolutePath
  ))
  (gensrc ** "*.java").get
}.taskValue