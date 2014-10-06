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

package net.sf.taverna.xml.schema.parser;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;

/**
 * An abstract implementation of XML Schema component tree node.
 * 
 * @author Dmitry Repchevsky
 */

public abstract class XSComponent<T,V extends T, U extends XmlSchemaObject> extends XSNode<T,V> {
    protected U component;
    
    public XSComponent(U component) {
        this(component, null);
    }

    public XSComponent(U component, Object value) {
        this.component = component;
    }

    public U getXSComponent() {
        return component;
    }

    public final QName getTypeName() {
        XmlSchemaType type = getType();

        // the type could be anonymouse
        QName qname = type.getQName();
        
        return qname != null ? new QName(qname.getNamespaceURI(), qname.getLocalPart()) : new QName("");
    }
    
    public abstract QName getName();
    public abstract XmlSchemaType getType();

    public abstract Boolean validate();
    public abstract void write(XMLStreamWriter stream) throws XMLStreamException;
    public abstract String getXPath();

    protected void setPrefix(XMLStreamWriter stream) throws XMLStreamException {
        final QName qname = getName();
        final String namespace = qname.getNamespaceURI();
        
        if (namespace.length() > 0) {
            NamespaceContext ctx = stream.getNamespaceContext();

            String prefix = ctx.getPrefix(namespace);
            if (prefix == null && component instanceof XmlSchemaNamed) {
                XmlSchemaNamed named = (XmlSchemaNamed)component;
                XmlSchema schema = named.getParent();
                    
                NamespacePrefixList namespaces = schema.getNamespaceContext();
                if (namespaces != null) {
                    prefix = namespaces.getPrefix(namespace);
                }
            }
            if (prefix == null) {
                prefix = "ns0";
                for (int i = 1; !XMLConstants.NULL_NS_URI.equals(ctx.getNamespaceURI(prefix)); i++) {
                    prefix = "ns" + i;
                }
            }
            stream.setPrefix(prefix, namespace);
        }
    }
}
