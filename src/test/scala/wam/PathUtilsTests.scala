package wam

import org.scalatest.{Outcome, fixture}
import java.nio.file.{NoSuchFileException, Path}

class PathUtilsTests extends fixture.FunSpec {
  type FixtureParam = (Path, Files)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val files = new Files {}
    val path = files.createTempDirectory()
    try {
      withFixture(test.toNoArgTest((path, files)))
    } finally {
      files.deleteTree(path)
    }
  }

  describe("PathUtils") {
    describe("/") {
      it("should work like resolve()") {
        _ =>
          assert(path("one") / "two" === path("one").resolve("two"))
      }

      it("should handle slashes embedded within the path the same as if they were specified separately") {
        _ =>
          assert(path("one") / "two/three" === path("one") / "two" / "three")
      }
    }

    describe("~/") {
      it("should behave like / for paths that don't exist") {
        case (base, files) =>
          implicit val _files = files // ~/ takes a Files, but we can't mark stuff as implicit while pattern-matching over it
          assert(base ~/ "one" === base / "one")
          assert(base ~/ "OnE" === base / "OnE")
      }

      it("should correct the casing for paths that do exist") {
        case (base, files) =>
          implicit val _files = files
          files.createDirectories(base / "OnE")
          files.createDirectories(base / "two")
          assert(base ~/ "one" === base / "OnE")
          assert(base ~/ "OnE" === base / "OnE")
          assert(base ~/ "TwO" === base / "two")
          assert(base ~/ "two" === base / "two")
      }
    }
  }

  describe("Files") {
    describe("deleteTree") {
      it("should delete files") {
        case (base, files) =>
          val path = base / "hi"
          files.createFile(path)
          files.deleteTree(path)
          assert(!files.exists(path), "the file still exists")
      }

      it("should delete empty directories") {
        case (base, files) =>
          val path = base / "hi"
          files.createDirectory(path)
          files.deleteTree(path)
          assert(!files.exists(path), "the directory still exists")
      }

      it("should delete trees") {
        case (base, files) =>
          val path = base / "hi"
          files.createDirectories(path / "one" / "two" / "three")
          files.createFile(path / "one" / "two" / "three" / "four")
          files.deleteTree(path)
          assert(!files.exists(path), "the directory still exists")
      }

      it("should fail on paths that don't exist") {
        case (base, files) =>
          val path = base / "hi"
          intercept[NoSuchFileException] {
            files.deleteTree(path)
          }
      }

      it("should not traverse symlinks") {
        case (base, files) =>
          val node = "node"
          val tree = base / "tree"
          val link = base / "symlink"

          files.createDirectories(tree / node)
          files.createSymlink(link, tree)
          assert(files.exists(link / node), "the subdirectory is not accessible through the symlink")
          files.deleteTree(link)
          assert(files.exists(tree / node), "the subdirectory no longer exists after deleting the symlink")
      }
    }
  }
}
