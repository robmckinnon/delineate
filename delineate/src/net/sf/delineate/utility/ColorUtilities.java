/*
 * ColorUtilities.java
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
package net.sf.delineate.utility;

import java.awt.Color;

/**
 * Colour utility methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class ColorUtilities {

    public static Color getColor(String hexColor) {
        int red = Integer.parseInt(hexColor.substring(0, 2), 16);
        int green = Integer.parseInt(hexColor.substring(2, 4), 16);
        int blue = Integer.parseInt(hexColor.substring(4), 16);

        return new Color(red, green, blue);
    }

    public static String getHexColor(Color color) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getHexString(color.getRed()));
        buffer.append(getHexString(color.getGreen()));
        buffer.append(getHexString(color.getBlue()));
        return buffer.toString();
    }

    private static String getHexString(int integer) {
        String hex = Integer.toHexString(integer).toUpperCase();

        if(hex.length() == 1) {
            hex = '0' + hex;
        }

        return hex;
    }

}
