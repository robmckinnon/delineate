/*
 * SvgViewerController.java
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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import java.io.File;

/**
 * Controls the SVG viewer panels.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgViewerController {
    private String uri;

    private SvgViewerPanel svgViewerB;
    private SvgViewerPanel svgViewerA;

    private JPanel panel = new JPanel(new SpringLayout());

    public SvgViewerController(JFrame frame) {
        svgViewerA = new SvgViewerPanel(false);
        svgViewerB = new SvgViewerPanel(true);

        panel.add(svgViewerA.getViewerPanel());
        panel.add(svgViewerB.getViewerPanel());
//        panel.setBorder(BorderFactory.createTitledBorder("Result SVG"));
        panel.setBorder(BorderFactory.createEmptyBorder());

        SpringUtilities.makeCompactGrid(panel, 2, 1, 1, 1, 4, 4);
    }

    public JComponent getSvgViewerPanels() {
        return panel;
    }

    public void movePreviousSvg() {
        if(uri != null) {
            File file = FileUtilities.getFile(uri);
            File previousFile = new File(file.getParent(), file.getName() + '~');
            previousFile.delete();
            file.renameTo(previousFile);
            svgViewerB.setURI(uri + '~');
        }
    }

    public void load(String uri) {
        this.uri = uri;
        System.out.println("loading " + uri);
        svgViewerA.setURI(uri);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Batik");
        SvgViewerController app = new SvgViewerController(frame);
        frame.setContentPane(app.getSvgViewerPanels());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setBounds(200, 100, 400, 400);
        frame.setVisible(true);
    }

}