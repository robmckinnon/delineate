/*
 * SettingsPanel.java
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

import net.sf.delineate.command.Command;
import net.sf.delineate.utility.ColorUtilities;
import net.sf.delineate.utility.XPathTool;
import org.xml.sax.SAXException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Controls user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SettingsPanel {

    private static final String SAVE_SETTINGS_ACTION = "SaveSettingsAction";
    private static final String LOAD_SETTINGS_ACTION = "SaveSettingsAction";

    private JPanel panel;
    private Command command;
//    private JTextArea commandTextArea;

    private final HashMap textFieldMap = new HashMap(5);

    private final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();

            if(source instanceof SpinnerSlider) {
                SpinnerSlider spinnerSlider = (SpinnerSlider)source;
                command.setParameterValue(spinnerSlider.getName(), spinnerSlider.getValueAsString());
            } else {
                JCheckBox checkBox = (JCheckBox)e.getSource();
                command.setParameterEnabled(checkBox.getName(), checkBox.isSelected());
            }
        }
    };

    private final KeyAdapter textFieldKeyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            JTextField textField = ((JTextField)e.getSource());
            command.setParameterValue(textField.getName(), textField.getText());
        }
    };
    private static final String BACKGROUND_COLOR_PARAMETER = "background-color";


    public SettingsPanel(String parameterFile) throws Exception {
//        commandTextArea = initCommandTextArea();

//        JPanel commandPanel = new JPanel();
//        commandPanel.add(commandTextArea);

        panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Settings"));
        panel.add(initContentPane(parameterFile));
//        panel.add(commandPanel);

//        SpringUtilities.makeCompactGrid(panel, 1, 1, 2, 2, 2, 2);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommand() {
        return command.getCommand();// commandTextArea.getText();
    }

    public boolean inputFileExists() {
        String inputFile = command.getParameterValue("input-file");
        File file = new File(inputFile);

        return file.isFile();
    }

    public String getOutputFile() {
        return command.getParameterValue("output-file");
    }

    public Action getSaveSettingsAction() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                Properties properties = new Properties();
                properties.setProperty("setting", getCommand());
                String settingsFile = "settings.prop";
                File file = new File(settingsFile);
                try {
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    properties.store(outputStream, settingsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        setKeyBinding(SAVE_SETTINGS_ACTION, KeyEvent.VK_S, KeyEvent.CTRL_MASK, action);

        return action;
    }

    public void selectInputTextField() {
        JTextField textField = getTextField("input-file");
        textField.selectAll();
        textField.requestFocus();
    }


    private void setKeyBinding(String actionKey, int key, int modifiers, AbstractAction action) {
        ActionMap actionMap = panel.getActionMap();
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
    }

    public Action getLoadSettingsAction() {
        AbstractAction action = new AbstractAction() {
                    public void actionPerformed(ActionEvent event) {
                        String settingsFile = "settings.prop";
                        File file = new File(settingsFile);
                        Properties properties = new Properties();

                        try {
                            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
                            properties.load(inStream);
                            String property = properties.getProperty("setting");
                            command.setCommand(property);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

        setKeyBinding(LOAD_SETTINGS_ACTION, KeyEvent.VK_L, KeyEvent.CTRL_MASK, action);

        return action;
    }

//    private JTextArea initCommandTextArea() {
//        JTextArea textArea = new JTextArea(5, 30);
//        textArea.setLineWrap(true);
//        textArea.setWrapStyleWord(true);
//        textArea.setEditable(false);
//        textArea.setBorder(BorderFactory.createLineBorder(Color.gray));
//        return textArea;
//    }

    private JPanel initContentPane(String parameterFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        XPathTool xpath = new XPathTool(new File(parameterFile));

        int parameterCount = xpath.count("/parameters/parameter");

        command = new Command(parameterCount, new Command.CommandChangeListener() {
            public void commandChanged(String commandText) {
//                commandTextArea.setText(commandText);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new SpringLayout());

        for(int type = 0; type < 3; type++) {
            for(int i = 0; i < parameterCount; i++) {
                String xpathPrefix = "/parameters/parameter[" + (i + 1) + "]/";
                xpath.setXpathPrefix(xpathPrefix);
                String name = xpath.string("name");
                boolean isFileParameter = name.endsWith("file");
                boolean isNumberParameter = xpath.count("range") == 1;

                switch(type) {
                    case 0:
                        if(isFileParameter)
                            addParameter(panel, xpath, xpathPrefix, name);
                        break;
                    case 1:
                        if(!isFileParameter && !isNumberParameter)
                            addParameter(panel, xpath, xpathPrefix, name);
                        break;
                    case 2:
                        if(!isFileParameter && isNumberParameter)
                            addParameter(panel, xpath, xpathPrefix, name);
                        break;
                }
            }
        }

        SpringUtilities.makeCompactGrid(panel, parameterCount, 2, 6, 6, 6, 6);
        return panel;
    }

    private void addParameter(JPanel panel, XPathTool xpath, String xpathPrefix, String name) throws TransformerException {
        boolean optional = xpath.toBoolean("optional");
        boolean enabled = optional ? xpath.toBoolean("enabled") : true;

        String value = xpath.string("default");
        String desc = xpath.string("description");
        command.addParameter(name, enabled, value);

        JComponent labelPanel = null;
        JComponent controlComponent = null;

        if(xpath.count("range") != 1) {
            labelPanel = initLabelPanel(optional, enabled, null, desc, name);
            controlComponent = initControlComponent(value, name);
        } else {
            xpath.setXpathPrefix(xpathPrefix + "range/");
            boolean useWholeNumbers = xpath.toBoolean("use-whole-numbers");
            String stepString = xpath.string("step").trim();

            SpinnerNumberModel model = initSpinnerModel(useWholeNumbers, xpath, value);
            SpinnerSlider spinnerSlider = initSpinnerSlider(model, name, enabled, desc, useWholeNumbers, stepString);

            labelPanel = initLabelPanel(optional, enabled, spinnerSlider, desc, name);
            controlComponent = initControlPanel(spinnerSlider);
        }

        panel.add(labelPanel);
        panel.add(controlComponent);
    }

    private JComponent initControlComponent(String value, String name) {
        if(value.length() > 0) {
            JTextField textField = new JTextField(value);
            textField.setName(name);
            textField.setColumns(15);

            textField.addKeyListener(textFieldKeyListener);

            textFieldMap.put(name, textField);

            if(name.equals(BACKGROUND_COLOR_PARAMETER)) {
                textField.setEnabled(false);
            }

            return textField;
        } else {
            return new JLabel("");
        }
    }

    private JPanel initControlPanel(SpinnerSlider spinnerSlider) {
        JPanel controlPanel = new JPanel(new BorderLayout(0,0));

        JSpinner spinner = spinnerSlider.getSpinner();
        Dimension size = new Dimension(53, (int)spinner.getPreferredSize().getHeight());
        spinner.setPreferredSize(size);
        controlPanel.add(spinner, BorderLayout.WEST);
        controlPanel.add(spinnerSlider.getSlider(), BorderLayout.EAST);

        return controlPanel;
    }

    private SpinnerSlider initSpinnerSlider(SpinnerNumberModel model, String name, boolean enabled, String desc, boolean useWholeNumbers, String stepString) {
        SpinnerSlider spinnerSlider = new SpinnerSlider(model);
        spinnerSlider.setName(name);
        spinnerSlider.setEnabled(enabled);
        spinnerSlider.setTooltipText(desc);
        spinnerSlider.addChangeListener(changeListener);

        if(!useWholeNumbers) {
            int fractionalDigits = stepString.substring(stepString.indexOf('.') + 1).length();
            spinnerSlider.setFractionDigitsLength(fractionalDigits);
        }
        return spinnerSlider;
    }

    private JPanel initLabelPanel(boolean optional, boolean enabled, final SpinnerSlider spinnerSlider, String desc, final String name) {
        boolean isFileParameter = name.endsWith("file");

        JLabel label = new JLabel(name.replace('-', ' '));
        label.setToolTipText(desc);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.WEST);

        if(optional) {
            JButton button = null;

            if(name.equals(BACKGROUND_COLOR_PARAMETER)) {
                button = new JButton("?");
                button.setToolTipText("Choose color");
                button.setEnabled(false);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        chooseColor(panel);
                    }
                });

                JPanel p = new JPanel();
                p.add(button);
                panel.add(p);
            }
            JCheckBox checkBox = initCheckbox(name, desc, enabled, spinnerSlider, button);
            panel.add(checkBox, BorderLayout.EAST);
        } else if(isFileParameter) {
            JButton button = initFileChooserButton(name);
            panel.add(button, BorderLayout.EAST);
        }

        return panel;
    }

    private void chooseColor(final JPanel panel) {
        JTextField textField = getTextField(BACKGROUND_COLOR_PARAMETER);
        String colorParameter = command.getParameterValue(BACKGROUND_COLOR_PARAMETER);
        Color initialColor = ColorUtilities.getColor(colorParameter);
        Color color = JColorChooser.showDialog(panel, "Choose background color", initialColor);
        if(color != null) {
            String hexColor = ColorUtilities.getHexColor(color);
            command.setParameterValue(BACKGROUND_COLOR_PARAMETER, hexColor);
            textField.setText(hexColor);
            textField.setBackground(color);
        }
    }

    private JButton initFileChooserButton(final String name) {
        final JFileChooser fileChooser = new JFileChooser();

        JButton button = new JButton("browse");
        button.setToolTipText("Browse files");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = fileChooser.showOpenDialog((JComponent)e.getSource());

                if(response == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    JTextField textField = getTextField(name);
                    textField.setText(file.getPath());
                    command.setParameterValue(textField.getName(), textField.getText());
                }
            }
        });
        return button;
    }

    private JCheckBox initCheckbox(final String name, String desc, boolean enabled, final SpinnerSlider spinnerSlider, final JButton button) {
        final JCheckBox checkBox = new JCheckBox("", false);
        checkBox.setName(name);
        checkBox.setToolTipText(desc);
        checkBox.setSelected(enabled);
        if(spinnerSlider != null) {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    spinnerSlider.setEnabled(checkBox.isSelected());
                }
            });
        } else {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JTextField textField = getTextField(BACKGROUND_COLOR_PARAMETER);
                    textField.setEnabled(checkBox.isSelected());
                    button.setEnabled(checkBox.isSelected());
                }
            });
        }
        checkBox.addChangeListener(changeListener);

        return checkBox;
    }

    private JTextField getTextField(String key) {
        JTextField textField = (JTextField)textFieldMap.get(key);
        return textField;
    }

    private SpinnerNumberModel initSpinnerModel(boolean useWholeNumbers, XPathTool xpath, String defaultValue) throws TransformerException {
        SpinnerNumberModel model;
        if(useWholeNumbers) {
            int value = Integer.parseInt(defaultValue);
            int min = xpath.toInt("min");
            int max = xpath.toInt("max");
            int step = xpath.toInt("step");

            model = new SpinnerNumberModel(value, min, max, step);
        } else {
            double value = Double.parseDouble(defaultValue);
            double min = xpath.toDouble("min");
            double max = xpath.toDouble("max");
            double step = xpath.toDouble("step");

            model = new SpinnerNumberModel(value, min, max, step);
        }
        return model;
    }

}
