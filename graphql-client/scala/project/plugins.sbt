addSbtPlugin("com.github.ghostdogpr" % "caliban-codegen-sbt" % "3.0.0")
addSbtPlugin("de.heikoseeberger"     % "sbt-header"          % "5.10.0")
// there was an issue with the OSSRH sunset meaning new version are now (temporarily) hosted on johnnei's repo
// See https://github.com/scapegoat-scala/sbt-scapegoat/issues/243
//addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat"       % "1.2.13")
addSbtPlugin("org.johnnei.scapegoat" %% "sbt-scapegoat" % "1.3.7")
