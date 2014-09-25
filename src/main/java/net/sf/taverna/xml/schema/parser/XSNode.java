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

package net.sf.taverna.xml.schema.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Generic Tree Node implementation class that is compatible with MutableTreeNode 
 * Swing interface. The class is used as a replacement of DefaultMutableTreeNode to 
 * remove Swing references from the package. It may be used as an implementation though
 * {@code
 * MyMutableTreeNode extends XSNode<TreeNode, MutableTreeNode> implements MutableTreeNode
 * }.
 * 
 * @author Dmitry Repchevsky
 */

public class XSNode <T, V extends T>  {

    private V parent;
    private Object userObject;
    private final List<V> children;
    
    public XSNode() {
        children = new ArrayList();
    }
    
    public void insert(V child, int index) {
        if (child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            XSNode oldParent = node.getParent();
            if (oldParent != null) {
                oldParent.remove(node);
            }
            node.setParent(this);
        }
        children.add(index, child);
    }

    public void remove(int index) {
        V child = children.remove(index);
        if (child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            node.setParent(null);
        }
    }

    public void remove(V child) {
        if (children.remove(child) && child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            node.setParent(null);
        }
    }

    public Object getUserObject() {
        return userObject;
    }
    
    public void setUserObject(Object object) {
        this.userObject = object;
    }

    public void removeFromParent() {
        if (parent instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)parent;
            node.remove(this);
        }
    }

    public void setParent(V newParent) {
        this.parent = newParent;
    }

    public V getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public V getParent() {
        return parent;
    }

    public int getIndex(T node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }
    
    public void removeAllChildren() {
        for (int i = getChildCount()-1; i >= 0; i--) {
            remove(i);
        }
    }
}
