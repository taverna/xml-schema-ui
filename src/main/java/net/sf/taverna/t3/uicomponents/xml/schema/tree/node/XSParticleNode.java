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

package net.sf.taverna.t3.uicomponents.xml.schema.tree.node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * @author Dmitry Repchevsky
 */

public class XSParticleNode extends XSAbstractNode<XmlSchemaParticle> {

    public XSParticleNode(XmlSchemaParticle particle) {
        super(particle);
    }

    @Override
    public XmlSchemaType getType() {
        if (component instanceof XmlSchemaElement) {
            XmlSchemaElement element = getElement((XmlSchemaElement)component);
            XmlSchemaType type = element.getSchemaType();
            if (type == null) {
                QName typeName = element.getSchemaTypeName();
                XmlSchema schema = element.getParent();
                XmlSchemaCollection schemaCollection = schema.getParent();
                if (schemaCollection != null) {
                    type = schemaCollection.getTypeByQName(typeName);
                } else {
                    type = schema.getTypeByName(typeName);
                }
            }
            
            return type;
        } else if (component instanceof XmlSchemaAny) {
            
        }
        return null; // ??? TODO ???
    }

    @Override
    public QName getName() {
        if (component instanceof XmlSchemaElement) {
            XmlSchemaElement element = getElement((XmlSchemaElement)component);
            return element.getQName();
        }
        return null;
    }

    @Override
    public Boolean validate() {
        Boolean isValid;

        final long min = component.getMinOccurs();
        final long max = component.getMaxOccurs();

        if (max > 1) {
            int bad = 0;
            int good = 0;
            int total = getChildCount();

            for (int i = 0; i < total; i++) {
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

            // ALL values must be the same - otherwise FALSE
            isValid = (bad == 0 && good == 0) ? null : bad == total ? null : good == total ? Boolean.TRUE : Boolean.FALSE;
        } else {
            int bad = 0;
            int good = getSimpleType() != null ? getUserObject() != null ? 1 : 0 : 0;

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

            isValid = good > 0 && bad > 0 ? Boolean.FALSE : bad > 0 ? min == 0 ? null : Boolean.FALSE : good > 0 ? Boolean.TRUE : null;
        }

        return isValid;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            setPrefix(stream);
            
            final QName qname = getName();
            final String localName = qname.getLocalPart();
            final String namespace = qname.getNamespaceURI();

            final long max = component.getMaxOccurs();
            if (max > 1) {
                for (int i = 0, n = getChildCount(); i < n; i++) {
                    if (namespace != null && namespace.length() > 0) {
                        stream.writeStartElement(namespace, localName);
                    }
                    else {
                        stream.writeStartElement(localName);
                    }

                    XSAbstractNode child = (XSAbstractNode)getChildAt(i);
                    child.write(stream);

                    stream.writeEndElement();
                }
            } else {
                if (namespace != null && namespace.length() > 0) {
                    stream.writeStartElement(namespace, localName);
                } else {
                    stream.writeStartElement(localName);
                }

                for (int i = 0, n = getChildCount(); i < n; i++) {
                    XSAbstractNode child = (XSAbstractNode)getChildAt(i);
                    child.write(stream);
                }

                Object object = getUserObject();
                if (object != null) {
                    stream.writeCharacters(object.toString());
                }

                stream.writeEndElement();
            }
        }
    }
}
