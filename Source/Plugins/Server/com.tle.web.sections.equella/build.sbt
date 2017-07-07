yuiResources := Seq("js/bootstrap.js", "css/bootstrap.css").
  map(p => (resourceDirectory in Compile).value / "web/bootstrap" / p)

enablePlugins(YUICompressPlugin)
