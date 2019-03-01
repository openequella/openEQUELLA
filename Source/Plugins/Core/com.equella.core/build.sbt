import Path.rebase
import Path.flat

import _root_.io.circe.parser._

langStrings := {
  val langDir = (resourceDirectory in Compile).value / "com/tle/core/i18n/service/impl"
  val baseJs  = baseDirectory.value / "js"
  Common.runYarn("build:langbundle", baseJs)
  val bundle =
    decode[Map[String, String]](IO.read(baseJs / "target/resources/lang/jsbundle.json"))
      .fold(throw _, identity)
  val pluginLangStrings = langStrings.value
  Seq(
    Common.loadLangProperties(langDir / "i18n-resource-centre.properties", "", "resource-centre"),
    Common.loadLangProperties(langDir / "i18n-admin-console.properties", "", "admin-console"),
    LangStrings("newui", false, bundle)
  ) ++ pluginLangStrings
}

lazy val jql = Seq(
  "jquery.fancybox.js",
  "jquery.ui.datepicker.js",
  "jquery.ui.draggable.js",
  "jquery.ui.droppable.js",
  "jquery.ui.mouse.js",
  "jquery.ui.resizable.js",
  "jquery.scrollTo.js",
  "jquery.ui.slider.js",
  "jquery.ui.sortable.js",
  "jquery.stars.js",
  "jquery.stylish-select.js",
  "jquery.ui.tabs.js",
  "jquery.hint.js",
  "jquery.timer.js",
  "jquery.ui.accordion.js",
  "jquery.ui.autocomplete.js",
  "jquery.ui.core.js",
  "jquery.ui.effect.js",
  "jquery.ui.menu.js",
  "jquery.ui.position.js",
  "jquery.ui.widget.js"
)
lazy val jqlc   = Seq("fancybox/jquery.fancybox.css", "jquery.stylish-select.css")
lazy val jqc    = Seq("jquery-migrate.js", "jquery.js")
lazy val others = Seq("css/themes/equella/jquery-ui.css")

yuiResources := {
  val bd      = (resourceDirectory in Compile).value / "web"
  val jqlDir  = bd / "jquerylib"
  val jqcDir  = bd / "jquerycore"
  val jqlcDir = bd / "css/jquerylib"
  jql.map(jqlDir./) ++
    jqc.map(jqcDir./) ++
    jqlc.map(jqlcDir./) ++
    others.map(bd./) ++
    (jqlDir * "jquery.ui.effect-*.js").get
}

yuiResources ++= Seq("js/bootstrap.js", "css/bootstrap.css").map(p =>
  (resourceDirectory in Compile).value / "web/bootstrap" / p)

enablePlugins(YUICompressPlugin)

import org.apache.axis2.wsdl.WSDL2Java

sourceGenerators in Compile += Def.task {
  val gensrc      = (sourceManaged in Compile).value
  val equellaWSDL = baseDirectory.value / "EQUELLA.WS.wsdl"
  val contextWSDL = baseDirectory.value / "Context.WS.wsdl"

  WSDL2Java.main(
    Array(
      "-o",
      gensrc.getAbsolutePath,
      "-S",
      ".",
      "-d",
      "adb",
      "-p",
      "com.tle.core.connectors.blackboard.webservice",
      "-uri",
      equellaWSDL.getAbsolutePath
    ))
  WSDL2Java.main(
    Array(
      "-o",
      gensrc.getAbsolutePath,
      "-S",
      ".",
      "-d",
      "adb",
      "-p",
      "com.blackboard",
      "-uri",
      contextWSDL.getAbsolutePath
    ))
  (gensrc ** "*.java").get
}.taskValue

resourceGenerators in Compile += Def.task {
  val base = (resourceManaged in Compile).value
  IO.copy(Some(versionProperties.value -> base / "web/version.properties")).toSeq
}.taskValue

lazy val inplaceEditorJar = project in file("jarsrc")

resourceGenerators in Compile += Def.task {
  val outJar  = (resourceManaged in Compile).value / "web/inplaceedit.jar"
  val jarFile = (assembly in inplaceEditorJar).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue

resourceGenerators in Compile += Def.task {
  val baseSwagger = baseDirectory.value / "swaggerui"
  Common.runYarn("browserify", baseSwagger)
  val outDir = (resourceManaged in Compile).value / "web/apidocs"
  val bundle = baseSwagger / "target/bundle.js"
  val css    = baseSwagger / "node_modules/swagger-ui/dist/swagger-ui.css"
  IO.copy(Seq(bundle, css).pair(flat(outDir))).toSeq
}.taskValue

buildJS := {
  val baseJs = baseDirectory.value / "js"
  Common.runYarn("build", baseJs)
  val outDir       = (resourceManaged in Compile).value
  val baseJsTarget = baseJs / "target/resources"
  IO.copy(
      (baseJsTarget ** ("*.js" | "*.css" | "*.json" | "*.html")).pair(rebase(baseJsTarget, outDir)))
    .toSeq
}

resourceGenerators in Compile += buildJS.taskValue
