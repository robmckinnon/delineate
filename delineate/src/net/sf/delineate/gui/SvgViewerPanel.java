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
package net.sf.delineate.gui;

import net.sf.delineate.DelineateApplication;
import net.sf.delineate.utility.ColorUtilities;
import net.sf.delineate.utility.FileUtilities;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGSVGElement;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Panel for viewing SVG files.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgViewerPanel {

    public static final String VIEW_SOURCE_ACTION = "ViewSource";

    private final JSVGCanvas svgCanvas = new ScrollableJSVGCanvas();
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel sizeLabel = new JLabel("");
    private JPopupMenu popupMenu = new JPopupMenu();
    private int modifier;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private JPanel viewerPanel;
    private ActionMap controllerActionMap;
    private final ViewSourceAction viewSourceAction = new ViewSourceAction();
    private String uri;
    private boolean extractStyles = false;
    private int pathCount = 0;
    private DelineateApplication.ConversionListener listener;
    private boolean optimize = false;
    private String background;
    private boolean centerlineEnabled;
    private Map colorToStyleMap = new HashMap();
    private Set colorSet = new HashSet();

    public SvgViewerPanel(String resultText, int modifier) {
        this.modifier = modifier;
        installListeners(resultText);
        installActions();
        viewerPanel = createViewerPanel();
    }

    public JPanel getViewerPanel() {
        return viewerPanel;
    }


    public void closeViewSourceFrame() {
        viewSourceAction.closeFrame();
        viewSourceAction.setSourceUrl(null);
    }

    public int getPathCount() {
        return pathCount;
    }

    public void setPathCount(int pathCount) {
        this.pathCount = pathCount;
    }

    private void installActions() {
        InputMap inputMap = svgCanvas.getInputMap();
        KeyStroke[] keys = inputMap.keys();

        for(int i = 0; i < keys.length; i++) {
            KeyStroke key = keys[i];
            inputMap.remove(key);
        }

        ActionMap actionMap = svgCanvas.getActionMap();
        actionMap.put(SvgViewerPanel.VIEW_SOURCE_ACTION, viewSourceAction);
    }

    private void installListeners(final String resultText) {
        svgCanvas.addMouseListener(new PopupListener(popupMenu));

        svgCanvas.addSVGDocumentLoaderListener(new
            SVGDocumentLoaderAdapter() {
                public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                    sizeLabel.setText("");
                    setStatus("Loading...");
                }

                public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                    if(background != null) {
                        SVGDocument document = e.getSVGDocument();
                        SVGSVGElement root = document.getRootElement();

                        Element rectangle = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect");
                        rectangle.setAttributeNS(null, "width", root.getWidth().getBaseVal().getValueAsString());
                        rectangle.setAttributeNS(null, "height", root.getHeight().getBaseVal().getValueAsString());
                        rectangle.setAttributeNS(null, "fill", '#' + background);

                        Node firstChild = root.getFirstChild();
                        root.insertBefore(rectangle, firstChild);
                    }
                }
            });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                setStatus("Interpreting...");
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                setStatus("Rendering...");
            }


            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                final File file = FileUtilities.getFile(uri);

                if(optimize) {
                    setStatus("Optimizing...");
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            optimize(file);
                            optimize = false;
                            finishConversion(file, resultText);
                        }
                    });
                } else {
                    finishConversion(file, resultText);
                }
            }

            private void finishConversion(File file, String resultText) {
                ViewSourceAction viewSourceAction = (ViewSourceAction)getAction(VIEW_SOURCE_ACTION);
                viewSourceAction.setSourceUrl(uri);
                Container ancestor = svgCanvas.getTopLevelAncestor();
                int top = ancestor.getInsets().top;
                viewSourceAction.setLocation(ancestor.getX() + (top / 2), ancestor.getY() + top);

                String fileSize = FileUtilities.getFileSize(file);
                setStatus(resultText + file.getName());
                sizeLabel.setText(pathCount + " paths - " + fileSize);

                if(listener != null) {
                    listener.conversionFinished();
                }
            }
        });

    }


    private void optimize(File file) {
        try {
            long start = System.currentTimeMillis();

            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file.getPath())));

            Map styleToColorMap = new HashMap();
            List styleList = new LinkedList();

            if(extractStyles) {
                styleToColorMap = new HashMap();
                styleList = new LinkedList();
            }

            SVGSVGElement rootElement = getSvgDocument().getRootElement();
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

        Color[] colors = (Color[])colorSet.toArray(new Color[colorSet.size()]);
        listener.setColors(colors);

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

    public void setStatus(String text) {
        statusLabel.setText(text);
        statusLabel.repaint();
    }

    public Action getAction(String actionKey) {
        return svgCanvas.getActionMap().get(actionKey);
    }

    public void setURI(String uri) {
        setSvgDocument(uri, null);  // hack to prevent problem loading relative URI
        svgCanvas.setURI(uri);
    }

    public void setSvgDocument(String uri, SVGDocument svgDocument) {
        this.uri = uri;
        svgCanvas.setSVGDocument(svgDocument);
    }

    public JScrollBar getHorizontalScrollBar() {
        return horizontalScrollBar;
    }

    public JScrollBar getVerticalScrollBar() {
        return verticalScrollBar;
    }

    public void addAdjustmentListener(AdjustmentListener scrollListener) {
        horizontalScrollBar.addAdjustmentListener(scrollListener);
        verticalScrollBar.addAdjustmentListener(scrollListener);
    }

    private JPanel createViewerPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(sizeLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(svgCanvas);

        horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        verticalScrollBar = scrollPane.getVerticalScrollBar();

        panel.add(scrollPane);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void addMenuItem(Action action, KeyStroke keyStroke) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setText((String)action.getValue(Action.NAME));
        menuItem.setAccelerator(keyStroke);
        popupMenu.add(menuItem);
    }

    public void addSeparator() {
        popupMenu.addSeparator();
    }

    public JMenuItem getMenuItem(String text, String actionKey, int shortcutKey) {
        Action action = getAction(actionKey);

        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setText(text);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcutKey, modifier);
        menuItem.setAccelerator(keyStroke);
        return menuItem;
    }

    public void setControllerActionMap(ActionMap controllerActionMap) {
        this.controllerActionMap = controllerActionMap;
    }

    private void setActionsEnabled(boolean enabled) {
        Object[] keys = controllerActionMap.allKeys();
        for(int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            controllerActionMap.get(key).setEnabled(enabled);
        }
    }

    public SVGDocument getSvgDocument() {
        return svgCanvas.getSVGDocument();
    }

    public void setExtractStyles(boolean extractStyles) {
        this.extractStyles = extractStyles;
    }

    public void addConversionListener(DelineateApplication.ConversionListener listener) {
        this.listener = listener;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public void setBackgroundColor(String color) {
        background = color;
    }

    public void setCenterlineEnabled(boolean enabled) {
        centerlineEnabled = enabled;
    }

    private class PopupListener extends MouseAdapter {
        private JPopupMenu popupMenu;

        public PopupListener(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if(e.isPopupTrigger()) {
                SVGDocument svgDocument = svgCanvas.getSVGDocument();

                if(svgDocument == null) {
                    setActionsEnabled(false);
                } else {
                    setActionsEnabled(true);
                    ViewSourceAction viewSourceAction = (ViewSourceAction)getAction(VIEW_SOURCE_ACTION);
                    viewSourceAction.setLocation(e.getX(), e.getY());
                }

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}