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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import net.sf.taverna.xml.schema.ui.tree.node.MessagePartNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSAttributeNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSGlobalElementNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSMixedTextNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSParticleNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSTypeNode;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupMember;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

/**
 * XML Schema model class that represent the root node and contains a set of auxiliary
 * methods to manage the tree.
 * 
 * @author Dmitry Repchevsky
 */

public class XSModel<T, V extends T> extends XSNode<T,V> {
    private XmlSchemaCollection schemas;

    public XSModel() {
        schemas = new XmlSchemaCollection();
    }

    /**
     * Binds XML Schemas to the model. Execution of this method cleans the model (tree).
     * 
     * @param schemas new XML Schemas this model is based on.
     */
    public void setSchemaCollection(XmlSchemaCollection schemas) {
        removeAllChildren();
        this.schemas = schemas;
    }
    
    /**
     * Finds a node that corresponds to the provided XPath
     * 
     * @param xpath XPath expression for the queried node
     * 
     * @return the node in the model that corresponds to the XPath query
     */
    public XSNode<T,V> findNode(String xpath) {
        return findComponent(this, xpath);
    }
    
    private XSComponent findComponent(XSNode<T,V> node, String xpath) {
        if (node instanceof XSComponent) {
            XSComponent component = (XSComponent)node;
            if (xpath.equals(component.getXPath())) {
                return component;
            }
        }
        
        for (int i = 0, n = node.getChildCount(); i < n; i++) {
            XSNode<T, V> childNode = (XSNode<T, V>)node.getChildAt(i);
            XSComponent component = findComponent(childNode, xpath);
            if (component != null) {
                return component;
            }
        }
        
        return null;
    }

    /**
     * Includes the element (with all its subelements obtained from schema) to the model.
     *
     * @param element the element name to include into the model.
     */
    public void addGlobalElement(QName element) {
        XSComponent node = (XSComponent)findElement(element);

        if (node != null) {
            parse(node);
            insert((V)node, getChildCount());
        }
    }

    /**
     * Includes the type (with all its subelements obtained from schema) to the model.
     *
     * @param type
     * @param name
     */
    public void addGlobalType(QName type, QName name) {
        MessagePartNode node = findType(type, name);
        if (node != null) {
            parse(node);
            insert((V)node, getChildCount());
        }
    }

    /**
     * Validates the model tree against provided XML Schemas.
     * Basically it validates whether all obligatory components have their values set.
     * 
     * @return true if model is valid, false otherwise.
     */
    public boolean validate() {
        Boolean valid = null;

        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSComponent node = (XSComponent)getChildAt(i);

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
     * Constructs the model based on XML obtained from the provided stream
     *
     * @param stream The XML reader where the XML is read from
     * @throws javax.xml.stream.XMLStreamException
     */
    public void read(XMLStreamReader stream) throws XMLStreamException {
        removeAllChildren();

        readElement(this, stream);
    }

    /**
     * Constructs an XML based on the data provided by the model.
     * 
     * @param stream XML writer to write generated XML
     * @throws javax.xml.stream.XMLStreamException
     */
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSComponent node = (XSComponent)getChildAt(i);
            node.write(stream);
        }
    }

    protected XSParticle newParticle(XmlSchemaParticle particle) {
        return new XSParticle(particle);
    }
    
    protected XSType newType(XmlSchemaType type) {
        return new XSType(type);
    }
    
    protected XSAttribute newAttribute(XmlSchemaAttribute attribute) {
        return new XSAttribute(attribute);
    }

    /**
     * Builds the tree branch (child nodes) for the node.
     */
    private void parse(XSComponent node) {
        XmlSchemaType type = node.getType();
        if (type instanceof XmlSchemaComplexType) {
            addComplexType(node, (XmlSchemaComplexType)type);
        }
    }

    private void readElement(XSNode parent, XMLStreamReader reader) throws XMLStreamException {
        StringBuilder text = new StringBuilder();

        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT) {
                if (text.length() > 0 && parent instanceof XSComponent) {
                    XSComponent node = (XSComponent)parent;
                    if (XSModel.getSimpleType(node) != null) {
                        node.setUserObject(text.toString());
                    }
                }
                return;
            } else if (eventType == XMLStreamReader.START_ELEMENT) {
                QName elementName = reader.getName();

                if (parent.getParent() == null) {
                    XSGlobalElementNode node = findElement(elementName);
                    if (node != null) {
                        parent.insert(node, parent.getChildCount());
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
                                node.insert(tNode, node.getChildCount());
                                readAttributes(tNode, reader);
                                readElement(tNode, reader);
                                continue;
                            }

                            // we have reached a last element in a repeated sequence...
                            // move up to one level
                            parent = (XSNode)node.getParent();
                        }
                    }

                    XSComponent node = (XSComponent)parent;
                    XmlSchemaType type = node.getType();

                    if (type instanceof XmlSchemaComplexType) {
                        Map<QName, XmlSchemaElement> elements = getElements(node);
                        XmlSchemaElement element = elements.get(elementName);
                        if (element != null) {
                            XSParticleNode particleNode = new XSParticleNode(element);
                            node.insert(particleNode, node.getChildCount());

                            if (element.getMaxOccurs() > 1) {
                                XSTypeNode typeNode = new XSTypeNode(particleNode.getType());
                                particleNode.insert(typeNode, particleNode.getChildCount());
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
            } else if (parent.getParent() != null && 
                      (eventType == XMLStreamReader.CHARACTERS ||
                       eventType == XMLStreamReader.CDATA)) {
                text.append(reader.getText()); //.append(LINE_SEPARATOR);

                XSComponent node = (XSComponent)parent;
                XmlSchemaType type = node.getType();

                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                    if (complexType.isMixed()) {
                        XSMixedTextNode tNode = new XSMixedTextNode();
                        parent.insert(tNode, parent.getChildCount());

                        tNode.setUserObject(text.toString());
                        text.setLength(0);
                    }
                }
            }
        }
    }

    private void readAttributes(XSComponent node, XMLStreamReader reader) throws XMLStreamException {
        Map<QName, XmlSchemaAttribute> attributes = getAttributes(node);
        
        for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
            QName attributeName = reader.getAttributeName(i);

            XmlSchemaAttribute attribute = attributes.get(attributeName);
            if (attribute != null) {
                XSAttributeNode attributeNode = new XSAttributeNode(attribute);
                node.insert(attributeNode, node.getChildCount());
                String value = reader.getAttributeValue(i);
                if (value != null && value.length() > 0) {
                    XmlSchemaSimpleType simpleType = XSModel.getSimpleType(attributeNode);
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
    
    private XSGlobalElementNode findElement(QName elementName) {
        XmlSchemaElement element = schemas.getElementByQName(elementName);
        if (element != null) {
            return new XSGlobalElementNode(element);
        }

//        // the type can be simple ("xs:string")
//        XmlSchemaType type = schemas.getTypeByQName(elementName);
//        if (type != null) {
//            return new XSTypeNode(type);
//        }
        return null;
    }

    private MessagePartNode findType(QName typeName, QName partName) {
        XmlSchemaType type = schemas.getTypeByQName(typeName);
        if (type != null) {
            return new MessagePartNode(type, null, partName);
        }
        return null;
    }

    private void addComplexType(XSComponent component, XmlSchemaComplexType complexType) {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<>();
        addAttributes(attributes, complexType.getAttributes());

        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            addParticle(component, particle);
        } else {
            XmlSchemaContentModel contentModel = complexType.getContentModel();
            if (contentModel != null) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaComplexContentExtension) {
                    XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                    addAttributes(attributes, complexContentExtension.getAttributes());
                    
                    final QName baseTypeName = complexContentExtension.getBaseTypeName();
                    final XmlSchemaType baseType;
                    final XmlSchema xmlSchema = complexType.getParent();
                    final XmlSchemaCollection xmlSchemaCollection = xmlSchema.getParent();
                    if (xmlSchemaCollection != null) {
                        baseType = xmlSchemaCollection.getTypeByQName(baseTypeName);
                    } else {
                        baseType = xmlSchema.getTypeByName(baseTypeName);
                    }
                    addComplexType(component, (XmlSchemaComplexType)baseType);
                    
                    particle = complexContentExtension.getParticle();
                    addParticle(component, particle);
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                    addAttributes(attributes, complexContentRestriction.getAttributes());
                    particle = complexContentRestriction.getParticle();
                    addParticle(component, particle);
                } else if (content instanceof XmlSchemaSimpleContentExtension) {
                    XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
                    addAttributes(attributes, simpleContentExtension.getAttributes());
                } 
            }
        }
        
        for (XmlSchemaAttribute attribute : attributes.values()) {
            XSAttribute child = newAttribute(attribute);
            String value = attribute.getDefaultValue();
            if (value != null) {
                child.setUserObject(value);
            }
            component.insert((V)child, 0);
        }
    }

    private Map<QName, XmlSchemaAttribute> getAttributes(XSComponent component) {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<>();
        XmlSchemaType type = component.getType();
        if (type instanceof XmlSchemaComplexType) {
            addAttributes(attributes, (XmlSchemaComplexType)type);
        }
        return attributes;
    }

    private void addAttributes(Map<QName, XmlSchemaAttribute> attributes, XmlSchemaComplexType complexType) {
        addAttributes(attributes, complexType.getAttributes());

        XmlSchemaContentModel contentModel = complexType.getContentModel();
        if (contentModel != null) {
            XmlSchemaContent content = contentModel.getContent();
            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                addAttributes(attributes, complexContentExtension.getAttributes());

                final QName baseTypeName = complexContentExtension.getBaseTypeName();
                final XmlSchemaType baseType;
                final XmlSchema xmlSchema = complexType.getParent();
                final XmlSchemaCollection xmlSchemaCollection = xmlSchema.getParent();
                if (xmlSchemaCollection != null) {
                    baseType = xmlSchemaCollection.getTypeByQName(baseTypeName);
                } else {
                    baseType = xmlSchema.getTypeByName(baseTypeName);
                }
                addAttributes(attributes, (XmlSchemaComplexType) baseType);
            } else if (content instanceof XmlSchemaComplexContentRestriction) {
                XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                addAttributes(attributes, complexContentRestriction.getAttributes());
            } else if (content instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
                addAttributes(attributes, simpleContentExtension.getAttributes());
            }
        }
    }

    private void addAttributes(Map<QName, XmlSchemaAttribute> attributes, List<XmlSchemaAttributeOrGroupRef> attributeOrGroupRefs) {
        for (XmlSchemaAttributeOrGroupRef attributeOrGroupRef : attributeOrGroupRefs) {
            addAttributes(attributes, (XmlSchemaAttributeGroupMember)attributeOrGroupRef);
        }
    }

    private void addAttributes(Map<QName, XmlSchemaAttribute> attributes, XmlSchemaAttributeGroupMember attributeGroupMember) {
        if (attributeGroupMember instanceof XmlSchemaAttribute) {
            XmlSchemaAttribute attribute = getAttribute((XmlSchemaAttribute)attributeGroupMember);
            attributes.put(attribute.getWireName(), attribute);
        } else {
            XmlSchemaAttributeGroup attributeGroup;
            if (attributeGroupMember instanceof XmlSchemaAttributeGroup) {
                attributeGroup = (XmlSchemaAttributeGroup)attributeGroupMember;
            }
            else {
                XmlSchemaAttributeGroupRef attributeGroupRef = (XmlSchemaAttributeGroupRef)attributeGroupMember;
                XmlSchemaRef<XmlSchemaAttributeGroup> ref = attributeGroupRef.getRef();
                attributeGroup = ref.getTarget();
            }

            List<XmlSchemaAttributeGroupMember> attributeGroupMembers = attributeGroup.getAttributes();
            for (XmlSchemaAttributeGroupMember xmlSchemaAttributeGroupMember : attributeGroupMembers) {
                addAttributes(attributes, xmlSchemaAttributeGroupMember);
            }
        }
    }

    public final static Map<QName, XmlSchemaElement> getElements(XSComponent component) {
        Map<QName, XmlSchemaElement> elements = new LinkedHashMap<>();
        XmlSchemaType type = component.getType();
        if (type instanceof XmlSchemaComplexType) {
            addElements(elements, (XmlSchemaComplexType)type);
        }
        return elements;
    }
        
    private static void addElements(Map<QName, XmlSchemaElement> elements, XmlSchemaComplexType complexType) {
        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            addElement(elements, Arrays.asList(particle));
        } else {
            XmlSchemaContentModel contentModel = complexType.getContentModel();
            XmlSchemaContent content = contentModel.getContent();
            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                QName baseTypeName = complexContentExtension.getBaseTypeName();

                XmlSchema schema = complexType.getParent();
                XmlSchemaCollection schemaCollection = schema.getParent();

                final XmlSchemaType baseType;
                if (schemaCollection != null) {
                    baseType = schemaCollection.getTypeByQName(baseTypeName);
                } else {
                    baseType = schema.getTypeByName(baseTypeName);
                }

                if (baseType instanceof XmlSchemaComplexType) {
                    addElements(elements, (XmlSchemaComplexType)baseType);
                }
                
                particle = complexContentExtension.getParticle();
                if (particle != null) {
                    addElement(elements, Arrays.asList(particle));
                }
            } else if (content instanceof XmlSchemaComplexContentRestriction) {
                XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                particle = complexContentRestriction.getParticle();
                if (particle != null) {
                    addElement(elements, Arrays.asList(particle));
                }
            }
        }
    }

    private static void addElement(Map<QName, XmlSchemaElement> elements, List<? extends XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase item : items) {
            if (item instanceof XmlSchemaElement) {
                XmlSchemaElement element = getElement((XmlSchemaElement)item);
                elements.put(element.getQName(), element);
            } else if(item instanceof XmlSchemaSequence) {
                final XmlSchemaSequence sequence = (XmlSchemaSequence)item;
                addElement(elements, sequence.getItems());
            } else if (item instanceof XmlSchemaChoice) {
                XmlSchemaChoice choice = (XmlSchemaChoice)item;
                addElement(elements, choice.getItems());
            } else if (item instanceof XmlSchemaGroup) {
                XmlSchemaGroup group = (XmlSchemaGroup)item;
                XmlSchemaGroupParticle groupParticle = group.getParticle();
                addElement(elements, Arrays.asList(groupParticle));
            } else if (item instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef groupRef = (XmlSchemaGroupRef)item;
                XmlSchemaGroupParticle groupParticle = groupRef.getParticle();
                addElement(elements, Arrays.asList(groupParticle));
            }
        }
    }

    /**
     * Finds the attribute declaration.
     * <xs:attribute name="surname" type="xs:string" />
     * ...
     * <xs:attribute ref="surname"/>
     * 
     * 
     * @param attribute the attribute for which is declaration is needed
     * 
     * @return attribute declaration which is either a referred attribute or
     * attribute itself.
     */
    private XmlSchemaAttribute getAttribute(XmlSchemaAttribute attribute) {
        if (attribute.isRef()) {
            XmlSchemaRef<XmlSchemaAttribute> ref = attribute.getRef();
            XmlSchemaAttribute refAttribute = ref.getTarget();
            if (refAttribute != null) {
                return refAttribute;
            }
            QName targetName = ref.getTargetQName();
            XmlSchema schema = attribute.getParent();
            XmlSchemaCollection schemaCollection = schema.getParent();
            if (schemaCollection != null) {
                attribute = schemaCollection.getAttributeByQName(targetName);
            } else {
                attribute = schema.getAttributeByName(targetName);
            }
        }
        return attribute;
    }

    public final static XmlSchemaElement getElement(XmlSchemaElement element) {
        XmlSchemaRef<XmlSchemaElement> ref = element.getRef();
        if (ref != null) {
            XmlSchemaElement refElement = ref.getTarget();
            if (refElement != null) {
                return refElement;
            }
            QName targetName = ref.getTargetQName();
            if (targetName != null) {
                XmlSchema schema = element.getParent();
                XmlSchemaCollection schemaCollection = schema.getParent();
                if (schemaCollection != null) {
                    element = schemaCollection.getElementByQName(targetName);
                } else {
                    element = schema.getElementByName(targetName);
                }
            }
        }
        return element;
    }

    /**
     * Returns closest simple type that corresponds to this node text content.
     * In simple it returns a simple type if node is editable or null otherwise.
     * 
     * @return a simple type for the node content or null.
     */
    public final static XmlSchemaSimpleType getSimpleType(XSComponent component) {
        final XmlSchemaType type = component.getType();
        return type == null ? null : getSimpleType(type);
    }
    
    private static XmlSchemaSimpleType getSimpleType(XmlSchemaType type) {
        if (type instanceof XmlSchemaSimpleType) {
            return (XmlSchemaSimpleType)type;
        }
        
        XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
        return getSimpleType(complexType);        
    }
    
    private static XmlSchemaSimpleType getSimpleType(XmlSchemaComplexType complexType) {
        XmlSchemaContentModel contentModel = complexType.getContentModel();
        if (contentModel == null) {
            return null;
        }
        
        XmlSchemaContent content = contentModel.getContent();
        
        QName baseTypeName;
        if (content instanceof XmlSchemaComplexContentExtension) {
            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
            baseTypeName = complexContentExtension.getBaseTypeName();
        } else if (content instanceof XmlSchemaComplexContentRestriction) {
            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
            baseTypeName = complexContentRestriction.getBaseTypeName();
        } else if (content instanceof XmlSchemaSimpleContentExtension) {
            XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
            baseTypeName = simpleContentExtension.getBaseTypeName();
        } else {
            return null; // shouldn't happen
        }
        
        XmlSchemaType baseType;
        XmlSchema schema = complexType.getParent();
        XmlSchemaCollection schemaCollection = schema.getParent();
        if (schemaCollection != null) {
            baseType = schemaCollection.getTypeByQName(baseTypeName);
        } else {
            baseType = schema.getTypeByName(baseTypeName);
        }
        
        return getSimpleType(baseType);
    }

    public void addParticle(XSComponent component, XmlSchemaParticle particle) {
        if (particle instanceof XmlSchemaElement) {
            XSParticle node = newParticle(particle);
            //parse(node);
            component.insert((V)node, component.getChildCount());

            XmlSchemaElement element = (XmlSchemaElement)particle;
            String value = element.getDefaultValue();
            if (value != null) {
                node.setUserObject(value);
            }

            
            
            if (element.getMaxOccurs() <= 1) {
                parse(node);
            } else {
                XmlSchemaType type = node.getType();
                XSType tNode = newType(type);
                parse(tNode);
                node.insert(tNode, node.getChildCount());
//                for (int i = 0, n = Math.max(1, (int)particle.getMinOccurs()); i < n; i++) {
//                    XSTypeNode tNode = new XSTypeNode(type);
//                    node.add(tNode);
//                }
            }
        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
            List<XmlSchemaSequenceMember> items = sequence.getItems();
            for (XmlSchemaSequenceMember item : items) {
                addParticle(component, (XmlSchemaParticle)item);
            }
        } else if (particle instanceof XmlSchemaAll) {
            XmlSchemaAll all = (XmlSchemaAll)particle;
            List<XmlSchemaElement> elements = all.getItems();
            for (XmlSchemaElement element : elements) {
                addParticle(component, element);
            }
        } else if (particle instanceof XmlSchemaChoice) {
            XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice)particle;
            List<XmlSchemaObject> items = xmlSchemaChoice.getItems();
            
            // for the choice add only one element
            addParticle(component, (XmlSchemaParticle)items.get(0));
        }
    }
}
