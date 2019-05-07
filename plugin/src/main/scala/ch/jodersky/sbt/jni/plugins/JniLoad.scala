package ch.jodersky.sbt.jni
package plugins

import sbt._
import sbt.Keys._

object JniLoad extends AutoPlugin {

  override def requires = empty
  override def trigger = allRequirements

  lazy val settings: Seq[Setting[_]] = Seq(
    // Macro Paradise plugin and dependencies are needed to expand annotation macros.
    // Once expanded however, downstream projects don't need these dependencies anymore
    // (hence the "Provided" configuration).
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % ProjectVersion.MacrosParadise cross CrossVersion.full
    ),
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.simianquant" %% "sbt-jni-macros" % ProjectVersion.Macros,
      "com.simianquant" %% "sbt-jni-util" % ProjectVersion.Macros
    )
  )

  override def projectSettings = settings

}
