import java.io.{File, InputStream, OutputStream}
import java.nio.file.Path
import java.util.concurrent.{CompletableFuture, Executors}

import ch.epfl.scala.bsp4j._
import org.eclipse.lsp4j.jsonrpc.{Launcher, RemoteEndpoint}

import scala.collection.JavaConverters._
import scala.reflect._
import org.newsclub.net.unix.{AFUNIXSocket, AFUNIXSocketAddress}
import ammonite.ops._

import scala.concurrent.Future
import scala.util.Random

import scala.compat.java8.FutureConverters._

//Just a quick and dirty script so I can understand how to do BSP
//Credit to @tindzk

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object HelloWorld {

  trait BloopServer extends BuildServer with ScalaBuildServer

  //At the moment just print results
  val printClient = new BuildClient {
    override def onBuildShowMessage(params: ShowMessageParams): Unit = println("onBuildShowMessage", params)

    override def onBuildLogMessage(params: LogMessageParams): Unit = println("onBuildLogMessage", params)

    override def onBuildTaskStart(params: TaskStartParams): Unit = println("onBuildTaskStart", params)

    override def onBuildTaskProgress(params: TaskProgressParams): Unit = println("onBuildTaskProgress", params)

    override def onBuildTaskFinish(params: TaskFinishParams): Unit = println("onBuildTaskFinish", params)

    override def onBuildPublishDiagnostics(params: PublishDiagnosticsParams): Unit = println("onBuildPublishDiagnostics", params)

    override def onBuildTargetDidChange(params: DidChangeBuildTarget): Unit = println("onBuildTargetDidChange", params)
  }

  def main(args: Array[String]): Unit = {

    val es = Executors.newCachedThreadPool()

    val socket = AFUNIXSocket.newInstance()
    val tempSocketFile: FilePath = pwd / s"tempSocketFile-${Random.nextLong()}.socket"

    //don't block
    Future {
      ammonite.ops.%('bloop, "bsp", "--socket", tempSocketFile)(ammonite.ops.pwd)
    }
    Thread.sleep(1000)

    socket.connect(new AFUNIXSocketAddress(tempSocketFile.toNIO.toFile))

    val launcher = new Launcher.Builder[BloopServer]()
      .setRemoteInterface(classOf[BloopServer])
      .setExecutorService(es)
      .setInput(socket.getInputStream)
      .setOutput(socket.getOutputStream)
      .setLocalService(printClient)
      .create()

    launcher.startListening()
    val server = launcher.getRemoteProxy

    printClient.onConnectWithServer(server)

    Thread.sleep(1000)

    println("attempting build initialize")

    val initBuildParams = new InitializeBuildParams(
      "bsp",
      "1.3.4",
      "2.0",
      s"file:///$pwd",
      new BuildClientCapabilities(List("scala").asJava)
    )

    val buildTargetId = List(new BuildTargetIdentifier(s"file:///$pwd?id=bsp"))
    val compileParams = new CompileParams(buildTargetId.asJava)


    println(s"Request build/initialize $initBuildParams")
    for {
      initializeBuildResult <- server.buildInitialize(initBuildParams).toScala
      x = {
        println(s"Response $initializeBuildResult")
        server.onBuildInitialized()
      }
      compileResult <- {
        println(s"Request buildTarget/compile $compileParams")
        server.buildTargetCompile(compileParams).toScala
      }
    } {
      println(s"response: $compileResult")
    }

  }

}