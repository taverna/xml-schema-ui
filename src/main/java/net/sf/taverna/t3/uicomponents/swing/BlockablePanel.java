/**
 * *****************************************************************************
 * Copyright (C) 2014 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.t3.uicomponents.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

/**
 * @author Dmitry Repchevsky
 */

public class BlockablePanel extends JLayeredPane implements KeyEventDispatcher {
    private DummyGlassPane glass;

    public BlockablePanel() {
        add(glass = new DummyGlassPane(), JLayeredPane.DRAG_LAYER);
        glass.setVisible(false);
    }

    @Override
    public void doLayout() {
        Insets ins = this.getInsets();
        Dimension dim = this.getSize();

        Component[] components = getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);

        for (Component c : components) {
            c.setBounds(ins.left, ins.top, dim.width, dim.height);
        }

        glass.setBounds(ins.left, ins.top, dim.width, dim.height);
    }

    public void block() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        glass.setVisible(true);
    }

    public void unblock() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        glass.setVisible(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        return SwingUtilities.isDescendingFrom(e.getComponent(), this);
    }
}
