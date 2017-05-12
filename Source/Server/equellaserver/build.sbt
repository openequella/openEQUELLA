run := {
  writeDevManifests.value
  val cp = (fullClasspath in Runtime).value
  val plug_loc = target.value / "manifests"
  val o = ForkOptions(runJVMOptions = Seq(
    "-cp", Path.makeString(cp.files),
    "-Dequella.devmode=true", "-Dequella.autotest=true",
    s"-Dplugins.location=${plug_loc.getAbsolutePath}"
  ))
  Fork.java(o, Seq("com.tle.core.equella.runner.EQUELLAServer"))
}