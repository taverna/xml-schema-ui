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

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import net.sf.taverna.xml.schema.parser.XSAttribute;
import net.sf.taverna.xml.schema.parser.XSModel;
import net.sf.taverna.xml.schema.parser.XSParticle;
import net.sf.taverna.xml.schema.parser.XSType;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * The root node for the XML Tree component.
 * 
 * @author Dmitry Repchevsky
 */

public class XSRootNode extends XSModel<TreeNode, MutableTreeNode> implements MutableTreeNode {
    @Override
    protected XSParticle newParticle(XmlSchemaParticle particle) {
        return new XSParticleNode(particle);
    }
    
    @Override
    protected XSType newType(XmlSchemaType type) {
        return new XSTypeNode(type);
    }
    
    @Override
    protected XSAttribute newAttribute(XmlSchemaAttribute attribute) {
        return new XSAttributeNode(attribute);
    }
}
