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

package net.sf.taverna.xml.schema.ui.tree.node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * The node to hold a type value.
 * This node is used as a child of a parent particle (usually element), which
 * cardinality is more than 1. When cardinality of the parent particle is 1 
 * there is no need to make an additional node.
 * 
 * @author Dmitry Repchevsky
 */

public class XSTypeNode extends XSAbstractNode<XmlSchemaType> {

    private final boolean isSimple;

    public XSTypeNode(XmlSchemaType type) {
        this(type, null);
    }

    public XSTypeNode(XmlSchemaType type, Object object) {
        super(type, object);
        
        isSimple = getSimpleType() != null;
    }

    @Override
    public XmlSchemaType getType() {
        return component;
    }

    @Override
    public QName getName() {
        return component.getQName();
    }

    @Override
    public Boolean validate() {
        int bad = 0;
        int good = isSimple ? getUserObject() != null ? 1 : 0 : 0;

        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSAbstractNode child = (XSAbstractNode)getChildAt(i);
            Boolean valid = child.validate();
            if (valid != null) {
                if (valid) {
                    good++;
                } else {
                    bad++;
                }
            }
        }

        Boolean isValid = good > 0 ? bad > 0 ? Boolean.FALSE : Boolean.TRUE : null;
        return isValid;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            // suppose that attributes are ALWAYS before elements
            for (int i = 0, n = getChildCount(); i < n; i++) {
                XSAbstractNode child = (XSAbstractNode)getChildAt(i);
                child.write(stream);
            }

            if (isSimple) {
                Object object = getUserObject();
                if (object != null) {
                    stream.writeCharacters(object.toString());
                }
            }
        }
    }
}
