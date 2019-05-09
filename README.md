[![Build Status](https://travis-ci.org/SimianQuant/sbt-jni.svg?branch=master)](https://travis-ci.org/SimianQuant/sbt-jni)
[![Build status](https://ci.appveyor.com/api/projects/status/ubaj4l9yv4e00fue?svg=true)](https://ci.appveyor.com/project/harshad-deo/sbt-jni)

sbt-jni
===

A suite of plugins to simplify the creation and distribution of programs that use Java Native Interface (JNI). This is forked
from [sbt-jni](https://github.com/jodersky/sbt-jni/tree/master/plugin/src) to support Windows builds.

## To Use 

The plugin only supports sbt 1.x. To use, add the following line to `project/plugins.sbt`:

```scala
addSbtPlugin("com.simianquant" % "sbt-jni" % "0.1.0")
```

## Dependencies

The plugin uses [CMake](https://cmake.org/) to build the native implementations. 

1. On Linux, the default `Makefile` generator is used and no additional configuration, post installation of CMake, should be necessary
1. On Windows, the `NMake Makefile` generator is used. `NMake` is distributed as a part of Visual Studio. The MSVC toolset should 
be available on the path. Instructions on how to do it can be found [here](https://docs.microsoft.com/en-us/cpp/build/building-on-the-command-line?view=vs-2019). 

Additionally, `javah` should be on the path.

## Plugin Summary

| Plugin     | Description                                                                                           |  Enabled  |
| ---------- | ----------------------------------------------------------------------------------------------------- | --------- |
| JniJavah   | Adds support for generating headers from classfiles that have `@native` methods.                      | automatic |
| JniLoad    | Makes `@nativeLoader` annotation available, that injects code to transparently load native libraries. | automatic |
| JniNative  | Adds sbt wrapper tasks around native build tools to ease building and integrating native libraries.   | manual    |
| JniPackage | Packages native libraries into multi-platform fat jars. No more manual library installation!          | automatic |

### JniJavah

This plugin wraps the JDK `javah` command.

Run `sbt-javah` to generate C header files with prototypes for any methods marked as native. For example, the Scala class

```scala
package org.example
class Adder(val base: Int) {
  @native def plus(term: Int): Int // implemented in a native library
}
```

will yield this prototype
```c
/*
 * Class:     org_example_Adder
 * Method:    plus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_example_Adder_plus
  (JNIEnv *, jobject, jint);
```

The header output directory can be configured

```scala
target in javah := <dir> // defaults to target/native/include
```

Note that native methods declared both in Scala and Java are supported. Whereas Scala uses the `@native` annotation, Java uses the
`native` keyword.

### JniLoad

This plugin enables loading native libraries in a safe and transparent manner to the developer (no more explicit, static `System.load("library")` calls required). It does so by providing a class annotation which injects native loading code to all its annottees. Furthermore, in case a native library is not available on the current `java.library.path`, the code injected by the annotation will fall back to loading native libraries packaged according to the rules of `JniPackage`.

For example:

```scala
import ch.jodersky.jni.nativeLoader

// By adding this annotation, there is no need to call
// System.load("adder0") before accessing native methods.
@nativeLoader("adder0")
class Adder(val base: Int) {
  @native def plus(term: Int): Int // implemented in libadder0.so
}

// The application feels like a pure Scala app.
object Main extends App {
  (new Adder(0)).plus(1)
}
```

Note: this plugin is just a shorthand for adding `sbt-jni-macros` (the project in `macros/`) and the scala-macros-paradise projects as provided dependencies.

See the [annotation's implementation](macros/src/main/scala/ch/jodersky/jni/annotations.scala) for details about the injected code.

### JniNative

JniNative adds the capability of building native code (compiling and linking) to sbt by interfacing with CMake. The implementation of
this plugin is the main difference this fork and the original. The initial CMake configuration can be obtained by running `sbt nativeInit <tool>`. After this, projects are built by calling the `sbt nativeCompile` task.

Source and output directories are configurable:

```scala
sourceDirectory in nativeCompile := sourceDirectory.value / "native",
target in nativeCompile := target.value / "native" / (nativePlatform).value,
```

### JniPackage

This plugin packages native libraries produced by JniNative in a way that they can be transparently loaded with JniLoad. It uses the notion of a native "platform", as defined in [this file](https://github.com/SimianQuant/sbt-jni/blob/master/util/src/main/scala/ch/jodersky/sbt/jni/util/OsAndArch.scala).

## Canonical Use

*Keep in mind that sbt-jni is a __suite__ of plugins, there are many other use cases. This is a just a description of the simplest one.*

1. Define separate sub-projects for JVM and native sources. In `myproject/build.sbt`:

   ```scala
   lazy val core = project in file("myproject-core"). // regular scala code with @native methods
     dependsOn(native % Runtime) // remove this if `core` is a library, leave choice to end-user

   lazy val native = project in file("myproject-native"). // native code and build script
     enablePlugin(JniNative) // JniNative needs to be explicitly enabled
   ```
   Note that separate projects are not strictly required. They are strongly recommended nevertheless, as a portability-convenience tradeoff: programs written in a JVM language are expected to run anywhere without recompilation, but including native libraries in jars limits this portability to only platforms of the packaged libraries. Having a separate native project enables the users to easily swap out the native library with their own implementation.

2. Initialize the native build tool from a template:

   Run `sbt "nativeInit <libname>"`

3. Implement core project:

   This step is identical to building a regular scala project, with the addition that some classes will also contain `@native` methods.

4. Generate native headers:

   Run `sbt javah`

5. Implement native headers:

   The function prototypes in the header files must be implemented in native code (such as C, C++) and built into a shared library. Run `sbt nativeCompile` to call the native build tool and build a shared library.

6. Build/run/test:

   At this point, the project can be tested and run as any standard sbt project. For example, you can publish your project as a library (`sbt publish`), run it (`sbt core/run`) or simply run unit tests (`sbt test`). Packaging and recompiling of the native library will happen transparently.

7. Develop:

   The usual iterative development process. Nothing speial needs to be done, except in case any `@native` methods are added/removed or their signature changed, then `sbt javah` needs to be run again.

## Examples
The [plugins' unit tests](plugin/src/sbt-test/sbt-jni) offer some simple examples.

## License
This project is released under the terms of the 3-clause BSD license. See LICENSE for details.
