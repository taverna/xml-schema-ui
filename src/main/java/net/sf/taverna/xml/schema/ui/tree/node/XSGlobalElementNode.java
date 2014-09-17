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
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * The node that contains a global XML Schema element.
 * In the tree component these nodes may appear only as immediate children of the root node.
 * 
 * @author Dmitry Repchevsky
 */

public class XSGlobalElementNode extends XSAbstractNode<XmlSchemaElement> {

    public XSGlobalElementNode(XmlSchemaElement element) {
        super(element);
    }

    public XSGlobalElementNode(XmlSchemaElement element, Object value) {
        super(element, value);
    }
    
    @Override
    public XmlSchemaType getType() {
        XmlSchemaElement element = getElement(component);
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
    }

    @Override
    public QName getName() {
        return component.getQName();
    }

    @Override
    public Boolean validate() {
        final int n = getChildCount();

        // if there is no child nodes in the node suppose it's ok.
        if (n == 0) {
            return Boolean.TRUE;
        }

        Boolean valid = null;

        for (int i = 0; i < n; i++) {
            XSAbstractNode child = (XSAbstractNode)getChildAt(i);
            Boolean b = child.validate();
            if (Boolean.FALSE.equals(b)) {
                valid = Boolean.FALSE;
                break;
            }
            if (valid == null) {
                valid = b;
            }
        }

        return valid == null ? (component.isNillable() ? null : Boolean.FALSE) : Boolean.TRUE;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            XmlSchemaType type = component.getSchemaType();
            if (type instanceof XmlSchemaSimpleType) {
                writeType(stream);
            } else if (type instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                XmlSchemaParticle particle = complexType.getParticle();
                if (particle == null) {
                    final XmlSchemaContentModel contentModel = complexType.getContentModel();
                    if (contentModel instanceof XmlSchemaComplexContent) {
                        XmlSchemaContent content = contentModel.getContent();
                        if (content instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                            particle = complexContentExtension.getParticle();
                        } else if (content instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                            particle = complexContentRestriction.getParticle();
                        }
                    }
                }
                
                if (particle != null && particle.getMaxOccurs() > 1) {
                    writeModelGroup(stream);
                } else {
                    writeType(stream);
                }
            }
        }
    }

    /**
     * Write the element represented by this node to the stream.
     * 
     * @param stream
     * @throws XMLStreamException 
     */
    private void writeType(XMLStreamWriter stream) throws XMLStreamException {
        setPrefix(stream);

        final QName qname = component.getQName();

        final String localName = qname.getLocalPart();
        final String namespace = qname.getNamespaceURI();

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
            XmlSchemaSimpleType simpleType = getSimpleType();
            if (simpleType != null) {
                stream.writeCharacters(object.toString());
            }
        }

        stream.writeEndElement();
    }

    private void writeModelGroup(XMLStreamWriter stream) throws XMLStreamException {
        final QName name = component.getQName();

        final String localName = name.getLocalPart();
        final String namespace = name.getNamespaceURI();

        for (int i = 0, n = getChildCount(); i < n; i++) {
            if (namespace != null && namespace.length() > 0) {
                stream.writeStartElement(namespace, localName);
            } else {
                stream.writeStartElement(localName);
            }
            
            XSAbstractNode child = (XSAbstractNode)getChildAt(i);
            child.write(stream);

            stream.writeEndElement();
        }
    }
    
    @Override
    public String getXPath() {
        final StringBuilder xpath = new StringBuilder("/");
        final QName qname = getName();
        final String localpart = qname.getLocalPart();
        final String namespace = qname.getNamespaceURI();
        if (namespace.isEmpty()) {
            xpath.append(localpart);
        } else {
            xpath.append("*[namespace-uri()='").append(namespace).append("' and local-name()='").append(localpart).append("']");
        }
        return xpath.toString();
    }
}
