
   D E L I N E A T E - R A S T E R    T 0    S V G    C O N V E R T E R


What is it?
-----------

Delineate is a Java Swing GUI for converting bitmap raster images to
SVG (Scalable Vector Graphics) using AutoTrace (http://autotrace.sf.net).
It displays SVG results using the Apache Batik SVG toolkit.
Input formats are TGA, PBM, PNM, PGM, PPM or BMP.

Delineate is distributed under the GNU General Public License, see LICENSE.txt

For more information about Delineate, see http://delineate.sourceforge.net



Installing
----------

* GNU/Linux (& Mac OS X) *

1) You will need to install AutoTrace on your machine.
Downloads and installation instructions can be found at:

  http://autotrace.sourceforge.net/

2) You will also need to have Java 1.4.1 or Java 1.4.2 installed:

  http://java.sun.com/

3) Set your JAVA_HOME variable to the location where you have Java installed.

4) Extract delineate-0.2.tar.gz on to your machine


* Windows *

1) You will need to install AutoTrace on your machine.
Downloads and installation instructions can be found at:

  http://autotrace.sourceforge.net/

2) Put the AutoTrace install directory on to your system Path.
In Windoze XP you can change the path by going to:

  Start->Settings->Control Panel->System->Advanced->Environment Variables

Edit the Path variable, and append the AutoTrace install directory on to the end.


2) You will also need to have Java 1.4.1 or Java 1.4.2 installed:

  http://java.sun.com/

3) Set your JAVA_HOME environment variable to the location where you have Java installed.
In Windoze XP you can add this environement by going to:

  Start->Settings->Control Panel->System->Advanced->Environment Variables

4) Extract delineate-0.2.zip on to your machine



Running
-------

* GNU/Linux (& Mac OS X) *

 cd delineate/0.2

 chmod a+x delineate.sh

 ./delineate.sh


* Windows *

  Go to the directory <install dir>/delineate/0.2
  run delineate.bat



Change Log
----------

* From 0.1 to 0.2 *

- Key bindings and popup menu for zoom, scroll, reset and view source actions.
- Ability to save, load and delete settings configurations.
- Status bar for each of the two SVG result panels.
- File browser dialog for input and output file selection.
- File size information displayed.
- Color chooser dialog for background color selection.

