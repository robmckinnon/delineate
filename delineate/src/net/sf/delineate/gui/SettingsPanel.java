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
import net.sf.delineate.utility.XPathTool;
import net.sf.delineate.utility.FileUtilities;
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
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;
import java.util.Set;

/**
 * Controls user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SettingsPanel {

    private static final String SAVE_SETTINGS_ACTION = "SaveSettingsAction";
    private static final String LOAD_SETTINGS_ACTION = "LoadSettingsAction";
    private static final String DELETE_SETTINGS_ACTION = "DeleteSettingsAction";
    private static final String INPUT_FILE_ACTION = "InputFileAction";
    private static final String OUTPUT_FILE_ACTION = "OutputFileAction";
    private static final String DEFAULT_SETTING_NAME = "default";

    private JPanel panel;
    private Command command;

    private final Map textFieldMap = new HashMap(5);
    private Map fileSizeLabelMap = new HashMap(5);
    private final Map checkBoxMap = new HashMap(23);
    private final Map spinnerSliderMap = new HashMap(23);
    private Properties savedSettings;
    private JComboBox loadSettingsCombo;

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

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = initDeleteButton();

        buttonPanel.add(initSaveButton());
        buttonPanel.add(initLoadButton());
        buttonPanel.add(initLoadSettingsCombo());
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommand() {
        return command.getCommand();
    }

    public boolean inputFileExists() {
        String inputFile = command.getParameterValue("input-file");
        File file = new File(inputFile);

        return file.isFile();
    }

    public String getOutputFile() {
        return command.getParameterValue("output-file");
    }

    private void saveSettings() {
        String initialName = (String)loadSettingsCombo.getSelectedItem();

        if(initialName.equals(DEFAULT_SETTING_NAME)) {
            initialName = "";
        }
        String name = (String)JOptionPane.showInputDialog(this.panel, "Enter a name:", "Save settings", JOptionPane.PLAIN_MESSAGE, null, null, initialName);

        if(name != null) {
            if(name.length() == 0) {
                JOptionPane.showMessageDialog(this.panel, "You must enter a name to save settings.", "Invalid name.", JOptionPane.PLAIN_MESSAGE);
                saveSettings();
            } else if(name.equals(DEFAULT_SETTING_NAME)) {
                JOptionPane.showMessageDialog(this.panel, "The name " + DEFAULT_SETTING_NAME + " is reserved.", "Invalid name.", JOptionPane.PLAIN_MESSAGE);
                saveSettings();
            } else {
                savedSettings.setProperty(name, command.getCommand());
                saveProperties(savedSettings);

                if(!name.equals(initialName)) {
                    loadSettingsCombo.addItem(name);
                    loadSettingsCombo.setSelectedItem(name);
                }
            }
        }

    }

    private void deleteSettings() {
        String settingName = (String)loadSettingsCombo.getSelectedItem();
        int response = JOptionPane.showConfirmDialog(loadSettingsCombo, "Delete " + settingName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);

        if(response == JOptionPane.YES_OPTION) {
            savedSettings.remove(settingName);
            saveProperties(savedSettings);
            loadSettingsCombo.removeItem(settingName);
            loadSettingsCombo.setSelectedItem(DELETE_SETTINGS_ACTION);
        }
    }

    private JButton initDeleteButton() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteSettings();
            }
        };
        action.setEnabled(false);

        return initButton("Delete settings", DELETE_SETTINGS_ACTION, KeyEvent.VK_D, action);
    }

    private JButton initLoadButton() {
        JButton button = initButton("Load:", LOAD_SETTINGS_ACTION, KeyEvent.VK_L, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                loadSettingsCombo.requestFocus();
            }
        });
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        return button;
    }

    private JButton initSaveButton() {
        return initButton("Save settings", SAVE_SETTINGS_ACTION, KeyEvent.VK_S, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                saveSettings();
            }
        });
    }

    private JButton initButton(String text, String actionKey, int shortcutKey, AbstractAction action) {
        setKeyBinding(actionKey, shortcutKey, KeyEvent.CTRL_MASK, action);

        JButton button = new JButton(action);
        button.setText(text);
        button.setMnemonic(shortcutKey);
        return button;
    }

    private void saveProperties(Properties properties) {
        File file = getSettingsFile();
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(outputStream, "Delineate command settings for AutoTrace invocation - http//delineate.sourceforge.net");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private JComboBox initLoadSettingsCombo() {
        savedSettings = loadProperties();
        savedSettings.setProperty(DEFAULT_SETTING_NAME, command.getCommand());

        Set keySet = savedSettings.keySet();
        String[] settingNames = (String[])keySet.toArray(new String[keySet.size()]);

        loadSettingsCombo = new JComboBox(settingNames);
        loadSettingsCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    loadSettings((String)loadSettingsCombo.getSelectedItem());
                }
            }
        });

        loadSettingsCombo.setSelectedItem(DEFAULT_SETTING_NAME);
        Dimension size = loadSettingsCombo.getPreferredSize();
        size.width = 72;
        loadSettingsCombo.setPreferredSize(size);
        loadSettingsCombo.setMaximumSize(size);
        return loadSettingsCombo;
    }

    private void loadSettings(String settingName) {
        String commandSetting = savedSettings.getProperty(settingName);
        Action deleteAction = panel.getActionMap().get(DELETE_SETTINGS_ACTION);

        if(settingName.equals(DEFAULT_SETTING_NAME)) {
            command.setCommandDefaultValues();
            deleteAction.setEnabled(false);
        } else {
            deleteAction.setEnabled(true);
        }

        command.setCommand(commandSetting);
    }

    private Properties loadProperties() {
        File file = getSettingsFile();
        Properties properties = new Properties();

        try {
            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
            properties.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private File getSettingsFile() {
        String settingsFile = "settings.prop";
        File file = new File(settingsFile);
        return file;
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
            boolean isFileParameter = name.endsWith("file");
            if(isFileParameter) {
                setFileSizeText(name, value);
            }
            JTextField textField = new JTextField(value);
            textField.setName(name);
            textField.setColumns(15);

            textField.addKeyListener(textFieldKeyListener);

            textFieldMap.put(name, textField);

            if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
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
        JTextField textField = getTextField(Command.BACKGROUND_COLOR_PARAMETER);
        String colorParameter = command.getParameterValue(Command.BACKGROUND_COLOR_PARAMETER);
        Color initialColor = ColorUtilities.getColor(colorParameter);
        Color color = JColorChooser.showDialog(panel, "Choose background color", initialColor);
        if(color != null) {
            String hexColor = ColorUtilities.getHexColor(color);
            command.setParameterValue(Command.BACKGROUND_COLOR_PARAMETER, hexColor, false);
            textField.setText(hexColor);
            textField.setBackground(color);
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
            button = initButton(labelName, INPUT_FILE_ACTION, KeyEvent.VK_I, action);
        } else if(name.equals(Command.OUTPUT_FILE_PARAMETER)) {
            button = initButton(labelName, OUTPUT_FILE_ACTION, KeyEvent.VK_O, action);
        }

        button.setToolTipText("Browse files");
        return button;
    }

    public void updateFileSize() {
        String file = command.getParameterValue(Command.OUTPUT_FILE_PARAMETER);
        setFileSizeText(Command.OUTPUT_FILE_PARAMETER, file);
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
                    JTextField textField = getTextField(Command.BACKGROUND_COLOR_PARAMETER);
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
