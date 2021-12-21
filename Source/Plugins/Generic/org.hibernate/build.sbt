lazy val Hibernate     = config("hibernate")
lazy val CustomCompile = config("compile") extend Hibernate
val springVersion      = "5.2.9.RELEASE"

libraryDependencies := Seq(
  "org.hibernate"            % "hibernate-core"        % "5.4.21.Final",
  "org.hibernate"            % "hibernate-validator"   % "6.1.5.Final",
  "javax.persistence"        % "javax.persistence-api" % "2.2",
  "com.thoughtworks.xstream" % "xstream-hibernate"     % "1.4.13" excludeAll ExclusionRule(
    organization = "org.hibernate"),
  "org.springframework" % "spring-orm" % springVersion excludeAll (
    ExclusionRule(organization = "com.oracle", name = "toplink-essentials"),
    ExclusionRule(organization = "org.springframework", name = "spring-beans"),
    ExclusionRule(organization = "org.springframework", name = "spring-tx"),
    ExclusionRule(organization = "org.springframework", name = "spring-core"),
    ExclusionRule(organization = "org.springframework", name = "spring-context")
  )
).map(_ % Hibernate)

excludeDependencies ++= Seq(
  "org.slf4j"           % "slf4j-api",
  "dom4j"               % "dom4j",
  "commons-collections" % "commons-collections",
  "commons-logging"     % "commons-logging",
  "aopalliance"         % "aopalliance"
)

ivyConfigurations := overrideConfigs(Hibernate, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Hibernate, Set("jar"), update.value)
