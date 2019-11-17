import mill._, scalalib._

import $ivy.`com.lihaoyi::mill-contrib-bloop:0.5.2`

object bsp extends ScalaModule {
  def scalaVersion = "2.12.7"

  override def ivyDeps = Agg(
    ivy"ch.epfl.scala:bsp4j:2.0.0-M4+10-61e61e87",
    ivy"com.kohlschutter.junixsocket:junixsocket-core:2.2.0",
    ivy"com.lihaoyi::ammonite-ops:1.6.9",
    ivy"org.scala-lang.modules::scala-java8-compat:0.8.0"
  )
}

