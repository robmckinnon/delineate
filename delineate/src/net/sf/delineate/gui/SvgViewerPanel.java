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

import net.sf.delineate.utility.FileUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * Panel for viewing SVG files.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgViewerPanel {

    private final JSVGCanvas svgCanvas = new ScrollableJSVGCanvas();
    private final JLabel statusLabel = new JLabel("Ready.");
    private final JLabel sizeLabel = new JLabel("");
    private ViewSourceAction viewSourceAction = new ViewSourceAction();

    public SvgViewerPanel(boolean isPreviousResult) {
        final String resultLabel = isPreviousResult ? "Previous result: " : "Result: ";

        svgCanvas.addSVGDocumentLoaderListener(new
            SVGDocumentLoaderAdapter() {
                public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                    sizeLabel.setText("");
                    statusLabel.setText("Loading...");
                }
            });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                statusLabel.setText("Interpreting...");
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                statusLabel.setText("Rendering...");
            }

            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                String uri = svgCanvas.getURI();
                File file = FileUtilities.getFile(uri);
                String fileSize = FileUtilities.getFileSize(file);
                statusLabel.setText(resultLabel + file.getName());
                sizeLabel.setText(fileSize);
            }
        });
    }

    public void setURI(String uri) {
        svgCanvas.setSVGDocument(null);  // hack to prevent problem loading relative URI
        svgCanvas.setURI(uri);
    }

    public JPanel getViewerPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(sizeLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(svgCanvas));
        panel.add(statusPanel, BorderLayout.SOUTH);

        JMenuItem menuItem = new JMenuItem(viewSourceAction);
        menuItem.setText("View source");
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(menuItem);

        MouseListener popupListener = getPopupListener(popupMenu);

        svgCanvas.addMouseListener(popupListener);

        return panel;
    }

    private MouseListener getPopupListener(final JPopupMenu popupMenu) {
        MouseListener popupListener = new MouseAdapter() {
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
                        viewSourceAction.setEnabled(false);
                    } else {
                        viewSourceAction.setSvgDocument(svgDocument);
                        viewSourceAction.setLocation(e.getX(), e.getY());
                        viewSourceAction.setEnabled(true);
                    }

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        return popupListener;
    }

    public JButton getViewSourceButton() {
        JButton button = new JButton(viewSourceAction);
        button.setText("View source");
        return button;
    }

}