/*
 * SvgViewerPanel.java
 *
 * Copyright (C) 2003 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.delineate.utility;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Panel for viewing SVG files.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgOptimizer {

    public static final String VIEW_SOURCE_ACTION = "ViewSource";

    private boolean extractStyles = false;
    private int pathCount = 0;
    private String background;
    private boolean centerlineEnabled;
    private Map colorToStyleMap = new HashMap();
    private Set colorSet = new HashSet();
    private Color[] colors;

    public int getPathCount() {
        return pathCount;
    }

    public void setPathCount(int pathCount) {
        this.pathCount = pathCount;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setBackground(SVGDocument document) {
        if(background != null) {
            SVGSVGElement root = document.getRootElement();
            Element rectangle = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect");
            rectangle.setAttributeNS(null, "width", root.getWidth().getBaseVal().getValueAsString());
            rectangle.setAttributeNS(null, "height", root.getHeight().getBaseVal().getValueAsString());
            rectangle.setAttributeNS(null, "fill", '#' + background);

            Node firstChild = root.getFirstChild();
            root.insertBefore(rectangle, firstChild);
        }
    }

    public void optimize(File file, SVGDocument svgDocument) {
        try {
            long start = System.currentTimeMillis();

            SVGSVGElement rootElement = svgDocument.getRootElement();
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file.getPath())));

            Map styleToColorMap = new HashMap();
            List styleList = new LinkedList();

            if(extractStyles) {
                styleToColorMap = new HashMap();
                styleList = new LinkedList();
            }

            writeDocumentStart(w, rootElement);
            if(centerlineEnabled) {
                w.println("<g fill=\"none\">");
            } else {
                w.println("<g stroke=\"none\">");
            }
            pathCount = writePaths(rootElement, styleList, styleToColorMap, w);
            w.println("</g>");
            if(extractStyles) {
                writeStyles(w, styleList, styleToColorMap);
            }

            w.println("</svg>");
            w.flush();
            w.close();

            System.out.println("optimizing took " + (System.currentTimeMillis() - start));

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStyles(PrintWriter w, List codeList, Map codeToFillMap) {
        w.println("<defs>");
        w.println("<style type=\"text/css\"><![CDATA[");

        Iterator iterator = codeList.iterator();

        while(iterator.hasNext()) {
            String name = (String)iterator.next();
            w.print(".");
            w.print(name);
            if(centerlineEnabled) {
                w.print("{stroke:#");
            } else {
                w.print("{fill:#");
            }
            w.print(codeToFillMap.get(name).toString());
            w.println("}");
        }
        w.println("]]></style>");
        w.println("</defs>");
    }

    private int writePaths(SVGSVGElement rootElement, List styleList, Map styleToColorMap, PrintWriter w) {
        NodeList childNodes = rootElement.getChildNodes();
        int pathCount = 0;
        int styleCount = 0;
        String colorText = null;
        colorSet.clear();

        if(extractStyles) {
            colorToStyleMap.clear();
        }

        for(int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if(node instanceof SVGPathElement) {
                pathCount++;
                SVGPathElement path = (SVGPathElement)node;

                if(centerlineEnabled) {
                    colorText = path.getAttribute("style").substring(8, 14);
                } else {
                    colorText = path.getAttribute("style").substring(6, 12);
                }

                if(pathCount <= 279 && !colorSet.contains(colorText)) {
                    Color color = ColorUtilities.getColor(colorText);
                    colorSet.add(color);
                }

                if(extractStyles) {
                    if(!colorToStyleMap.containsKey(colorText)) {
                        String style = getStyleName(styleCount);
                        styleList.add(style);
                        colorToStyleMap.put(colorText, style);
                        styleToColorMap.put(style, colorText);
                        styleCount++;
                    }

                    w.print("<path class=\"");
                    w.print((String)colorToStyleMap.get(colorText));
                } else {
                    if(centerlineEnabled) {
                        w.print("<path stroke=\"#");
                    } else {
                        w.print("<path fill=\"#");
                    }
                    w.print(colorText);
                }

                w.print("\" d=\"");
                String pathText = path.getAttribute("d");
                int index = pathText.length() - 1;
                char c = pathText.charAt(index);

                if(c == 'z') {
                    do {
                        c = pathText.charAt(--index);
                    } while(Character.isDigit(c) || Character.isWhitespace(c));

                    if(c == 'L') {
                        pathText = pathText.substring(0, index) + 'z';
                    }
                }
                w.print(pathText);
                w.println("\"/>");
            }
        }

        colors = (Color[])colorSet.toArray(new Color[colorSet.size()]);

        return pathCount;
    }

    private void writeDocumentStart(PrintWriter w, SVGSVGElement rootElement) {
        String width = rootElement.getWidth().getBaseVal().getValueAsString();
        String height = rootElement.getHeight().getBaseVal().getValueAsString();

        w.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        w.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");

        w.print("<svg ");
        writeWidthAndHeight(w, width, height);
        w.println(">");

        if(background != null) {
            w.print("<rect fill=\"#");
            w.print(background);
            w.print("\" ");
            writeWidthAndHeight(w, width, height);
            w.println("/>");
        }
    }

    private void writeWidthAndHeight(PrintWriter w, String width, String height) {
        w.print("width=\"");
        w.print(width);
        w.print("\" height=\"");
        w.print(height);
        w.print("\"");
    }

    private String getStyleName(int i) {
        int first = i / 51;

        if(first == 0) {
            if(i < 26) {
                return (new Character((char)(i + 97))).toString();
            } else {
                return (new Character((char)(i + 39))).toString();
            }
        } else {
            return getStyleName(first - 1) + getStyleName(i % 51);
        }
    }

    public void setExtractStyles(boolean extractStyles) {
        this.extractStyles = extractStyles;
    }

    public void setBackgroundColor(String color) {
        background = color;
    }

    public void setCenterlineEnabled(boolean enabled) {
        centerlineEnabled = enabled;
    }

}