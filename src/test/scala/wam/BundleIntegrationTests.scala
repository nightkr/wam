package wam

import org.scalatest.{BeforeAndAfterAll, Outcome, fixture}
import wam.Bundle.BundleArchive
import java.io.BufferedInputStream
import resource._
import java.nio.file.{StandardCopyOption, Path, Files}

class BundleIntegrationTests extends fixture.FunSpec with BeforeAndAfterAll {
  type FixtureParam = WamCtx

  override protected def withFixture(test: OneArgTest): Outcome = {
    val tempDir = path("test_wamdir")
    val ctx = WamCtx(tempDir / "wow", tempDir / "repo")

    try {
      Files.createDirectories(tempDir)
      Files.createDirectories(ctx.enabledAddOnsDir)
      Files.createDirectories(ctx.repository)
      withFixture(test.toNoArgTest(ctx))
    } finally {
      WamFiles.deleteTree(tempDir)
    }
  }

  implicit class BundleTestTools(val bundle: Bundle) {
    def fakeInstall()(implicit ctx: WamCtx) {
      Files.createDirectories(bundle.registryPath)
      Files.createDirectories(Module("module_a").registryPath(bundle))
      Files.createDirectories(Module("module_b").registryPath(bundle))
    }
  }

  override protected def beforeAll() {
    archiveFile = Files.createTempFile("wam", null)

    for {
      src <- managed(this.getClass.getClassLoader.getResourceAsStream("TellMeWhen-6.2.6.zip"))
      bufSrc <- managed(new BufferedInputStream(src))
    } {
      Files.copy(bufSrc, archiveFile, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  override protected def afterAll() {
    Files.delete(archiveFile)
    archiveFile = null
  }

  var archiveFile: Path = null

  describe("A BundleRef") {
    val bundle = Bundle("MyBundle", Version("1.0"))

    describe("when not installed") {
      it("should not be installable") {
        implicit ctx =>
          assert(!bundle.installable, "the bundle does not consider itself installable")

          intercept[Bundle.NotInstallable] {
            bundle.install()
          }
      }

      it("should be uninstallable") {
        implicit ctx =>
          bundle.uninstall()
          assert(!bundle.installed, "the bundle still considers itself installed")
      }

      it("should not consider itself installed") {
        implicit ctx =>
          assert(!bundle.installed, "the bundle considers itself installed")
      }

      it("should detect no modules") {
        implicit ctx =>
          assert(bundle.modules === Set())
      }
    }

    describe("when installed") {
      it("should be installable") {
        implicit ctx =>
          bundle.fakeInstall()

          assert(bundle.installable, "the bundle does not consider itself installable")
          bundle.install()
          assert(bundle.installed, "the bundle does not consider itself installed")
      }

      it("should be uninstallable") {
        implicit ctx =>
          bundle.fakeInstall()

          bundle.uninstall()
          assert(!bundle.installed, "the bundle still considers itself installed")
      }

      it("should consider itself installed") {
        implicit ctx =>
          bundle.fakeInstall()

          assert(bundle.installed, "the bundle does not consider itself installed")
      }

      it("should detect the correct modules") {
        implicit ctx =>
          bundle.fakeInstall()

          assert(bundle.modules === Set(Module("module_a"), Module("module_b")))
      }
    }
  }

  describe("A BundleArchive") {
    describe("for a valid archive file") {
      def bundle: Bundle = BundleArchive("TellMeWhen", Version("6.2.6"), ZipArchive(archiveFile))

      describe("when not installed") {
        it("should be installable") {
          implicit ctx =>
            assert(bundle.installable, "the bundle does not consider itself installable")
            bundle.install()
            assert(bundle.installed, "the bundle still does not consider itself installed")

            // Verify a random selection of files exist
            val checkFiles = Seq(
              bundle.registryPath / "TellMeWhen" / "CHANGELOG.txt",
              bundle.registryPath / "TellMeWhen" / "LDB.lua",
              bundle.registryPath / "TellMeWhen_Options" / "TellMeWhen_Options.toc"
            )
            assert(checkFiles.forall(Files.exists(_)), "not all files were extracted properly")
        }
        it("should be uninstallable") {
          implicit ctx =>
            bundle.uninstall()
            assert(!bundle.installed, "the bundle still considers itself installed")
        }
        it("should not consider itself installed") {
          implicit ctx =>
            assert(!bundle.installed, "the bundle considers itself installed")
        }
        it("should detect the correct modules") {
          implicit ctx =>
            assert(bundle.modules === Set(Module("TellMeWhen"), Module("TellMeWhen_Options")))
        }
      }
    }

    describe("for a useless archive file") {
      def bundle: Bundle = BundleArchive("MyAddOn", Version("1.0.0"), new Archive {
        def extract(target: Path) {
          throw new NotImplementedError
        }

        def modules = throw new NotImplementedError

        def exists = true
      })

      it("should clean up the failed installation") {
        implicit ctx =>
          assert(bundle.installable, "the bundle does not consider itself installable")
          intercept[NotImplementedError] {
            bundle.install()
          }
          assert(!bundle.installed, "the bundle did not clean up after the failed installation")
      }
    }
  }
}
