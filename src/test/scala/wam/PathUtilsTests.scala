package wam

import org.scalatest.{Outcome, fixture}
import java.nio.file.{NoSuchFileException, Path, Files}

class PathUtilsTests extends fixture.FunSpec {
  type FixtureParam = Path

  override protected def withFixture(test: OneArgTest): Outcome = {
    val path = Files.createTempDirectory(null)
    try {
      withFixture(test.toNoArgTest(path))
    } finally {
      WamFiles.deleteTree(path)
    }
  }

  describe("PathUtils") {
    describe("/") {
      it("should work like resolve()") {
        _ =>
          assert(path("one") / "two" == path("one").resolve("two"))
      }

      it("should handle slashes embedded within the path the same as if they were specified separately") {
        _ =>
          assert(path("one") / "two/three" == path("one") / "two" / "three")
      }
    }

    describe("~/") {
      it("should behave like / for paths that don't exist") {
        base =>
          assert(base ~/ "one" == base / "one")
          assert(base ~/ "OnE" == base / "OnE")
      }

      it("should correct the casing for paths that do exist") {
        base =>
          Files.createDirectories(base / "OnE")
          Files.createDirectories(base / "two")
          assert(base ~/ "one" == base / "OnE")
          assert(base ~/ "OnE" == base / "OnE")
          assert(base ~/ "TwO" == base / "two")
          assert(base ~/ "two" == base / "two")
      }
    }
  }

  describe("WamFiles") {
    describe("deleteTree") {
      it("should delete files") {
        base =>
          val path = base / "hi"
          Files.createFile(path)
          WamFiles.deleteTree(path)
          assert(!Files.exists(path), "the file still exists")
      }

      it("should delete empty directories") {
        base =>
          val path = base / "hi"
          Files.createDirectory(path)
          WamFiles.deleteTree(path)
          assert(!Files.exists(path), "the directory still exists")
      }

      it("should delete trees") {
        base =>
          val path = base / "hi"
          Files.createDirectories(path / "one" / "two" / "three")
          Files.createFile(path / "one" / "two" / "three" / "four")
          WamFiles.deleteTree(path)
          assert(!Files.exists(path), "the directory still exists")
      }

      it("should fail on paths that don't exist") {
        base =>
          val path = base / "hi"
          intercept[NoSuchFileException] {
            WamFiles.deleteTree(path)
          }
      }
    }
  }
}
