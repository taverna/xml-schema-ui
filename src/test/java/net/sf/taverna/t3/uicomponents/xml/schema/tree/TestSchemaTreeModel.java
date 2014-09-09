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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Dmitry Repchevsky
 */

public abstract class TestSchemaTreeModel {
    
    SchemaTreeModel model;
    
    @Before
    public void init() {
        model = new SchemaTreeModel();
    }

    /**
     * Loads the model from provided XML Schema file
     * 
     * @param xsd XML Schema URI location
     */
    void loadModel(final String xsd) {
        XmlSchemaCollection schemas = new XmlSchemaCollection();
        
        try {
            InputStream in = SimpleType1Test.class.getClassLoader().getResourceAsStream(xsd);
            try {
                schemas.read(new InputStreamReader(in));
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }

        model.setSchemaCollection(schemas);
    }
    
    /**
     * Fills the model with provided XML file
     * 
     * @param xml XML document URI location
     */
    void fillModel(final String xml) {
        try {
            InputStream in = SimpleType1Test.class.getClassLoader().getResourceAsStream(xml);

            try {
                XMLInputFactory f = XMLInputFactory.newInstance();
                XMLStreamReader reader = f.createXMLStreamReader(in);
                model.read(reader);
            } catch(XMLStreamException ex) {
                Assert.fail(ex.getMessage());
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
