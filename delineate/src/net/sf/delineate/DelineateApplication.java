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
import net.sf.delineate.utility.FileUtilities;
import net.sf.delineate.utility.GuiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * GUI for converting raster images to SVG using AutoTrace
 * @author robmckinnon@users.sourceforge.net
 */
public class DelineateApplication {
    private static final String CONVERT_IMAGE_ACTION = "Convert";
    private static final JFrame frame = new JFrame("Delineate - raster to SVG converter");
    private final SvgViewerController svgViewerController;

    public DelineateApplication(String parameterFile) throws Exception {
        final SettingsPanel settingsPanel = new SettingsPanel(parameterFile);

        svgViewerController = new SvgViewerController(new ConversionListener() {
            public void conversionFinished() {
                settingsPanel.updateFileSize();
                Action action = settingsPanel.getPanel().getActionMap().get(CONVERT_IMAGE_ACTION);
                action.setEnabled(true);
            }

            public void setColors(Color[] colors) {
                settingsPanel.setColors(colors);
            }
        });

        JButton button = initConvertButton(settingsPanel);
        JPanel optionsPanel = initOptionsPanel(svgViewerController);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);

        JPanel controlPanel = createControlPanel(settingsPanel, buttonPanel, optionsPanel);
//        JMenuBar menuBar = createMenuBar(svgViewerController);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, svgViewerController.getSvgViewerPanels(), controlPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1);

        frame.setContentPane(splitPane);
        ImageIcon image = new ImageIcon("img/delineate-icon.png");
        frame.setIconImage(image.getImage());
        frame.setBounds(130, 0, 800, 735);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel initOptionsPanel(final SvgViewerController viewerController) {
        final String NO_GROUPS = "leave styles on paths";
        final String SINGLE_GROUP = "group all paths in one group";
        final String COLOR_GROUPS = "group paths by color";
        final String STYLE_DEFS = "create style definitions";

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if(command == NO_GROUPS) {
                    viewerController.setExtractStyles(false);

                } else if(command == SINGLE_GROUP) {
                    viewerController.setExtractStyles(false);

                } else if(command == COLOR_GROUPS) {
                    viewerController.setExtractStyles(false);

                } else if(command == STYLE_DEFS) {
                    viewerController.setExtractStyles(true);
                }
            }
        };
        JRadioButton noGroupsRadio = initRadio(NO_GROUPS,
            "Style attribute is on each path element.", listener);
        JRadioButton singleGroupRadio = initRadio(SINGLE_GROUP,
            "Group all paths in one group element, which defines stroke style.", listener);
        JRadioButton colorGroupsRadio = initRadio(COLOR_GROUPS,
            "Create a separate group element for each path fill color.", listener);
        JRadioButton styleDefsRadio = initRadio(STYLE_DEFS,
            "Creates SVG style definitions, may reduce output file size if there are many paths and few colors. Use with the color count setting.", listener);

        noGroupsRadio.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(noGroupsRadio);
        buttonGroup.add(singleGroupRadio);
        buttonGroup.add(colorGroupsRadio);
        buttonGroup.add(styleDefsRadio);

        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Result options"));

        panel.add(noGroupsRadio);
        panel.add(singleGroupRadio);
        panel.add(colorGroupsRadio);
        panel.add(styleDefsRadio);

        SpringUtilities.makeCompactGrid(panel, 2, 2, 2, 2, 2, 2);
        return panel;
    }

    private JRadioButton initRadio(String text, String tooltip, ActionListener listener) {
        JRadioButton radio = new JRadioButton(text);
        radio.setToolTipText(tooltip);
        radio.setActionCommand(text);
        radio.addActionListener(listener);
        return radio;
    }

//    private JMenuBar createMenuBar(final SvgViewerController svgViewerController) {
//        JMenuBar menuBar = new JMenuBar();
//        menuBar.add(svgViewerController.getSvgViewerMenu());
//        return menuBar;
//    }

    private JPanel createControlPanel(final SettingsPanel settingsPanel, JPanel buttonPanel, JPanel optionsPanel) {
        JPanel controlPanel = new JPanel(new SpringLayout());
        controlPanel.add(settingsPanel.getPanel());
        controlPanel.add(optionsPanel);
        controlPanel.add(buttonPanel);
        SpringUtilities.makeCompactGrid(controlPanel, 3, 1, 6, 6, 6, 6);
        JPanel controlWrapperPanel = new JPanel();
        controlWrapperPanel.add(controlPanel);
        return controlWrapperPanel;
    }

    private JButton initConvertButton(final SettingsPanel settingsPanel) {
        JButton button = GuiUtilities.initButton("Run", CONVERT_IMAGE_ACTION, KeyEvent.VK_ENTER, 0, settingsPanel.getPanel(), new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                convert(settingsPanel);
            }
        });

        button.setMnemonic(KeyEvent.VK_R);
        return button;
    }

    private void convert(final SettingsPanel settingsPanel) {
        if(settingsPanel.inputFileExists()) {
            Action action = settingsPanel.getPanel().getActionMap().get(CONVERT_IMAGE_ACTION);
            action.setEnabled(false);
            svgViewerController.setStatus("Converting...");

            new Thread() {
                public void run() {
                    try {
                        final String outputFile = settingsPanel.getOutputFile();
                        svgViewerController.movePreviousSvg(outputFile);

                        String[] commandArray = settingsPanel.getCommandAsArray();
                        System.out.println(settingsPanel.getCommand());

                        Runtime runtime = Runtime.getRuntime();
                        Process process = runtime.exec(commandArray, null, null);
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        process.waitFor();

                        if(errorReader.ready()) {
                            StringBuffer buffer = new StringBuffer(errorReader.readLine());
                            while(errorReader.ready()) {
                                buffer.append('\n' + errorReader.readLine());
                            }
                            throw new RuntimeException(buffer.toString());
                        }

                        new BufferedInputStream(process.getErrorStream());
                        svgViewerController.setBackgroundColor(settingsPanel.getBackgroundColor());
                        svgViewerController.setCenterlineEnabled(settingsPanel.getCenterlineEnabled());

                        svgViewerController.load(FileUtilities.getUri(outputFile));
                    } catch(Exception e) {
                        e.printStackTrace();
                        if(e instanceof IOException) {
                            showMessage("You must install AutoTrace to run conversions.\n" +
                                "See INSTALL.txt file for details.", "AutoTrace not installed");
                            System.exit(0);
                        } else {
                            showMessage("An error occurred, cannot run conversion: \n" + e.getMessage(), "Error");
                        }
                    }
                }
            }.start();

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

    public static interface ConversionListener {
        void conversionFinished();

        void setColors(Color[] colors);
    }

}