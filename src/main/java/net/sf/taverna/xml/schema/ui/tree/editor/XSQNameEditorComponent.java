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

import java.util.regex.Pattern;
import javax.swing.JTextField;
import javax.swing.tree.TreeCellEditor;
import javax.xml.namespace.QName;

/**
 * @author Dmitry Repchevsky
 */

public class XSQNameEditorComponent extends XSTextEditorComponent<JTextField> {

    private final static Pattern PATTERN = Pattern.compile("^(\\{(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?\\})?([a-zA-Z_][a-zA-Z0-9_\\-]*)");

    public XSQNameEditorComponent(final TreeCellEditor cEditor) {
       super(cEditor, new JTextField("{}"));
    }

    @Override
    public Object parse(String value) throws IllegalArgumentException {
        if (PATTERN.matcher(value).matches()) {
            return QName.valueOf(value);
        }
        throw new IllegalArgumentException();
    }
}
