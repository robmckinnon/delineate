/*
 * Parameter.java
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


/**
 * Represents a Autotrace command parameter.
 * @author robmckinnon@users.sourceforge.net
 */
public class Parameter implements Comparable {
    String name;
    boolean enabled;
    String defaultValue;
    String value;

    public Parameter(String name, boolean enabled, String value) {
        this.name = name;
        this.enabled = enabled;
        this.defaultValue = value;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getValue() {
        return value;
    }

    public String parameterSetting() {
        if(enabled) {
            String settingValue = value;
            if(name.equals(Command.INPUT_FILE_PARAMETER)) {
                if(value.indexOf(' ') != -1) {
                    settingValue = '"' + value + '"';
                }
                return settingValue;
            } else {
                String option = '-' + name + ' ';
                if(name.equals(Command.OUTPUT_FILE_PARAMETER) && value.indexOf(' ') != -1) {
                    settingValue = '"' + value + '"';
                }
                return (value.length() == 0) ? option : option + settingValue + ' ';
            }
        } else {
            return "";
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if(obj != null && obj instanceof Parameter) {
            return name.equals(((Parameter)obj).name);
        } else {
            return false;
        }
    }

    public int compareTo(Object obj) {
        if(obj != null) {
            if(name.equals("input-file")) {
                return 1;
            } else if(obj instanceof Parameter) {
                return name.compareTo(((Parameter)obj).name);
            } else {
                return name.compareTo(obj);
            }
        } else {
            throw new IllegalStateException("illegal state, this is not a Parameter " + String.valueOf(obj));
        }
    }

}
