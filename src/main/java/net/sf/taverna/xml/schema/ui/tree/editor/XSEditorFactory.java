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

package net.sf.taverna.xml.schema.ui.tree.editor;

import javax.swing.tree.TreeCellEditor;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;

/**
 * @author Dmitry Repchevsky
 */

public class XSEditorFactory {
    public static XSEditorInterface getXSEditorComponent(TreeCellEditor cEditor, XmlSchemaSimpleType xmlSchemaSimpleType) {
        while(true) {
            final QName qname = xmlSchemaSimpleType.getQName();
            if (qname != null && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(qname.getNamespaceURI())) {
                String name = qname.getLocalPart();
                switch(name) {
                    case "string": return new XSStringEditorComponent(cEditor);
                    case "boolean": return new XSBooleanComponent(cEditor);
                    case "integer": return new XSIntegerEditorComponent(cEditor);
                    case "long": return new XSLongEditorComponent(cEditor);
                    case "int": return new XSIntEditorComponent(cEditor);
                    case "short": return new XSShortEditorComponent(cEditor);
                    case "byte": return new XSByteEditorComponent(cEditor);
                    case "unsignedInt": return new XSUnsignedIntEditorComponent(cEditor);
                    case "unsignedShort": return new XSUnsignedIntEditorComponent(cEditor);
                    case "double": return new XSDoubleEditorComponent(cEditor);
                    case "decimal": return new XSDecimalEditorComponent(cEditor);
                    case "float": return new XSFloatEditorComponent(cEditor);
                    case "time": return new XSTimeEditorComponent(cEditor);
                    case "date": return new XSDateEditorComponent(cEditor);
                    case "dateTime": return new XSDateTimeEditorComponent(cEditor);
                    case "anySimpleType": return new XSAnySimpleTypeEditorComponent(cEditor);
                    case "QName": return new XSQNameEditorComponent(cEditor);
                    case "base64Binary": return new XSBase64BinaryEditorComponent(cEditor);
                }
            }

            XmlSchemaSimpleTypeContent xmlSchemaSimpleTypeContent = xmlSchemaSimpleType.getContent();
            if (xmlSchemaSimpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
                XmlSchemaSimpleTypeRestriction XmlSchemaSimpleTypeRestriction = (XmlSchemaSimpleTypeRestriction)xmlSchemaSimpleTypeContent;
                xmlSchemaSimpleType = XmlSchemaSimpleTypeRestriction.getBaseType();
            } else {
                break; // no support for unions or lists
            }
        }

        return null;
    }
}
