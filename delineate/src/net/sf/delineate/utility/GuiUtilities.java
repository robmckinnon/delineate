/*
 * GuiUtilities.java
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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * GUI helper methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class GuiUtilities {

    public static JButton initButton(String text, String actionKey, int shortcutKey, AbstractAction action, JComponent component) {
        setKeyBinding(actionKey, shortcutKey, KeyEvent.CTRL_MASK, action, component);

        JButton button = new JButton(action);
        button.setText(text);
        button.setMnemonic(shortcutKey);
        return button;
    }

    public static void setKeyBinding(String actionKey, int key, int modifiers, AbstractAction action, JComponent component) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
    }

}
