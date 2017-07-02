import sbtprotobuf.ProtobufTestPlugin.{Keys => PBT}

scalaVersion := "2.10.6"

crossScalaVersions += "2.11.11"

enablePlugins(ProtobufPlugin, ProtobufTestPlugin)

version in protobufConfig := "3.3.1"

version in PBT.protobufConfig := (version in protobufConfig).value

libraryDependencies += "com.google.protobuf" % "protobuf-java" % (version in protobufConfig).value % protobufConfig.name

libraryDependencies += "com.google.protobuf" % "protobuf-java" % (version in PBT.protobufConfig).value % PBT.protobufConfig.name

protobufRunProtoc in protobufConfig := { args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v330" +: args.toArray)
}

PBT.protobufRunProtoc in PBT.protobufConfig := (protobufRunProtoc in protobufConfig).value

excludeFilter in protobufConfig := "test1.proto"

excludeFilter in PBT.protobufConfig := "test3.proto"

unmanagedResourceDirectories in Compile += (sourceDirectory in protobufConfig).value

unmanagedResourceDirectories in Test += (sourceDirectory in PBT.protobufConfig).value

TaskKey[Unit]("checkJar") := {
  val compileJar = (packageBin in Compile).value
  val testJar = (packageBin in Test).value

  IO.withTemporaryDirectory{ dir =>
    val files = IO.unzip(compileJar, dir, "*.proto")
    val expect = Set("test1.proto", "test2.proto").map(dir / _)
    val testfiles = IO.unzip(testJar, dir, "*.proto")
    val testexpect = Set("test3.proto", "test4.proto").map(dir / _)
    assert(files == expect, s"$files $expect")
    assert(testfiles == testexpect, s"$testfiles $testexpect")
  }
}

// https://github.com/sbt/sbt-protobuf/issues/37
mainClass in compile := Some("whatever")
