name := "dinosaur"
version := "0.1.0"
organization := "io.whaling"
licenses += ("WTFPL", url("http://www.wtfpl.net/txt/copying/"))
enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-feature")
// nativeLinkingOptions += "-static -lrt -lunwind -lunwind-x86_64 -lgc"
