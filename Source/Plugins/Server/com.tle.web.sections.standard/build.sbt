lazy val jql = Seq("jquery.fancybox.js", "jquery.ui.datepicker.js", "jquery.ui.draggable.js", "jquery.ui.droppable.js",
  "jquery.ui.mouse.js", "jquery.ui.resizable.js", "jquery.scrollTo.js", "jquery.ui.slider.js", "jquery.ui.sortable.js",
  "jquery.stars.js", "jquery.stylish-select.js", "jquery.ui.tabs.js", "jquery.hint.js", "jquery.timer.js", "jquery.ui.accordion.js",
  "jquery.ui.autocomplete.js", "jquery.ui.core.js", "jquery.ui.effect.js", "jquery.ui.menu.js", "jquery.ui.position.js",
  "jquery.ui.widget.js"
)
lazy val jqlc = Seq("fancybox/jquery.fancybox.css", "jquery.stylish-select.css")
lazy val jqc = Seq("jquery-migrate.js", "jquery.js")
lazy val others = Seq("css/themes/equella/jquery-ui.css")

yuiResources := {
  val bd = (resourceDirectory in Compile).value / "web"
  val jqlDir = bd / "jquerylib"
  val jqcDir = bd / "jquerycore"
  val jqlcDir = bd / "css/jquerylib"
  jql.map(jqlDir./) ++
  jqc.map(jqcDir./) ++
  jqlc.map(jqlcDir./) ++
  others.map(bd./) ++
    (jqlDir * "jquery.ui.effect-*.js").get
}

enablePlugins(YUICompressPlugin)
