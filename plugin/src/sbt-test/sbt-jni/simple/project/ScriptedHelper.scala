import sbt._
import sbt.Keys._

object ScriptedHelper extends AutoPlugin {

  override def requires = empty
  override def trigger = allRequirements

  override def projectSettings = Seq(
    scalacOptions ++= List(
      ("-Xlint:adapted-args,nullary-unit,inaccessible,nullary-override,infer-any,doc-detached,private-shadow," +
        "type-parameter-shadow,poly-implicit-overload,option-implicit,delayedinit-select,by-name-right-associative," +
        "package-object-classes,unsound-match,stars-align,constant"),
      "-Ywarn-unused:imports,patvars,privates,locals",
      "-opt:l:method",
      "-Ywarn-unused-import",
      "-deprecation",
      "-unchecked",
      "-explaintypes",
      "-encoding",
      "UTF-8",
      "-feature",
      "-Xlog-reflective-calls",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Xfuture"
    ),
    scalaVersion := "2.12.6"
  )

}
