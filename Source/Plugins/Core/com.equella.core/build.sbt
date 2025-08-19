import Path.{flat, rebase}
import _root_.io.circe.parser._

libraryDependencies += "org.mockito" % "mockito-core" % "5.19.0" % Test

langStrings := {
  val langDir = (Compile / resourceDirectory).value / "com/tle/core/i18n/service/impl"
  val bundle =
    decode[Map[String, String]](IO.read(reactFrontEndLanguageBundle.value))
      .fold(throw _, identity)
  val pluginLangStrings = langStrings.value
  Seq(
    Common.loadLangProperties(langDir / "i18n-resource-centre.properties", "", "resource-centre"),
    Common.loadLangProperties(langDir / "i18n-admin-console.properties", "", "admin-console"),
    LangStrings("newui", xml = false, bundle)
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
  val bd      = (Compile / resourceDirectory).value / "web"
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
  (Compile / resourceDirectory).value / "web/bootstrap" / p
)

enablePlugins(YUICompressPlugin)

import org.apache.axis2.wsdl.WSDL2Java

Compile / sourceGenerators += Def.task {
  val gensrc      = (Compile / sourceManaged).value
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
    )
  )
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
    )
  )
  (gensrc ** "*.java").get
}.taskValue

Compile / resourceGenerators += Def.task {
  val base = (Compile / resourceManaged).value
  IO.copy(Some(versionProperties.value -> base / "web/version.properties")).toSeq
}.taskValue

Compile / resourceGenerators += Def.task {
  val baseSwagger = baseDirectory.value / "swaggerui"
  Common.nodeInstall(baseSwagger)
  Common.nodeScript("build", baseSwagger)
  val outDir = (Compile / resourceManaged).value / "web/apidocs"
  val bundle = baseSwagger / "target/bundle.js"
  val css    = baseSwagger / "node_modules/swagger-ui/dist/swagger-ui.css"
  IO.copy(Seq(bundle, css).pair(flat(outDir))).toSeq
}.taskValue

// Pull in the react-front-end
Compile / resourceGenerators += Def.task {
  val outDir = (Compile / resourceManaged).value
  val srcDir = buildReactFrontEnd.value
  IO.copy(
    (srcDir ** ("*.js" | "*.css" | "*.json" | "*.html" | "*.woff" | "*.woff2"))
      .pair(rebase(srcDir, outDir))
  ).toSeq
}.taskValue

clean := {
  clean.value
  val baseSwagger = baseDirectory.value / "swaggerui"
  Common.nodeScript("clean", baseSwagger)
}
