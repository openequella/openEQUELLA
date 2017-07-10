lazy val Hibernate = config("hibernate")
lazy val CustomCompile = config("compile") extend Hibernate

libraryDependencies := Seq(
  "org.hibernate" % "hibernate-core" % "3.6.8.Final",
  "org.hibernate" % "hibernate-validator" % "4.2.0.Final",
//  "org.hibernate" % "hibernate-validator-annotation-processor" % "4.2.0.Final",
  "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
  "com.thoughtworks.xstream" % "xstream-hibernate" % "1.4.9" excludeAll (
    ExclusionRule(organization = "org.hibernate")
  ),
  "org.springframework" % "spring-orm" % "2.5.5" excludeAll(
    ExclusionRule(organization = "com.oracle", name = "toplink-essentials"),
    ExclusionRule(organization = "org.springframework", name = "spring-beans"),
    ExclusionRule(organization = "org.springframework", name = "spring-tx"),
    ExclusionRule(organization = "org.springframework", name = "spring-core"),
    ExclusionRule(organization = "org.springframework", name = "spring-context")
  )).map(_ % Hibernate)

excludeDependencies ++= Seq(
  "org.slf4j" % "slf4j-api",
  "dom4j" % "dom4j",
  "commons-collections" % "commons-collections",
  "commons-logging" % "commons-logging",
  "aopalliance" % "aopalliance"
)

ivyConfigurations := overrideConfigs(Hibernate, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Hibernate, Set("jar"), update.value)