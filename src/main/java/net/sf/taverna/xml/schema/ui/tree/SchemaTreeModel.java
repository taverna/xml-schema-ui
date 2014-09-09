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

import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import net.sf.taverna.xml.schema.ui.tree.node.MessagePartNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSAbstractNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSAttributeNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSGlobalElementNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSMixedTextNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSParticleNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSTypeNode;

/**
 * @author Dmitry Repchevsky
 */

public class SchemaTreeModel extends DefaultTreeModel {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    XmlSchemaCollection schemas;

    public SchemaTreeModel() {
        super(new DefaultMutableTreeNode());
        schemas = new XmlSchemaCollection();
    }

    @Override
    public DefaultMutableTreeNode getRoot() {
        return (DefaultMutableTreeNode)super.getRoot();
    }

    /**
     * Clears the tree model
     */
    public void clear() {
        DefaultMutableTreeNode raiz = getRoot();
        raiz.removeAllChildren();
        nodeStructureChanged(raiz);
    }

    public void setSchemaCollection(XmlSchemaCollection schemas) {
        clear();
        this.schemas = schemas;
    }

    public boolean validate() {
        Boolean valid = null;

        DefaultMutableTreeNode raiz = getRoot();

        for (int i = 0, n = raiz.getChildCount(); i < n; i++) {
            XSAbstractNode node = (XSAbstractNode)raiz.getChildAt(i);

            Boolean b = node.validate();
            if (Boolean.FALSE.equals(b)) {
                valid = Boolean.FALSE;
                break;
            }

            if (valid == null) {
                valid = b;
            }
        }
        return valid != null && valid;
    }

    /**
     * Includes the element (with all its subelements obtained from schema) to the model
     *
     * @param element the element name to include into the model
     */
    public void addElement(QName element) {
        DefaultMutableTreeNode raiz = getRoot();

        XSAbstractNode node = findElement(element);

        if (node != null) {
            node.parse();
            raiz.add(node);
            nodeStructureChanged(raiz);
        }
    }

    /**
     * Includes the type (with all its subelements obtained from schema) to the model
     *
     * @param type
     * @param name
     */
    public void addType(QName type, QName name) {
        DefaultMutableTreeNode raiz = getRoot();

        XSAbstractNode node = findType(type, name);
        if (node != null) {
            node.parse();
            raiz.add(node);
            nodeStructureChanged(raiz);
        }
    }

    /**
     * Constructs the model based on XML obtained from the provided stream
     *
     * @param stream The XML reader where the XML is read from
     * @throws javax.xml.stream.XMLStreamException
     */
    public void read(XMLStreamReader stream) throws XMLStreamException {
        DefaultMutableTreeNode raiz = getRoot();
        raiz.removeAllChildren();

        readElement(raiz, stream);

        nodeStructureChanged(raiz);
    }

    /**
     * Constructs an XML based on the data provided by the model
     * @param stream XML writer to write generated XML
     * @throws javax.xml.stream.XMLStreamException
     */
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        DefaultMutableTreeNode raiz = getRoot();
        for (int i = 0, n = raiz.getChildCount(); i < n; i++) {
            XSAbstractNode node = (XSAbstractNode)raiz.getChildAt(i);
            node.write(stream);
        }
    }

    private void readElement(DefaultMutableTreeNode parent, XMLStreamReader reader) throws XMLStreamException {
        StringBuilder text = new StringBuilder();

        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT) {
                if (text.length() > 0 && parent instanceof XSAbstractNode) {
                    XSAbstractNode node = (XSAbstractNode) parent;
                    if (node.getSimpleType() != null) {
                        parent.setUserObject(text.toString());
                    }
                }
                return;
            } else if (eventType == XMLStreamReader.START_ELEMENT) {
                QName elementName = reader.getName();

                if (parent.isRoot()) {
                    XSAbstractNode node = findElement(elementName);
                    if (node != null) {
                        parent.add(node);

                        readAttributes(node, reader);
                        readElement(node, reader);
                    }
                } else {
                    if (parent instanceof XSParticleNode) {
                        XSParticleNode node = (XSParticleNode)parent;
                        XmlSchemaParticle particle = node.getXSComponent();
                        if (particle.getMaxOccurs() > 1) {
                            if (elementName.equals(node.getName())) {
                                XSTypeNode tNode = new XSTypeNode(node.getType());
                                node.add(tNode);

                                readAttributes(tNode, reader);
                                readElement(tNode, reader);
                                continue;
                            }

                            // we have reached a last element in a repeated sequence...
                            // move up to one level
                            parent = (DefaultMutableTreeNode)node.getParent();
                        }
                    }

                    XSAbstractNode node = (XSAbstractNode)parent;
                    XmlSchemaType type = node.getType();

                    if (type instanceof XmlSchemaComplexType) {
                        Map<QName, XmlSchemaElement> elements = node.getElements();
                        XmlSchemaElement element = elements.get(elementName);
                        if (element != null) {
                            XSParticleNode particleNode = new XSParticleNode(element);
                            parent.add(particleNode);

                            if (element.getMaxOccurs() > 1) {
                                XSTypeNode typeNode = new XSTypeNode(particleNode.getType());
                                particleNode.add(typeNode);

                                parent = particleNode;

                                readAttributes(typeNode, reader);
                                readElement(typeNode, reader);
                            } else {
                                readAttributes(particleNode, reader);
                                readElement(particleNode, reader);
                            }
                        }
                    } else {
                        throw new XMLStreamException("Simple type " + type.getQName() + " cannot have children! ");
                    }
                }
            } else if (eventType == XMLStreamReader.CHARACTERS ||
                       eventType == XMLStreamReader.CDATA) {
                text.append(reader.getText()); //.append(LINE_SEPARATOR);

                XSAbstractNode node = (XSAbstractNode)parent;
                XmlSchemaType type = node.getType();

                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                    if (complexType.isMixed()) {
                        XSMixedTextNode tNode = new XSMixedTextNode();
                        parent.add(tNode);

                        tNode.setUserObject(text.toString());
                        text.setLength(0);
                    }
                }
            }
        }
    }

    private void readAttributes(XSAbstractNode node, XMLStreamReader reader) throws XMLStreamException {
        Map<QName, XmlSchemaAttribute> attributes = node.getAttributes();
        
        for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
            QName attributeName = reader.getAttributeName(i);

            XmlSchemaAttribute attribute = attributes.get(attributeName);
            if (attribute != null) {
                XSAttributeNode attributeNode = new XSAttributeNode(attribute);
                node.add(attributeNode);
                String value = reader.getAttributeValue(i);
                if (value != null && value.length() > 0) {
                    XmlSchemaSimpleType simpleType = attributeNode.getSimpleType();
                    if (Constants.XSD_QNAME.equals(simpleType.getQName())) {
                        QName qname = DatatypeConverter.parseQName(value, reader.getNamespaceContext());
                        attributeNode.setUserObject(qname);
                    } else {
                        attributeNode.setUserObject(value);
                    }
                }
            }
        }
    }
    
    private XSAbstractNode findElement(QName elementName) {
        XmlSchemaElement element = schemas.getElementByQName(elementName);
        if (element != null) {
            return new XSGlobalElementNode(element);
        }

        // the type can be simple ("xs:string")
        XmlSchemaType type = schemas.getTypeByQName(elementName);
        if (type != null) {
            return new XSTypeNode(type);
        }
        return null;
    }

    private XSAbstractNode findType(QName typeName, QName partName) {
        XmlSchemaType type = schemas.getTypeByQName(typeName);
        if (type != null) {
            return new MessagePartNode(type, null, partName);
        }
        return null;
    }
}
