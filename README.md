zipstarter
==========

This is a demonstration project to show you, how you can enable every zip archive to be executed via
`java -jar <zip-archive>`. The name of the zip-file or its extension doesn't really matter. So it can be
a war, a zip, a jar or a file without any extension. It is useful if you want to write comand line tools in java
(eg. I wrote a command line tool to read and write into JMS queues). Or if you want to add some admin comandline
functionality to your war file (like creating encrypted passwords using Jasypt etc.).

How has the archive to look like:

* it has to be a zip file (doh)
* it has to have a manifest file with the correct entries (see below)
* it has to have the class files of the /boot directory from the zipStarter project

What to do to use it
--------------------
You need to create the archive within you build as usual. Then add the output of the zipStarter project to the root
of your zip content (it it is a war file, make sure the /boot directory is not accessable via the webApp).
Note that both classes from the zipStarter project have inner classes, so that you have to add **4 class files**.
After that add the necessary entries to the manifest file. Three entries are necessary:

### Main-Class ###

must be 'boot.ZipStarter'

### ZipStarter-Class-Path ###

this is a class path for the zip file, following the same syntax as the manifest Class-Path entry (' ' is delimeter,
' ' at the beginning of a continued line etc.). Its entries can be

1. _directories_: the content of every jar inside this directory will be part of the class path. Additional every class
 starting with its package structure at this directory will be part of the class path.
2. _jars_: the content of the jar will be part of the class path

### ZipStarter-Main-Class ###

this is your class containing a main() function that you want to start up when `java -jar <zip-file>` is called

Zip Content
-----------
the zip file should look like this:

    <zip>
      |
      +- META-INF
      |     |
      |     +- MANIFEST.MF
      |
      +- boot
      |    |
      |    +- ZipStarter$1.class
      |    |
      |    +- ZipStarter.class
      |    |
      |    + zip
      |       |
      |       +- Handler$1.class
      |       |
      |       +- Handler.class
      |
      ... the rest of you zip file

Note: Some java functions assume, that the first file in a jar is the META-INF/MANIFEST.MF file. AFAIK this is not part
of the JAR spec, but eg. `JarStream.getManifest()` is expecting this and the jar tool of java enforces it. It seems to
work fine if MANIFEST.MF is not the first file in the JAR, but be aware of this fact

MANIFEST.MF content
-------------------
please see the example projects for real life examples. The manifest should have (among optional others entries) this:

    Main-Class: boot.ZipStarter
    ZipStarter-Class-Path: <1st directory> <2nd directoyr> <3rd jar-file>
    ZipStarter-Main-Class: <fully qualified name of your class with the main function>


Eample zipExample
-----------------
This is just an example to show you, how you can enable a simple zip file to be an java executable. I've spiced up
the usual hello world example by reading the text from a resource out of the archive.

Example warExample
------------------
This is an example of showing you how you can enable a war file to be an java executable. For this example I am reusing
the war structure for the class path and I am starting up a limited part of the spring context from the command line.
This is great to add some command line functionality to a war.
