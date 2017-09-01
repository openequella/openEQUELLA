langStrings := {
  val langDir = (resourceDirectory in Compile).value / "com/tle/core/i18n/service/impl"
  Seq(
    Common.loadLangProperties(langDir / "i18n-resource-centre.properties", "", "resource-centre"),
    Common.loadLangProperties(langDir / "i18n-admin-console.properties", "", "admin-console")
  )
}