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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.tree.TreeCellEditor;

/**
 * @author Dmitry Repchevsky
 */

public abstract class XSTextAreaEditorComponent extends XSTextEditorComponent<JTextArea> {

    public XSTextAreaEditorComponent(final TreeCellEditor cEditor) {
        super(cEditor, new JTextArea());

        AbstractDocument document = (AbstractDocument)textComponent.getDocument();

        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
                String toRemove = fb.getDocument().getText(offset, length);
                fb.remove(offset, length);
                if (toRemove.indexOf('\n') >= 0) {
                    checkLayout();
                }
            }

            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                fb.insertString(offset, string, attr);
                if (string.indexOf('\n') >= 0) {
                    checkLayout();
                }
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                final int oldRowNumber = Math.min(4, textComponent.getLineCount());
                fb.replace(offset, length, text, attrs);
                final int newRowNumber = Math.min(4, textComponent.getLineCount());
                if (oldRowNumber != newRowNumber) {
                    checkLayout();
                }
            }
        });

        InputMap map = textComponent.getInputMap(JComponent.WHEN_FOCUSED);

        Object shift_enter_key = map.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK));
        if (shift_enter_key == null) {
            shift_enter_key = "shiftEnter";
        }

        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), shift_enter_key);

        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textComponent.isEnabled() && textComponent.isEditable()) {
                    Document doc = textComponent.getDocument();
                    try {
                        doc.insertString(textComponent.getCaretPosition(), "\r\n", null);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        textComponent.getActionMap().put(shift_enter_key, action);  
    }

    @Override
    public void setEditorValue(Object value) {
        super.setEditorValue(value);

        final int rows = textComponent.getLineCount();
        textComponent.setRows(Math.min(4, rows));
    }

    private void checkLayout() {
        final int rows = Math.min(4, textComponent.getLineCount());
        textComponent.setRows(rows);

        Component child = textComponent;

        // looking for JTree component
        Container parent;
        while(!((parent = child.getParent()) instanceof JTree) && parent != null) {
            child = parent;
        }

        if (parent != null) {
            JTree tree = (JTree)parent;

            TreeUI ui = tree.getUI();
            if (ui instanceof BasicTreeUI) {
                JComponent xsEditor = getXSEditor();
                xsEditor.setPreferredSize(null);
                Dimension dim = child.getSize();
                dim.height = xsEditor.getPreferredSize().height;

                child.setPreferredSize(null);
                child.setSize(dim);

                // relayout whole JTree
                BasicTreeUI basicTreeUI = (BasicTreeUI) tree.getUI();
                basicTreeUI.setLeftChildIndent(basicTreeUI.getLeftChildIndent());
            }
        }
    }
}
