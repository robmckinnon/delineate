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
import net.sf.delineate.command.Parameter;
import net.sf.delineate.utility.ColorUtilities;
import net.sf.delineate.utility.FileUtilities;
import net.sf.delineate.utility.GuiUtilities;
import net.sf.delineate.utility.XPathTool;
import org.xml.sax.SAXException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SettingsPanel {

    private static final String INPUT_FILE_ACTION = "InputFileAction";
    private static final String OUTPUT_FILE_ACTION = "OutputFileAction";

    private JPanel panel;
    private Command command;

    private final Map textFieldMap = new HashMap(5);
    private Map fileSizeLabelMap = new HashMap(5);
    private final Map checkBoxMap = new HashMap(23);
    private final Map spinnerSliderMap = new HashMap(23);

    private final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();

            if(source instanceof SpinnerSlider) {
                SpinnerSlider spinnerSlider = (SpinnerSlider)source;
                command.setParameterValue(spinnerSlider.getName(), spinnerSlider.getValueAsString(), false);
            } else {
                JCheckBox checkBox = (JCheckBox)e.getSource();
                command.setParameterEnabled(checkBox.getName(), checkBox.isSelected(), false);
            }
        }
    };

    private final KeyAdapter colorTextFieldKeyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent event) {
            JComboBox combo = (JComboBox)textFieldMap.get(Command.BACKGROUND_COLOR_PARAMETER);
            JTextField textField = (JTextField)event.getSource();
            String text = textField.getText();

            if(text.length() == 6) {
                try {
                    Color color = ColorUtilities.getColor(text);
                    combo.addItem(text);
                    combo.setSelectedItem(text);
                    textField.setBackground(color);
                    Color foreground = ColorUtilities.getForeground(color);
                    textField.setForeground(foreground);
                    textField.setCaretColor(foreground);
                    command.setParameterValue(Command.BACKGROUND_COLOR_PARAMETER, text, false);
                } catch(Exception e) {

                }
            } else {
                textField.setBackground(Color.white);
                textField.setForeground(Color.black);
            }

        }
    };

    private final KeyAdapter textFieldKeyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            JTextField textField = ((JTextField)e.getSource());
            command.setParameterValue(textField.getName(), textField.getText(), false);
            setFileSizeText(textField.getName(), textField.getText());
        }
    };


    public SettingsPanel(String parameterFile) throws Exception {
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Settings"));
        panel.add(initContentPane(parameterFile), BorderLayout.NORTH);

        SaveSettingsPanel saveSettingsPanel = new SaveSettingsPanel(command);

        panel.add(saveSettingsPanel.getPanel(), BorderLayout.SOUTH);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommand() {
        return command.getCommand();
    }

    public String getBackgroundColor() {
        if(command.getParameterEnabled(Command.BACKGROUND_COLOR_PARAMETER)) {
            return command.getParameterValue(Command.BACKGROUND_COLOR_PARAMETER);
        } else {
            return null;
        }
    }

    public boolean inputFileExists() {
        String inputFile = command.getParameterValue(Command.INPUT_FILE_PARAMETER);
        File file = new File(inputFile);

        return file.isFile();
    }

    public String getOutputFile() {
        return command.getParameterValue(Command.OUTPUT_FILE_PARAMETER);
    }

    public void selectInputTextField() {
        JTextField textField = getTextField(Command.INPUT_FILE_PARAMETER);
        textField.selectAll();
        textField.requestFocus();
    }

    public void updateFileSize() {
        String file = command.getParameterValue(Command.OUTPUT_FILE_PARAMETER);
        setFileSizeText(Command.OUTPUT_FILE_PARAMETER, file);
    }

    private JPanel initContentPane(String parameterFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new SpringLayout());

        XPathTool xpath = new XPathTool(new File(parameterFile));

        int parameterCount = xpath.count("/parameters/parameter");

        command = new Command(parameterCount, new Command.CommandChangeListener() {
            public void enabledChanged(Parameter parameter) {
                String name = parameter.getName();
                JCheckBox checkBox = (JCheckBox)checkBoxMap.get(name);
                if(checkBox != null) checkBox.setSelected(parameter.isEnabled());
            }

            public void valueChanged(Parameter parameter) {
                String name = parameter.getName();
                JTextField textField = (JTextField)textFieldMap.get(name);
                if(textField != null) {
                    String path = parameter.getValue();
                    textField.setText(path);
                }

                SpinnerSlider spinnerSlider = (SpinnerSlider)spinnerSliderMap.get(name);
                if(spinnerSlider != null) {
                    spinnerSlider.setValue(parameter.getValue());
                }
            }
        });

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
            if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
                JComboBox colorCombo = new JComboBox();
                colorCombo.setEditable(true);
                colorCombo.setEnabled(false);
                colorCombo.setName(name);
                Component editorComponent = colorCombo.getEditor().getEditorComponent();
                editorComponent.addKeyListener(colorTextFieldKeyListener);
                textFieldMap.put(name, colorCombo);

                return colorCombo;
            } else {
                setFileSizeText(name, value);
                JTextField textField = new JTextField(value);
                textField.setName(name);
                textField.setColumns(15);
                textField.addKeyListener(textFieldKeyListener);
                textFieldMap.put(name, textField);

                return textField;
            }

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
        spinnerSliderMap.put(name, spinnerSlider);

        if(!useWholeNumbers) {
            int fractionalDigits = stepString.substring(stepString.indexOf('.') + 1).length();
            spinnerSlider.setFractionDigitsLength(fractionalDigits);
        }
        return spinnerSlider;
    }

    private JPanel initLabelPanel(boolean optional, boolean enabled, final SpinnerSlider spinnerSlider, String desc, final String name) {
        String labelName = name.replace('-', ' ');
        boolean isFileParameter = name.endsWith("file");
        boolean isBgColorParameter = name.equals(Command.BACKGROUND_COLOR_PARAMETER);
        final JPanel panel = new JPanel(new BorderLayout());
        Component labelComponent = null;

        JButton button = null;
        if(!isFileParameter && !isBgColorParameter) {
            JLabel label = new JLabel(labelName);
            label.setToolTipText(desc);
            labelComponent = label;
        } else if(isBgColorParameter) {
            button = initColorChooserButton(labelName, panel);
            labelComponent = button;
        } else if(isFileParameter) {
            labelComponent = initFileChooserButton(name, labelName);
        }

        panel.add(labelComponent, BorderLayout.WEST);

        if(optional) {
            JCheckBox checkBox = initCheckbox(name, desc, enabled, spinnerSlider, button);
            panel.add(checkBox, BorderLayout.EAST);
        } else if(isFileParameter) {
            JLabel label = new JLabel();
            fileSizeLabelMap.put(name, label);
            panel.add(label, BorderLayout.EAST);
        }

        return panel;
    }

    private JButton initColorChooserButton(String labelName, final JPanel panel) {
        JButton button;
        button = new JButton(labelName);
        button.setToolTipText("Choose color");
        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseColor(panel);
            }
        });
        return button;
    }

    private void chooseColor(final JPanel panel) {
        JComboBox combo = (JComboBox)textFieldMap.get(Command.BACKGROUND_COLOR_PARAMETER);
        String colorParameter = command.getParameterValue(Command.BACKGROUND_COLOR_PARAMETER);
        Color initialColor = ColorUtilities.getColor(colorParameter);
        Color color = JColorChooser.showDialog(panel, "Choose background color", initialColor);
        if(color != null) {
            String hexColor = ColorUtilities.getHexColor(color);
            command.setParameterValue(Command.BACKGROUND_COLOR_PARAMETER, hexColor, false);
            combo.addItem(hexColor);
            combo.setSelectedItem(hexColor);
            Component editorComponent = combo.getEditor().getEditorComponent();
            editorComponent.setBackground(color);
            editorComponent.setForeground(ColorUtilities.getForeground(color));
        }
    }

    private JButton initFileChooserButton(final String name, String labelName) {
        AbstractAction action = new AbstractAction() {
            JFileChooser fileChooser = new JFileChooser();

            public void actionPerformed(ActionEvent e) {
                int response = fileChooser.showOpenDialog((JComponent)e.getSource());

                if(response == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    JTextField textField = getTextField(name);
                    textField.setText(file.getPath());
                    setFileSizeText(name, file.getPath());

                    command.setParameterValue(textField.getName(), textField.getText(), false);
                }
            }
        };

        JButton button = null;

        if(name.equals(Command.INPUT_FILE_PARAMETER)) {
            button = GuiUtilities.initButton(labelName, INPUT_FILE_ACTION, KeyEvent.VK_I, panel, action);
        } else if(name.equals(Command.OUTPUT_FILE_PARAMETER)) {
            button = GuiUtilities.initButton(labelName, OUTPUT_FILE_ACTION, KeyEvent.VK_O, panel, action);
        }

        button.setToolTipText("Browse files");
        return button;
    }

    private void setFileSizeText(final String name, String filePath) {
        JLabel label = (JLabel)fileSizeLabelMap.get(name);

        if(label != null) {
            File file = FileUtilities.getFile(filePath);
            String fileSize = FileUtilities.getFileSize(file);
            label.setText(fileSize);
        }
    }

    private JCheckBox initCheckbox(final String name, String desc, boolean enabled, final SpinnerSlider spinnerSlider, final JButton button) {
        final JCheckBox checkBox = new JCheckBox("", false);
        checkBoxMap.put(name, checkBox);
        checkBox.setName(name);
        checkBox.setToolTipText(desc);
        checkBox.setSelected(enabled);
        checkBox.setFocusPainted(true);
        if(spinnerSlider != null) {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    spinnerSlider.setEnabled(checkBox.isSelected());
                }
            });
        } else if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JComboBox combo = (JComboBox)textFieldMap.get(name);
                    combo.setEnabled(checkBox.isSelected());
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
