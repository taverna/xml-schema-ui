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

package net.sf.taverna.xml.schema.ui.tree;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import net.sf.taverna.xml.schema.ui.tree.editor.XSEditorInterface;

/**
 * @author Dmitry Repchevsky
 */

public class XSEnumerationEditor extends JComboBox implements XSEditorInterface {

    public XSEnumerationEditor(List<XmlSchemaEnumerationFacet> facets) {
        super(new EnumerationListModel(facets));
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

    public static class EnumerationListModel extends DefaultComboBoxModel {

        public EnumerationListModel(List<XmlSchemaEnumerationFacet> facets) {
            for (XmlSchemaEnumerationFacet facet : facets) {
                Object value = facet.getValue();
                addElement(value);
            }
        }
    }
}
