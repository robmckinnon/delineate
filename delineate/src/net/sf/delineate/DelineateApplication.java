/*
 * DelineateApplication.java - GUI for converting raster images to SVG using AutoTrace
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
package net.sf.delineate;

import net.sf.delineate.gui.SettingsPanel;
import net.sf.delineate.gui.SvgViewerController;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * GUI for converting raster images to SVG using AutoTrace
 * @author robmckinnon@users.sourceforge.net
 */
public class DelineateApplication {
    private static final String CONVERT__IMAGE__ACTION = "Convert";
    private static final JFrame frame = new JFrame("Delineate - raster to SVG converter");
    private SvgViewerController svgViewerController;

    public DelineateApplication(String parameterFile) throws Exception {
        final SettingsPanel settingsPanel = new SettingsPanel(parameterFile);

        svgViewerController = new SvgViewerController();

        JButton button = initConvertButton(settingsPanel, svgViewerController);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);

        JPanel controlPanel = createControlPanel(settingsPanel, buttonPanel);
//        JMenuBar menuBar = createMenuBar(svgViewerController);

        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(menuBar, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.EAST);
        panel.add(svgViewerController.getSvgViewerPanels());

        frame.setContentPane(panel);
        ImageIcon image = new ImageIcon("img/icon.png");
        frame.setIconImage(image.getImage());
        frame.setBounds(130, 30, 800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

//    private JMenuBar createMenuBar(final SvgViewerController svgViewerController) {
//        JMenuBar menuBar = new JMenuBar();
//        menuBar.add(svgViewerController.getSvgViewerMenu());
//        return menuBar;
//    }

    private JPanel createControlPanel(final SettingsPanel settingsPanel, JPanel buttonPanel) {
        JPanel controlPanel = new JPanel(new SpringLayout());
        controlPanel.add(settingsPanel.getPanel());
        controlPanel.add(buttonPanel);
        SpringUtilities.makeCompactGrid(controlPanel, 2, 1, 2, 2, 2, 2);
        JPanel controlWrapperPanel = new JPanel();
        controlWrapperPanel.add(controlPanel);
        return controlWrapperPanel;
    }

    private JButton initConvertButton(final SettingsPanel settingsPanel, final SvgViewerController viewerController) {
        JPanel panel = settingsPanel.getPanel();
        ActionMap actionMap = panel.getActionMap();
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        actionMap.put(CONVERT__IMAGE__ACTION, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                convert(settingsPanel, viewerController);
            }
        });

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        inputMap.put(keyStroke, CONVERT__IMAGE__ACTION);

        JButton button = new JButton(actionMap.get(CONVERT__IMAGE__ACTION));
        button.setText("Run");
        button.setMnemonic(KeyEvent.VK_R);
        return button;
    }

    private void convert(final SettingsPanel settingsPanel, final SvgViewerController viewerController) {
        if(settingsPanel.inputFileExists()) {
            viewerController.movePreviousSvg();

            String command = settingsPanel.getCommand();
            svgViewerController.setStatus("Converting...");
            System.out.println(command);

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                String outputFile = settingsPanel.getOutputFile();
                viewerController.load("file:" + outputFile);
                settingsPanel.updateFileSize();
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            showMessage("Input file does not exist.", "Invalid input file");
            settingsPanel.selectInputTextField();
        }
    }

    public static void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String args[]) throws Exception {
        new DelineateApplication(args[0]);
    }


}
