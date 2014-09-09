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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

/**
 * @author Dmitry Repchevsky
 * 
 * The abstract TreeNode to represent XML Schema structures
 * 
 * @param <T>
 */

public abstract class XSAbstractNode<T extends XmlSchemaObject> extends DefaultMutableTreeNode {

    protected T component;

    public XSAbstractNode(T component) {
        this(component, null);
    }

    public XSAbstractNode(T component, Object object) {
        super(object);
        this.component = component;
    }

    public T getXSComponent() {
        return component;
    }

    public abstract QName getName();
    public abstract XmlSchemaType getType();

    public final QName getTypeName() {
        XmlSchemaType type = getType();

        // the type could be anonymouse
        QName qname = type.getQName();
        
        return qname != null ? new QName(qname.getNamespaceURI(), qname.getLocalPart()) : new QName("");
    }

    public abstract Boolean validate();
    public abstract void write(XMLStreamWriter stream) throws XMLStreamException;

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

    /**
     * Returns closest simple type that corresponds to this node text content.
     * In simple it returns a simple type if node is editable or null otherwise.
     * 
     * @return a simple type for the node content or null.
     */
    public XmlSchemaSimpleType getSimpleType() {
        final XmlSchemaType type = getType();
        return type == null ? null : getSimpleType(type);
    }
    
    private XmlSchemaSimpleType getSimpleType(XmlSchemaType type) {
        if (type instanceof XmlSchemaSimpleType) {
            return (XmlSchemaSimpleType)type;
        }
        
        XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
        return getSimpleType(complexType);        
    }
    
    private XmlSchemaSimpleType getSimpleType(XmlSchemaComplexType complexType) {
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

    public Map<QName, XmlSchemaElement> getElements() {
        Map<QName, XmlSchemaElement> elements = new LinkedHashMap<>();
        XmlSchemaType type = getType();
        if (type instanceof XmlSchemaComplexType) {
            addElements(elements, (XmlSchemaComplexType)type);
        }
        return elements;
    }

    public Map<QName, XmlSchemaAttribute> getAttributes() {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<>();
        XmlSchemaType type = getType();
        if (type instanceof XmlSchemaComplexType) {
            addAttributes(attributes, (XmlSchemaComplexType)type);
        }
        return attributes;
    }

    /**
     * Builds the tree branch (child nodes) for this node.
     */
    public void parse() {
        XmlSchemaType type = getType();
        if (type instanceof XmlSchemaComplexType) {
            addComplexType((XmlSchemaComplexType)type);
        }
    }

    private void addComplexType(XmlSchemaComplexType complexType) {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<>();
        addAttributes(attributes, complexType.getAttributes());

        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            addParticle(particle);
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
                    addComplexType((XmlSchemaComplexType) baseType);
                    
                    particle = complexContentExtension.getParticle();
                    addParticle(particle);
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                    addAttributes(attributes, complexContentRestriction.getAttributes());
                    particle = complexContentRestriction.getParticle();
                    addParticle(particle);
                } else if (content instanceof XmlSchemaSimpleContentExtension) {
                    XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
                    addAttributes(attributes, simpleContentExtension.getAttributes());
                } 
            }
        }
        
        for (XmlSchemaAttribute attribute : attributes.values()) {
            XSAbstractNode child = new XSAttributeNode(attribute);
            String value = attribute.getDefaultValue();
            if (value != null) {
                child.setUserObject(value);
            }
            insert(child, 0);
        }
    }

    private void addAttributes(Map<QName, XmlSchemaAttribute> attributes, List<XmlSchemaAttributeOrGroupRef> attributeOrGroupRefs) {
        for (XmlSchemaAttributeOrGroupRef attributeOrGroupRef : attributeOrGroupRefs) {
            addAttributes(attributes, (XmlSchemaAttributeGroupMember)attributeOrGroupRef);
        }
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
    
    private void addElements(Map<QName, XmlSchemaElement> elements, XmlSchemaComplexType complexType) {
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

    private void addElement(Map<QName, XmlSchemaElement> elements, List<? extends XmlSchemaObjectBase> items) {
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

    public void addParticle(XmlSchemaParticle particle) {

        if (particle instanceof XmlSchemaElement) {
            XSParticleNode node = new XSParticleNode(particle);
            node.parse();
            add(node);

            XmlSchemaElement element = (XmlSchemaElement)particle;
            String value = element.getDefaultValue();
            if (value != null) {
                node.setUserObject(value);
            }

            XmlSchemaType type = node.getType();
            
            if (element.getMaxOccurs() > 1) {
                XSTypeNode tNode = new XSTypeNode(type);
                node.add(tNode);
//                for (int i = 0, n = Math.max(1, (int)particle.getMinOccurs()); i < n; i++) {
//                    XSTypeNode tNode = new XSTypeNode(type);
//                    node.add(tNode);
//                }
            }
//            else {
//                node.addType();
//            }
        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
            List<XmlSchemaSequenceMember> items = sequence.getItems();
            for (XmlSchemaSequenceMember item : items) {
                addParticle((XmlSchemaParticle) item);
            }
        } else if (particle instanceof XmlSchemaAll) {
            XmlSchemaAll all = (XmlSchemaAll)particle;
            List<XmlSchemaElement> elements = all.getItems();
            for (XmlSchemaElement element : elements) {
                addParticle(element);
            }
        } else if (particle instanceof XmlSchemaChoice) {
            XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice)particle;
            List<XmlSchemaObject> items = xmlSchemaChoice.getItems();
            
            // for the choice add only one element
            addParticle((XmlSchemaParticle) items.get(0));
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

    protected XmlSchemaElement getElement(XmlSchemaElement element) {
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
}
