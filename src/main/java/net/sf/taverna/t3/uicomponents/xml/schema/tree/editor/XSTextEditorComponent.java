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

package net.sf.taverna.t3.uicomponents.xml.schema.tree.editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreeCellEditor;

/**
 * @author Dmitry Repchevsky
 */

public abstract class XSTextEditorComponent<T extends JTextComponent> implements XSEditorInterface, XSParserInterface {
    
    private final static Color BACKGOUND = new Color(0xFFFFCC);
    private final static Color FOREGOUND = new Color(0x0000FF);

    protected final T textComponent;
    private final JScrollPane scroll;

    public XSTextEditorComponent(final TreeCellEditor cEditor, T textComponent) {
        this.textComponent = textComponent;

        textComponent.setForeground(FOREGOUND);
        textComponent.setBackground(BACKGOUND);

        textComponent.setDocument(new XSDocument(this));

        InputMap map = textComponent.getInputMap(JComponent.WHEN_FOCUSED);

        Object enter_key = map.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        if (enter_key == null) {
            enter_key = "enter";
        }

        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter_key);

        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cEditor.stopCellEditing();
            }
        };

        textComponent.getActionMap().put(enter_key, action);

        scroll = new JScrollPane(textComponent, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
    }

    private Object parse(Object value) {
        if (value != null) {
            String text = value.toString();
            if (text != null && text.length() > 0) {
                try {
                    parse(text);
                    return text;
                }
                catch(IllegalArgumentException ex) {}
            }
        }
        return null;
    }

    @Override
    public Object getEditorValue() {
        String text = textComponent.getText();

        try {
            return parse(text);
        } catch(IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public void setEditorValue(Object value) {
        Object object = parse(value);
        String text = object == null ? "" : object.toString();

        textComponent.setText(text == null ? "" : text);
    }

    @Override
    public JComponent getXSEditor() {
        return scroll;
    }

    protected abstract class XSAbstractDocument extends PlainDocument implements DocumentListener {
        public XSAbstractDocument() {
            addDocumentListener(this);
        }

        protected abstract boolean isValid();

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            textComponent.setBackground(isValid() ? BACKGOUND : Color.RED);
        }
    }

    protected class XSDocument extends XSAbstractDocument {
        private final XSParserInterface parser;

        public XSDocument(XSParserInterface parser) {
            this.parser = parser;
            //setDocumentFilter(new XSDocumentFilter(parser));
        }

        @Override
        protected boolean isValid() {
            try {
                return parser.parse(getText(0, getLength())) != null;
            }
            catch(IllegalArgumentException ex) {}
            catch(BadLocationException ex) {}

            return false;
        }
    }
}
