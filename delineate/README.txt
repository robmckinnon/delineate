
D E L I N E A T E   -   R A S T E R    T 0    S V G    C O N V E R T E R


What is it?
-----------

Delineate is a Java Swing GUI tool for converting bitmap raster images
to SVG (Scalable Vector Graphics) using AutoTrace. It displays SVG 
results using the Apache Batik SVG toolkit. Input formats are TGA, PBM, 
PNM, PGM, PPM or BMP.

Delineate is distributed under the GNU General Public License, see
LICENSE.txt

For more information about Delineate, see
http://delineate.sourceforge.net


Installing
----------

To install, see INSTALL.txt


Change Log
----------

From 0.2 to 0.3

- Reduced output file sizes, by changing SVG produced by AutoTrace to be
  more concise.
  - Use single 'g' element to set stroke=none for all paths.
  - Use 'fill' attribute instead of 'style' attribute in paths.
  - Remove the last unneeded "line to" command in each path.
  
  For example SVG from AutoTrace like this:
  <svg width="350" height="318">
  <path style="fill:#ded6aa; stroke:none;" d="M11 0L11 9L12 9L11 0z"/>
  <path style="fill:#685a37; stroke:none;" d="M12 0L2 12L13 12L12 0z"/>
  </svg>
  
  Is converted by Delineate to this:
  <svg width="350" height="318">
  <g stroke="none">
  <path fill="#ded6aa" d="M11 0L11 9L12 9z"/>
  <path fill="#685a37" d="M12 0L2 12L13 12z"/>
  </g>
  </svg>

- Optionally SVG style definitions can be created in the result SVG.
  This may reduce file size further when there are many paths and a
  limited number of colors.
  
  For example, if turned on, the SVG above would become:
  <svg width="350" height="318">
  <g stroke="none">
  <path class="a" d="M11 0L11 9L12 9z"/>
  <path class="b" d="M12 0L2 12L13 12z"/>
  </g>
  <defs>
  <style type="text/css"><![CDATA[
  .a{fill:#ded6aa}
  .b{fill:#685a37}
  ]]></style>
  </defs>
  </svg>
   
- Split panes now allow the previous result view and/or the settings
  panel to be hidden.
- Can see the number of paths in result SVG, next to the file size in
  the status bars.
- Put zoom in onto the popup menu, it was meant to be there in v0.2.
- Fixed bug that occurred when trying to convert when a view source
  dialog was open.
- Display warning dialog if trying to convert when autotrace is not
  installed.


From 0.1 to 0.2

- Key bindings and popup menu for zoom, scroll, reset and view source
  actions.
- Ability to save, load and delete settings configurations.
- Status bar for each of the two SVG result panels.
- File sizes are displayed.
- File browser dialog for input and output file selection.
- Color chooser dialog for background color selection.
