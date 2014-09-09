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

package net.sf.taverna.t3.uicomponents.xml.schema.tree.node;

import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * @author Dmitry Repchevsky
 */

public class MessagePartNode extends XSTypeNode {
    private QName name;

    public MessagePartNode(XmlSchemaType xmlSchemaType) {
        this(xmlSchemaType, null);
    }

    public MessagePartNode(XmlSchemaType xmlSchemaType, Object object) {
        this(xmlSchemaType, object, null);
    }

    public MessagePartNode(XmlSchemaType xmlSchemaType, Object object, QName name) {
        super(xmlSchemaType, object);
        this.name = name;
    }

    @Override
    public QName getName() {
        return name != null ? name : super.getName();
    }
}
