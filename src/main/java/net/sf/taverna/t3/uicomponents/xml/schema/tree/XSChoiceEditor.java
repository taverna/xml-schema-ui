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

package net.sf.taverna.t3.uicomponents.xml.schema.tree;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.editor.XSEditorInterface;

/**
 * @author Dmitry Repchevsky
 */

public class XSChoiceEditor extends JComboBox implements XSEditorInterface {

    public XSChoiceEditor(XmlSchemaChoice xmlSchemaChoice) {
        super(new ChoiceListModel(xmlSchemaChoice));

        setRenderer(new ChoiceListCellRenderer());
        setBorder(new EmptyBorder(0,0,0,0));
    }

    @Override
    public Object getEditorValue() {
        return getSelectedItem();
    }

    @Override
    public void setEditorValue(Object value) {
        setSelectedItem(value);
    }

    @Override
    public JComponent getXSEditor() {
        return this;
    }

    public static class ChoiceListModel extends DefaultComboBoxModel {

        public ChoiceListModel(XmlSchemaChoice xmlSchemaChoice) {

            List<XmlSchemaObject> items = xmlSchemaChoice.getItems();
            for (XmlSchemaObject item : items) {
                this.addElement(item);
            }
        }
    }

    public static class ChoiceListCellRenderer extends JLabel implements ListCellRenderer {

        public ChoiceListCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0,0,0,0));
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Color foreground, background;
            if (isSelected) {
                foreground = UIManager.getLookAndFeel().getDefaults().getColor("Tree.selectionForeground");
                if (foreground == null) {
                    foreground = Color.WHITE;
                }

                background = UIManager.getLookAndFeel().getDefaults().getColor("Tree.selectionBackground");
                if (background == null) {
                    background = Color.BLUE;
                }
            } else {
                foreground = list.getForeground();
                background = list.getBackground();
            }

            setForeground(foreground);
            setBackground(background);

            if (value instanceof XmlSchemaElement) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement)value;
                XmlSchemaType xmlSchemaType = xmlSchemaElement.getSchemaType(); // TODO: (may be ref

                if (xmlSchemaType instanceof XmlSchemaSimpleType) {
                    value = xmlSchemaType.getName();
                } else {
                    value = "<html>" + xmlSchemaElement.getName() + " (" + xmlSchemaType.getName() + ")</html>";
                }
            }
            setText(value.toString());

            return this;
        }
    }
}
