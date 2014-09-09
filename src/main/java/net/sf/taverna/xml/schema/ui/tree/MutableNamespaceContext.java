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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * @author Dmitry Repchevsky
 */

public class MutableNamespaceContext implements NamespaceContext {

    private final Map<QName, String> xmlns;
    
    public MutableNamespaceContext() {
        this.xmlns = new HashMap<>();
    }
    
    @Override
    public String getNamespaceURI(String prefix) {
        for (Map.Entry<QName, String> entry : xmlns.entrySet()) {
            QName name = entry.getKey();
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(name.getNamespaceURI()) &&
                prefix.equals(name.getLocalPart())) {
                return entry.getValue();
            }
        }
        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<QName, String> entry : xmlns.entrySet()) {
            QName name = entry.getKey();
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(name.getNamespaceURI()) &&
                namespaceURI.equals(entry.getValue())) {
                return name.getLocalPart();
            }
        }
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        
        List prefixes = new ArrayList();
        for (Map.Entry<QName, String> entry : xmlns.entrySet()) {
            QName name = entry.getKey();
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(name.getNamespaceURI()) &&
                namespaceURI.equals(entry.getValue())) {
                prefixes.add(name.getLocalPart());
            }
        }
        return prefixes.iterator();
    }

    public String putNamespaceURI(String prefix, String namespaceURI) {
        String pfx = getPrefix(namespaceURI);
        if (pfx == null) {
            pfx = prefix != null ? prefix : "ns1";

            int i = 1;
            while (!XMLConstants.NULL_NS_URI.equals(getNamespaceURI(pfx))) {
                pfx = "ns" + i++;
            }
            xmlns.put(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, pfx, XMLConstants.XMLNS_ATTRIBUTE), namespaceURI);
        }
        return pfx;
    }

    public String remove(String prefix) {
        return xmlns.remove(new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix));
    }
}
