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

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaUse;

/**
 * @author Dmitry Repchevsky
 */

public class XSAttributeNode extends XSAbstractNode<XmlSchemaAttribute> {

    public XSAttributeNode(XmlSchemaAttribute attribute) {
        this(attribute, null);
    }

    public XSAttributeNode(XmlSchemaAttribute attribute, Object object) {
        super(attribute, object);
    }

    @Override
    public XmlSchemaType getType() {
        XmlSchemaType simpleType = component.getSchemaType();
        if (simpleType == null) {
            QName typeName = component.getSchemaTypeName();
            XmlSchema schema = component.getParent();
            XmlSchemaCollection schemaCollection = schema.getParent();
            if (schemaCollection != null) {
                simpleType = schemaCollection.getTypeByQName(typeName);
            } else {
                simpleType = schema.getTypeByName(typeName);
            }
        }
        return simpleType;
    }

    @Override
    public QName getName() {
        return component.getWireName();
    }

    @Override
    public Boolean validate() {
        if (getUserObject() != null) {
            return Boolean.TRUE;
        }    
        return XmlSchemaUse.REQUIRED == component.getUse() ? Boolean.FALSE : null;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        QName type = getTypeName();
        Object object = getUserObject();

        if (object != null) {
            if (object instanceof QName) {
                object = DatatypeConverter.printQName((QName)object, stream.getNamespaceContext());
            }

            setPrefix(stream);
            
            final QName name = getName();
            final String localName = name.getLocalPart();
            final String namespace = name.getNamespaceURI();

            if (namespace != null && namespace.length() > 0) {
                stream.writeAttribute(namespace, localName, object.toString());
            } else {
                stream.writeAttribute(localName, object.toString());
            }
        } else if (XmlSchemaUse.REQUIRED == component.getUse()) {
            throw new XMLStreamException("Required attribute missing: " + type.toString());
        }
    }
}
