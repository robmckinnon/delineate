/*
 * Command.java
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
package net.sf.delineate.command;

import net.sf.delineate.command.Parameter;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Represents Autotrace command.
 * @author robmckinnon@users.sourceforge.net
 */
public class Command {

    private CommandChangeListener changeListener;
    private Parameter[] parameters;
    int parameterCount = 0;
    public static final String INPUT_FILE_PARAMETER = "input-file";
    public static final String OUTPUT_FILE_PARAMETER = "output-file";
    public static final String BACKGROUND_COLOR_PARAMETER = "background-color";
    public static final String CENTERLINE_PARAMETER = "centerline";

    public Command(int totalParameterCount, CommandChangeListener listener) {
        parameters = new Parameter[totalParameterCount];
        changeListener = listener;
    }

    public void addParameter(String name, boolean enabled, String value) {
        if(parameterCount == parameters.length) {
            throw new IllegalStateException("Command can only hold " + parameters.length + " parameters.");
        }

        Parameter parameter = new Parameter(name, enabled, value);
        parameters[parameterCount] = parameter;
        parameterCount++;

        if(parameterCount == parameters.length) {
            Arrays.sort(parameters);
        }
    }

    public void setParameterEnabled(String name, boolean enabled, boolean notify) {
        Parameter parameter = getParameter(name);

        if(parameter.enabled != enabled) {
            parameter.enabled = enabled;
        }
        if(notify) {
            changeListener.enabledChanged(parameter);
        }
    }

    public void setParameterValue(String name, String value, boolean notify) {
        Parameter parameter = getParameter(name);

        if(!parameter.value.equals(value)) {
            parameter.value = value;
        }
        if(notify) {
            changeListener.valueChanged(parameter);
        }
    }

    public String getCommand() {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String parameterSetting = parameter.parameterSetting();
            buffer.append(parameterSetting);
        }
        String command = "autotrace " + buffer.toString();
        return command;
    }

    private Parameter getParameter(String name) {
        if(name.equals(INPUT_FILE_PARAMETER)) {
            return parameters[parameters.length -1];
        } else {
            int index = Arrays.binarySearch(parameters, name);
            Parameter parameter = parameters[index];
            return parameter;
        }
    }

    public boolean getParameterEnabled(String name) {
        return getParameter(name).enabled;
    }

    public String getParameterValue(String name) {
        return getParameter(name).value;
    }

    public void setCommandDefaultValues() {
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = parameter.getName();
            if(!name.equals(INPUT_FILE_PARAMETER) && !name.equals(OUTPUT_FILE_PARAMETER)) {
                setParameterValue(name, parameter.defaultValue, true);
            }
        }
    }

    public void setCommand(String command) {
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = parameter.getName();
            if(!name.equals(INPUT_FILE_PARAMETER)) {
                setParameterEnabled(name, false, true);
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        tokenizer.nextToken();
        String name = tokenizer.nextToken();
        while(tokenizer.hasMoreTokens()) {
            if(name.charAt(0) == '-') {
                name = name.substring(1);
                setParameterEnabled(name, true, true);

                String value = tokenizer.nextToken();
                if(value.charAt(0) != '-') {
                    if(!name.equals(OUTPUT_FILE_PARAMETER)) {
                        setParameterValue(name, value, true);
                    }
                    name = tokenizer.nextToken();
                } else {
                    name = value;
                }
            }
        }
    }

    /**
     * For listening to command changes.
     */
    public interface CommandChangeListener {
        void enabledChanged(Parameter parameter);
        void valueChanged(Parameter parameter);
    }

}
