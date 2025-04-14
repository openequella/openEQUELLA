import sbt._

import scala.xml.XML

case class JacocoAgent(jar: File, jacocoOpts: String) {
  def opts = s"-javaagent:${jar.absolutePath}=$jacocoOpts"
}

case class InstallOptions(
    installDir: File,
    jvmHome: File,
    url: String,
    hostname: String,
    port: Int,
    jacoco: Option[JacocoAgent],
    dbtype: String,
    dbhost: String,
    dbport: Int,
    dbname: String,
    dbuser: String,
    dbpassword: String,
    auditLevel: String
) {
  def writeXML(xmlFile: File, baseInstall: File) = {
    val optXml = <commands>
      <installer>
        <product>
          <name>EQUELLA</name>
          <version>
            &lt;
            No Product Version
            &gt;
          </version>
          <company>Apereo</company>
        </product>
        <local>{baseInstall.absolutePath}</local>
        <platform>linux64</platform>
      </installer>
      <install.path>{installDir.absolutePath}</install.path>
      <java>
        <jdk>{jvmHome.absolutePath}</jdk>
      </java>
      <datasource>
        <dbtype>{dbtype}</dbtype>
        <host>{dbhost}</host>
        <port>{dbport}</port>
        <database>{dbname}</database>
        <username>{dbuser}</username>
        <password>{dbpassword}</password>
        <trustservercerts>true</trustservercerts>
        <idtype>:</idtype>
      </datasource>
      <webserver>
        <url>{url}</url>
        <binding>all</binding>
        <host>{hostname}</host>
        <port>{port}</port>
        <context>/</context>
        <javaopts>-Xss2m -Dequella.autotest=true -Duser.timezone=Australia/Hobart {
      jacoco.map(_.opts).getOrElse("")
    }</javaopts>
      </webserver>
      <service>
        <port>3000</port>
      </service>
      <proxy>
        <connection>false</connection>
      </proxy>
      <memory>
        <min>96m</min>
        <max>512m</max>
      </memory>
      <imagemagick>
        <path>/usr/bin</path>
      </imagemagick>
      <ffmpeg>
        <path>/usr/bin</path>
      </ffmpeg>
      <auditing>
        <audit.level>{auditLevel}</audit.level>
      </auditing>
      <hashed.admin.password>SHA256:2a0fd3498c35eced0663c523a80125cbfd3fb8d1634ce87c2ccc020b924ac2d9</hashed.admin.password>
      <datastore>
        <initialise>true</initialise>
      </datastore>
      <tomcat>
        <path>{(installDir / "tomcat").absolutePath}</path>
      </tomcat>
      <admin.password>admin</admin.password>
    </commands>
    XML.save(xmlFile.absolutePath, optXml)
  }
}
