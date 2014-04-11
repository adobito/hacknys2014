name := "foursquarerpgserver"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  javaJpa,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.code.gson"%"gson"%"2.2.4",
  "com.google.http-client" % "google-http-client" % "1.18.0-rc",
    "com.google.http-client" % "google-http-client-gson" % "1.18.0-rc",
      "com.google.http-client" % "google-http-client-jackson" % "1.18.0-rc"  
)     

play.Project.playScalaSettings
