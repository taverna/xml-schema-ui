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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import net.sf.taverna.xml.schema.parser.XSNode;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import net.sf.taverna.xml.schema.ui.tree.node.XSRootNode;

/**
 * @author Dmitry Repchevsky
 */

public class SchemaTreeModel extends DefaultTreeModel {

    public SchemaTreeModel() {
        super(new XSRootNode());
    }

    @Override
    public XSRootNode getRoot() {
        return (XSRootNode)super.getRoot();
    }

    /**
     * Clears the tree model
     */
    public void clear() {
        XSRootNode raiz = getRoot();
        raiz.removeAllChildren();
        nodeStructureChanged(raiz);
    }

    public void setSchemaCollection(XmlSchemaCollection schemas) {
        clear();
        getRoot().setSchemaCollection(schemas);
    }

    public boolean validate() {
        return getRoot().validate();
    }

    /**
     * Includes the element (with all its subelements obtained from schema) to the model
     *
     * @param element the element name to include into the model
     */
    public void addGlobalElement(QName element) {
        getRoot().addGlobalElement(element);
        nodeStructureChanged(root);
    }

    /**
     * Includes the type (with all its subelements obtained from schema) to the model
     *
     * @param type
     * @param name
     */
    public void addGlobalType(QName type, QName name) {
        getRoot().addGlobalType(type, name);
        nodeStructureChanged(root);
    }

    /**
     * Constructs the model based on XML obtained from the provided stream
     *
     * @param stream The XML reader where the XML is read from
     * @throws javax.xml.stream.XMLStreamException
     */
    public void read(XMLStreamReader stream) throws XMLStreamException {
        getRoot().read(stream);
        nodeStructureChanged(root);
    }

    /**
     * Constructs an XML based on the data provided by the model
     * @param stream XML writer to write generated XML
     * @throws javax.xml.stream.XMLStreamException
     */
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        getRoot().write(stream);
    }
    
    /**
     * Returns the value of the node identified by the XPath query
     * 
     * @param xpath XPath query of the node
     * 
     * @return the value of the node identified by the XPath query
     */
    public Object getNodeValue(String xpath) {
        XSNode node = getRoot().findNode(xpath);
        return node == null ? null : node.getUserObject();
    }
    
    /**
     * Sets the value for the node identified by the XPath query
     * 
     * @param xpath XPath query of the node
     * @param value the value to set
     */
    public void setNodeValue(String xpath, Object value) {
        XSNode node = getRoot().findNode(xpath);
        if (node != null) {
            node.setUserObject(value);
            fireTreeNodesChanged(node, getPathToRoot((TreeNode)node), null, null);
        }
    }
}
