run := {
  writeDevManifests.value
  val cp = (fullClasspath in Runtime).value
  val o = ForkOptions(runJVMOptions = Seq(
    "-cp", Path.makeString(cp.files),
    "-Dequella.devmode=true", "-Dequella.autotest=true"
  ))
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}