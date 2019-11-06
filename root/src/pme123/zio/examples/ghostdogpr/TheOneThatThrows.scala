package pme123.zio.examples.ghostdogpr

import java.nio.file.{Files, Path, Paths}

import pme123.zio.examples.ghostdogpr.TheOneThatThrows.copyFile
import zio._

object TheOneThatThrows {

  def copyFile(path: String, destination: String): Task[Path] = IO.effect(
    Files.copy(Paths.get(path), Paths.get(destination))
  )

}
object CopyRunner extends App {
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    copyFile("/pme", "pme2")
      .catchAll(ex => console.putStrLn(s"Expected ERROR: $ex")) // this should be printed
      .fold(
        _ => -1,
        _ => 0
      )
}
