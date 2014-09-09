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

package net.sf.taverna.xml.schema.ui.tree.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.tree.TreeCellEditor;

/**
 * @author Dmitry Repchevsky
 */

public class XSBooleanComponent extends Box 
        implements XSEditorInterface, ActionListener {

    private final TreeCellEditor cEditor;

    private final JRadioButton bTrue;
    private final JRadioButton bFalse;

    public XSBooleanComponent(final TreeCellEditor cEditor) {
        super(BoxLayout.X_AXIS);

        this.cEditor = cEditor;

        setOpaque(false);

        bTrue = new JRadioButton(Boolean.toString(true));
        bFalse = new JRadioButton(Boolean.toString(false));

        bTrue.setFocusPainted(false);
        bFalse.setFocusPainted(false);
        bTrue.setContentAreaFilled(false);
        bFalse.setContentAreaFilled(false);

        bTrue.addActionListener(this);
        bFalse.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(bTrue);
        group.add(bFalse);

        add(bTrue);
        add(bFalse);
    }

    @Override
    public Object getEditorValue() {
        return bTrue.isSelected();
    }

    @Override
    public void setEditorValue(Object value) {
        final boolean b = value == null || value.equals(Boolean.FALSE);
        bFalse.setSelected(b);
        bTrue.setSelected(!b);
    }

    @Override
    public JComponent getXSEditor() {
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cEditor.stopCellEditing();
    }
}
