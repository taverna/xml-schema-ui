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
import java.io.InputStream;
import javax.swing.tree.TreeNode;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sf.taverna.xml.schema.ui.tree.node.XSGlobalElementNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSRootNode;
import net.sf.taverna.xml.schema.ui.tree.node.XSParticleNode;
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

public class ComplexType1Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "xs/complex_type1.xml";
    private final static String XSD_FILE = "xs/complex_type1.xsd";

    @Before
    public void loadModel() {
        loadModel(XSD_FILE);
    }

    @Test 
    public void complexTypeReadTest() {
        fillModel(XML_FILE);
        testModel();
    }

    @Test
    public void complexTypeWriteTest() {
        fillModel();
        testModel();
    }
    
    @Test
    public void XPathTest() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        InputStream in = ComplexType1Test.class.getClassLoader().getResourceAsStream(XML_FILE);
        Document doc;
        try {
            doc = db.parse(in);
        } finally {
            in.close();
        }
        
        XPath xpath = XPathFactory.newInstance().newXPath();

        fillModel(XML_FILE);
        
        XSRootNode root = model.getRoot();
        XSGlobalElementNode element = (XSGlobalElementNode)root.getChildAt(0);
        
        XSParticleNode particle1 = (XSParticleNode)element.getChildAt(0);
        Object name_value = xpath.evaluate(particle1.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the 'name' element", particle1.getUserObject(), name_value);
        
        XSParticleNode particle2 = (XSParticleNode)element.getChildAt(1);
        Object surname_value = xpath.evaluate(particle2.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the 'surname' element", particle2.getUserObject(), surname_value);
    }
    
    private void fillModel() {
        model.addGlobalElement(new QName("http://example.com","person"));

        XSRootNode root = model.getRoot();
        Assert.assertTrue("the root node must have one child node", root.getChildCount() == 1);
        
        TreeNode node = root.getChildAt(0);
        Assert.assertEquals("the node 'person' should have two child nodes", 2, node.getChildCount());
        Assert.assertTrue("the node 'person' must be represented by an element node", node instanceof XSGlobalElementNode);
        
        TreeNode node1 = node.getChildAt(0);
        Assert.assertTrue("the node 'name' must be represented by a particle node", node1 instanceof XSParticleNode);

        XSParticleNode particle1 = (XSParticleNode)node1;
        particle1.setUserObject("Napoleon");

        TreeNode node2 = node.getChildAt(1);
        Assert.assertTrue("the node 'surname' must be represented by a particle node", node2 instanceof XSParticleNode);
        
        XSParticleNode particle2 = (XSParticleNode)node2;
        particle2.setUserObject("Bonaparte");
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
            Assert.assertEquals("the element '<person>' should have two child nodes", 2, children.getLength());
            
            Node node1 = children.item(0);
            Assert.assertEquals("wrong first child node type", Node.ELEMENT_NODE, node1.getNodeType());
            Assert.assertEquals("wrong first child element name", "name", node1.getLocalName());
            Assert.assertEquals("wrong 'name' element namespace", "http://example.com", node1.getNamespaceURI());
            Assert.assertEquals("wrong '{http://example.com}name' text content", "Napoleon", node1.getTextContent());
            
            Node node2 = children.item(1);
            Assert.assertEquals("wrong second child node type", Node.ELEMENT_NODE, node2.getNodeType());
            Assert.assertEquals("wrong second child element name", "surname", node2.getLocalName());
            Assert.assertEquals("wrong 'surname' element namespace", "http://example.com", node2.getNamespaceURI());
            Assert.assertEquals("wrong '{http://example.com}surname' element text content", "Bonaparte", node2.getTextContent());

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public static void main(String[] args) throws IOException {
        ComplexType1Test test = new ComplexType1Test();
        test.init();
        test.loadModel();
        test.fillModel();
        test.runGUI();
    }

    private void runGUI() {
        javax.swing.JFrame frame = new javax.swing.JFrame("ComplexType Test 1");
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
