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

package net.sf.taverna.t3.uicomponents.xml.schema.tree;

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
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSGlobalElementNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSParticleNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSTypeNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Dmitry Repchevsky
 */

public class ComplexType4Test extends TestSchemaTreeModel {

    @Before
    public void loadModel() {
        loadModel("xs/complex_type4.xsd");
    }

    @Test 
    public void complexTypeReadTest() {
        fillModel("xs/complex_type4.xml");
        testModel();
    }

    @Test
    public void complexTypeWriteTest() {
        fillModel();
        testModel();
    }
    
    private void fillModel() {

        //Adding the element to the model automatically constructs the tree structure
        model.addElement(new QName("http://example.com", "person"));

        DefaultMutableTreeNode root = model.getRoot();
        Assert.assertTrue("the root node must have one child node", root.getChildCount() == 1);
        
        TreeNode node = root.getChildAt(0);
        Assert.assertTrue("the node 'person' must be the represented by an element node", node instanceof XSGlobalElementNode);
        Assert.assertEquals("the node 'person' should have one child node", 1, node.getChildCount());
        
        TreeNode particleNode = node.getChildAt(0);
        Assert.assertTrue("the sequence node must be the represented by a particle node", particleNode instanceof XSParticleNode);
        Assert.assertEquals("the sequence node should have one child node", 1, particleNode.getChildCount());

        TreeNode node1 = particleNode.getChildAt(0);
        Assert.assertTrue("the 'hobby' node must be the represented by a particle node", node1 instanceof XSTypeNode);
        
        XSTypeNode hobby1 = (XSTypeNode)node1;
        hobby1.setUserObject("war");

        XSTypeNode hobby2 = new XSTypeNode(hobby1.getXSComponent());
        hobby2.setUserObject("archeology");
        ((XSParticleNode)particleNode).add(hobby2);

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
            
            NodeList children = root.getChildNodes();
            Assert.assertEquals("the element 'person' should have two child elements", 2, children.getLength());
            
            Node node1 = children.item(0);
            Assert.assertEquals("wrong first child element type", Node.ELEMENT_NODE, node1.getNodeType());
            Assert.assertEquals("wrong first child element name", "hobby", node1.getLocalName());
            Assert.assertEquals("wrong first child element namespace", "http://example.com", node1.getNamespaceURI());
            Assert.assertEquals("wrong first child element text content", "war", node1.getTextContent());
            
            Node node2 = children.item(1);
            Assert.assertEquals("wrong second child element type", Node.ELEMENT_NODE, node2.getNodeType());
            Assert.assertEquals("wrong second child element name", "hobby", node2.getLocalName());
            Assert.assertEquals("wrong second child element namespace", "http://example.com", node2.getNamespaceURI());
            Assert.assertEquals("wrong second child element text content", "archeology", node2.getTextContent());

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public static void main(String[] args) throws IOException {
        ComplexType4Test test = new ComplexType4Test();
        test.init();
        test.loadModel();
        test.fillModel();
        test.runGUI();
    }

    private void runGUI() {
        javax.swing.JFrame frame = new javax.swing.JFrame("ComplexType Test 4");
        javax.swing.JTree tree = new javax.swing.JTree(model);
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
