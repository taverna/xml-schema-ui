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

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.sf.taverna.xml.schema.ui.tree.node.XSAttributeNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSGlobalElementNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Dmitry Repchevsky
 */

public class ComplexType6Test extends TestSchemaTreeModel {

    @Before
    public void loadModel() {
        loadModel("xs/complex_type6.xsd");
    }

    @Test 
    public void complexTypeReadTest() {
        fillModel("xs/complex_type6.xml");
        testModel();
    }

    @Test
    public void complexTypeWriteTest() {
        fillModel();
        testModel();
    }
    
    private void fillModel() {
        model.addElement(new QName("http://example.com", "person"));

        DefaultMutableTreeNode root = model.getRoot();
        Assert.assertTrue("the root node must have one child node", root.getChildCount() == 1);
        
        TreeNode node = root.getChildAt(0);
        Assert.assertTrue("the node 'person' must be the represented by an element node", node instanceof XSGlobalElementNode);
        Assert.assertEquals("the node 'person' should have three child nodes", 3, node.getChildCount());

        for (int i = 0, n = node.getChildCount(); i <n; i++) {
            TreeNode child = node.getChildAt(i);
            Assert.assertTrue("the child node must be the represented by an attribute node", child instanceof XSAttributeNode);
            
            final XSAttributeNode attributeNode = (XSAttributeNode)child;
            final QName qname = attributeNode.getName();

            final String namespaceURI = qname.getNamespaceURI();
            Assert.assertEquals("wrong attribute namespace", "", namespaceURI);

            final String localPart = qname.getLocalPart();
            switch(localPart) {
                case "name": attributeNode.setUserObject("Napoleon");break;
                case "surname": attributeNode.setUserObject("Bonaparte");break;
                case "hobby": attributeNode.setUserObject("war");break;
                default: Assert.fail("unknown attribute node found");
            }
        }
    }
    
    private void testModel() {
        XMLOutputFactory f = XMLOutputFactory.newInstance();
        f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            XMLStreamWriter writer = f.createXMLStreamWriter(out);
            try {
                model.write(writer);
            } finally {
                writer.close();
            }
        } catch(XMLStreamException ex) {
            Assert.fail(ex.getMessage());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            Document doc = db.parse(new ByteArrayInputStream(out.toByteArray()));

            Element root = doc.getDocumentElement();
            Assert.assertNotNull("missed root ('person') element", root);
            Assert.assertEquals("wrong 'person' element type", Node.ELEMENT_NODE, root.getNodeType());
            Assert.assertEquals("wrong 'person' element name", "person", root.getLocalName());
            Assert.assertEquals("wrong 'person' element namespace", "http://example.com", root.getNamespaceURI());

            final NodeList children = root.getChildNodes();
            Assert.assertEquals("there shouldn't be any child nodes in the 'person' element", 0, children.getLength());

            final Attr attribute = root.getAttributeNode("name");
            Assert.assertNotNull("'name' attribute is missed in 'person' element", attribute);
            Assert.assertEquals("wrong 'name' attribute text content", "Napoleon", attribute.getValue());
            
        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public static void main(String[] args) throws IOException {
        ComplexType6Test test = new ComplexType6Test();
        test.init();
        test.loadModel();
        test.fillModel();
        test.runGUI();
    }

    private void runGUI() {
        javax.swing.JFrame frame = new javax.swing.JFrame("ComplexType Test 6");
        javax.swing.JTree tree = new javax.swing.JTree(model);
        tree.setToggleClickCount(1);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.setEditable(true);
        tree.setCellRenderer(new XSElementTreeCellEditor());
        tree.setCellEditor(new XSElementTreeCellEditor());
        
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tree);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
