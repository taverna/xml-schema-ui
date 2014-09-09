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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.editor.XSEditorFactory;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.editor.XSEditorInterface;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.editor.XSStringEditorComponent;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSAbstractNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSAttributeNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSGlobalElementNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSMixedTextNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSParticleNode;
import net.sf.taverna.t3.uicomponents.xml.schema.tree.node.XSTypeNode;
import net.sf.taverna.t3.uicomponents.swing.IconLoader;

/**
 * @author Dmitry Repchevsky
 */

public class XSElementTreeCellEditor extends JPanel
        implements TreeCellEditor, TreeCellRenderer, ComponentListener {

    private final static Color DISABLED_COLOR = new Color(0x66CCFF);
    private final static Color ENABLED_COLOR = new Color(0x0000CC);
    private final static Color ERROR_COLOR = new Color(0xFF0000);
    private final static Color TEXT_COLOR = new Color(0xF9A800);

    private final GridBagConstraints constraints;

    private final JLabel label;
    private XSEditorInterface editor;
    
    private final ArrayList listeners;
    private final ChangeEvent event;

    private final Font font;

    public XSElementTreeCellEditor() {
        super(new GridBagLayout());

        setOpaque(false);

        constraints = new GridBagConstraints();
        constraints.ipadx = 2;

        constraints.weightx = Double.MIN_NORMAL;
        constraints.anchor = GridBagConstraints.WEST;

        add(label = new JLabel(), constraints);

        // prepare parameters for the variable (editor) part
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;

        listeners = new ArrayList();
        event = new ChangeEvent(this);

        font = new Font("sansserif", Font.PLAIN, 16); // font for icons
    }

    private void addResizeTreeListener(JTree tree) {
        ComponentListener[] l = tree.getComponentListeners();

        for (ComponentListener listener : l) {
            if (listener == this) { 
                return; // already registred
            }
        }

        tree.addComponentListener(this);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row) {
        addResizeTreeListener(tree);

        setLabel(value, false);
        
        if (value instanceof XSAttributeNode) {
            XSAttributeNode node = (XSAttributeNode)value;
            setEditor(tree, node);
        } else if (value instanceof XSTypeNode) {
            XSTypeNode node = (XSTypeNode)value;
            setEditor(tree, node);
        } else if (value instanceof XSGlobalElementNode) {
            XSGlobalElementNode node = (XSGlobalElementNode)value;
            setEditor(tree, node);
        } else if (value instanceof XSParticleNode) {
            XSParticleNode node = (XSParticleNode)value;
            XmlSchemaParticle particle = node.getXSComponent();
            if (particle instanceof XmlSchemaElement) {
                setEditor(tree, node);
            }
        } else if (value instanceof XSMixedTextNode) {
            XSMixedTextNode node = (XSMixedTextNode)value;
            editor = new XSStringEditorComponent(this);
            Object o = node.getUserObject();
            editor.setEditorValue(o);

            add(editor.getXSEditor(), constraints);
        }

        fixEditorWidth(tree, (DefaultMutableTreeNode)value);

        return this;
    }

    private void setEditor(final JTree tree, final XSAbstractNode node) {
        XmlSchemaSimpleType simpleType = node.getSimpleType();

        if(simpleType == null) {
            XmlSchemaType type = node.getType();
            XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
            
            XmlSchemaParticle particle = complexType.getParticle();
            if (particle == null) {
                //particle = complexType.getContentTypeParticle();
                XmlSchemaContentModel contentModel = complexType.getContentModel();
                if (contentModel != null) {
                    XmlSchemaContent content = contentModel.getContent();
                    if (content instanceof XmlSchemaComplexContentExtension) {
                        XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                        particle = complexContentExtension.getParticle();
                    } else if (content instanceof XmlSchemaComplexContentRestriction) {
                        XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                        particle = complexContentRestriction.getParticle();
                    }
                }
            }
            
            if (particle instanceof XmlSchemaChoice) {
                final XmlSchemaChoice choice = (XmlSchemaChoice)particle;
                final XSChoiceEditor ed = new XSChoiceEditor(choice);
                ed.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        XmlSchemaParticle particle = (XmlSchemaParticle)ed.getSelectedItem();
                        if (particle != null) {
                            node.removeAllChildren();
                            node.addParticle(particle);

                            TreeModel m = tree.getModel();

                            if (m instanceof DefaultTreeModel) {
                                DefaultTreeModel model = (DefaultTreeModel)m;
                                model.nodeStructureChanged(node);
                            }
                        }
                        cancelCellEditing(); // don't update combo value (user object)
                    }
                });
                ed.setBackground(tree.getBackground());
                add(ed, constraints);
                label.setText("");
            }
        } else {
            // either the type itself or its content is simple (last case is a complex type with a simple content)

            XmlSchemaSimpleTypeContent simpleTypeContent = simpleType.getContent();
            if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
                XmlSchemaSimpleTypeRestriction simpleTypeRestriction = (XmlSchemaSimpleTypeRestriction)simpleTypeContent;
                
                List<XmlSchemaEnumerationFacet> enumerationFacets = new ArrayList<>();
                
                List<XmlSchemaFacet> facets = simpleTypeRestriction.getFacets();
                for (XmlSchemaFacet facet : facets){
                    if (facet instanceof XmlSchemaEnumerationFacet) {
                        XmlSchemaEnumerationFacet enumerationFacet = (XmlSchemaEnumerationFacet)facet;
                        enumerationFacets.add(enumerationFacet);
                    }
                }
                
                if (!enumerationFacets.isEmpty()) {
                    final XSEnumerationEditor ed = new XSEnumerationEditor(enumerationFacets);
                    ed.setBackground(tree.getBackground());

                    ed.setSelectedItem(node.getUserObject());

                    ed.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            stopCellEditing();
                        }
                    });
                    add(ed, constraints);

                    editor = ed; // so node value could be updated on stopCellEditing()
                    return;                    
                }
            }

            editor = XSEditorFactory.getXSEditorComponent(this, simpleType);
            //editor.getXSEditor().setBackground(tree.getBackground());

            if (editor != null) {
                Object o = node.getUserObject();
                editor.setEditorValue(o);
                add(editor.getXSEditor(), constraints);
            }
        }
    }

    @Override
    public Object getCellEditorValue() {
        return editor != null ? editor.getEditorValue() : null;
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent)event;
            if (mouseEvent.getClickCount() == 2) {
                JTree tree = (JTree)event.getSource();
                TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (path != null) {
                    Object object = path.getLastPathComponent();
                    if (object != null) {
                        if (object instanceof XSAttributeNode) {
                            return true;
                        }

                        if (object instanceof XSMixedTextNode) {
                            return true;
                        }

                        if (object instanceof XSParticleNode) {
                            XSParticleNode node = (XSParticleNode)object;
                            XmlSchemaParticle xmlSchemaParticle = node.getXSComponent();

                            if (xmlSchemaParticle.getMaxOccurs() > 1) {
                                return false;
                            }

                            if (xmlSchemaParticle instanceof XmlSchemaChoice) {
                                return true;
                            }

                            return node.getSimpleType() != null;
                        } if (object instanceof XSTypeNode) {
                            XSTypeNode node = (XSTypeNode)object;
                            XmlSchemaType type = node.getXSComponent();

                            if (type instanceof XmlSchemaComplexType) {
                                XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                                XmlSchemaParticle particle = complexType.getParticle();
                                if (particle == null) {
                                    //particle = complexType.getContentTypeParticle();
                                    XmlSchemaContentModel contentModel = complexType.getContentModel();
                                    if (contentModel != null) {
                                        XmlSchemaContent content = contentModel.getContent();
                                        if (content instanceof XmlSchemaComplexContentExtension) {
                                            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                                            particle = complexContentExtension.getParticle();
                                        } else if (content instanceof XmlSchemaComplexContentRestriction) {
                                            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                                            particle = complexContentRestriction.getParticle();
                                        }
                                    }
                                }
                                
                                if (particle instanceof XmlSchemaChoice) {
                                    return true;
                                }
                                
                                return false;
                            }
                            return true;
                        } if (object instanceof XSGlobalElementNode) {
                            XSGlobalElementNode node = (XSGlobalElementNode)object;
                            return node.getSimpleType() != null;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        for (Object listener1 : listeners) {
            CellEditorListener listener = (CellEditorListener) listener1;
            listener.editingStopped(event);
        }

        return true;
    }

    @Override
    public void cancelCellEditing() {
        for (Object listener1 : listeners) {
            CellEditorListener listener = (CellEditorListener) listener1;
            listener.editingCanceled(event);
        }
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
        setLabel(value, true);
        return this;
    }

    private void setLabel(Object value, boolean isRenderer) {
        setToolTipText(null);
        
        if (getComponentCount() > 1) {
            remove(1);
        }

        final Icon icon;
        if (value instanceof XSAttributeNode) {
            XSAttributeNode node = (XSAttributeNode)value;
            setLabel(node, isRenderer);
            icon = IconLoader.load("icons/attribute.png");
        } else if (value instanceof XSTypeNode) {
            XSTypeNode node = (XSTypeNode)value;
            setLabel(node, isRenderer);


            String iconName;
            
            XmlSchemaSimpleType simpleType = node.getSimpleType();
            if (simpleType != null) {
                iconName = "icons/simple.png";
            } else {
                final XmlSchemaType type = node.getType();
                XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                XmlSchemaParticle particle = complexType.getParticle();
                if (particle == null) {
                    //particle = complexType.getContentTypeParticle();
                    XmlSchemaContentModel contentModel = complexType.getContentModel();
                    if (contentModel != null) {
                        XmlSchemaContent content = contentModel.getContent();
                        if (content instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                            particle = complexContentExtension.getParticle();
                        } else if (content instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                            particle = complexContentRestriction.getParticle();
                        }
                    }
                }
                    
                if (particle == null) {
                    iconName = "icons/dummy.png";
                } else if (particle instanceof XmlSchemaChoice) {
                    iconName = "icons/choice.png";
                } else {
                    iconName = complexType.isMixed() ? "icons/complex_mixed.png" : "icons/complex.png";
                }
            }

            TreeNode parent = node.getParent();

            if (parent instanceof XSParticleNode) { // if we are a part of sequence numerate the icon
                final int index = node.getParent().getIndex(node);
                icon = getIndexIcon(iconName, index + 1);
            } else {
                icon = IconLoader.load(iconName);
            }
        } else if (value instanceof XSParticleNode) {
            XSParticleNode node = (XSParticleNode)value;
            XmlSchemaParticle particle = node.getXSComponent();

            final String iconName;
            if (particle instanceof XmlSchemaChoice) {
                iconName = "icons/choice.png";
            } else if (particle instanceof XmlSchemaElement) {
                setValueForType(node, isRenderer);

                final XmlSchemaElement element = (XmlSchemaElement)particle;
                if (element.getMaxOccurs() > 1) {
                    iconName = "icons/list.png";
                } else {
                    XmlSchemaSimpleType simpleType = node.getSimpleType();
                    if (simpleType != null) {
                        iconName = "icons/simple.png";
                    } else {
                        final XmlSchemaType type = node.getType();
                        iconName = type.isMixed() ? "icons/complex_mixed.png" : "icons/complex.png";
                    }
                }
            } else {
                // todo:
                iconName = "icons/dummy.png";
            }
            icon = IconLoader.load(iconName);
        } else if (value instanceof XSGlobalElementNode) {
            XSGlobalElementNode node = (XSGlobalElementNode)value;
            
            setValueForType(node, isRenderer);
            
            final String iconName;
            XmlSchemaSimpleType simpleType = node.getSimpleType();
            if (simpleType != null) {
                iconName = "icons/simple.png";
            } else {
                final XmlSchemaType type = node.getType();
                iconName = type.isMixed() ? "icons/complex_mixed.png" : "icons/complex.png";
            }
            icon = IconLoader.load(iconName);
        } else if(value instanceof XSMixedTextNode) {
            icon = IconLoader.load("icons/text.png");
            XSMixedTextNode node = (XSMixedTextNode)value;
            label.setText(isRenderer ? node.getUserObject().toString() : "");
            label.setForeground(TEXT_COLOR);
        } else if (value instanceof XSAbstractNode) {
            icon = IconLoader.load("icons/dummy.png");
            XSAbstractNode node = (XSAbstractNode)value;
            Boolean valid = node.validate();
            label.setForeground(valid == null ? DISABLED_COLOR : valid ? ENABLED_COLOR : ERROR_COLOR);
        } else {
            icon = IconLoader.load("icons/dummy.png");
        }
        label.setIcon(icon);
    }

    private void setValueForType(XSAbstractNode node, boolean isRenderer) {
        
        XmlSchemaSimpleType simpleType = node.getSimpleType();
        if (simpleType != null) {
            setLabel(node, isRenderer);
        } else {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append(node.getName().getLocalPart());

                String t = node.getTypeName().getLocalPart();
                // the type can be embedded and has no name
                if (t != null && t.length() > 0) {
                    sb.append(" <font style='color: #DDDDDD'>(").append(t).append(")</font>");
                }

                sb.append("</html>");

                label.setText(sb.toString()); 
        }
    }

    private Icon getIndexIcon(String path, int number) {
        String string = String.valueOf(number);

        BufferedImage image;
        Graphics2D g;
        try {
            BufferedImage gif = ImageIO.read(XSElementTreeCellEditor.class.getClassLoader().getResourceAsStream(path));

            image = new BufferedImage(gif.getWidth(), gif.getHeight(), BufferedImage.TRANSLUCENT);
            g = image.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g.drawImage(gif, 0, 0, null);

        } catch (Exception ex) {
            image = new BufferedImage(label.getHeight(), label.getHeight(), BufferedImage.TRANSLUCENT);
            g = image.createGraphics();
            ex.printStackTrace();
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.setColor(Color.BLUE);
        g.setFont(font);

        FontRenderContext frc = g.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics(string, frc);
        Rectangle2D rec = font.getStringBounds(string, frc);
        g.scale(image.getWidth()/rec.getWidth() * 0.7, image.getHeight()/rec.getHeight() * 0.7);

        g.drawString(string, 0, (int)((rec.getHeight() - lm.getDescent()) * 1.4));

        g.dispose();

        return new ImageIcon(image);
    }

    private void setLabel(XSAbstractNode node, boolean isRenderer) {
        label.setText(node.getName().getLocalPart());

        if (isRenderer) {
            Object o = node.getUserObject();
            if (o != null) {
                String s = o.toString();

                final int eol = s.indexOf('\n');

                JTextField text = new JTextField(eol < 0 ? s : s.substring(0, eol));
                text.setBorder(null);

                add(text, constraints);

                setToolTipText(s.length() > 0 ? s : null);
            }
        }
    }

    private void fixEditorWidth(JTree tree, DefaultMutableTreeNode xsNode) {
        TreeUI treeUI = tree.getUI();
        if (treeUI instanceof BasicTreeUI) {
            BasicTreeUI basicTreeUI = (BasicTreeUI)treeUI;

            // if root is not visible - one indent less
            int path = tree.isRootVisible() ? 0 : -1;
            path += tree.getShowsRootHandles() ? 1 : 0;

            for (TreeNode parent = xsNode.getParent(); parent != null; path++) {
                parent = parent.getParent();
            }

            final int indent = basicTreeUI.getLeftChildIndent() + basicTreeUI.getRightChildIndent();
            final int offset = (indent * path) + label.getPreferredSize().width;
            final int width = tree.getWidth() - offset - constraints.ipadx * 2;

            Dimension dim = editor.getXSEditor().getPreferredSize();

            dim.width = width;

            editor.getXSEditor().setPreferredSize(dim);
            setSize(width + offset, dim.height);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        JTree tree = (JTree) e.getComponent();
        TreePath path = tree.getEditingPath();
        if (path != null) {
            Object o = path.getLastPathComponent();

            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)o;
                editor.getXSEditor().setPreferredSize(null);

                // recalculate tree size (width)
                BasicTreeUI basicTreeUI = (BasicTreeUI) tree.getUI();
                basicTreeUI.setLeftChildIndent(basicTreeUI.getLeftChildIndent());

                // ajust editor width with a tree.
                fixEditorWidth(tree, node);

                tree.treeDidChange();
            }
        }

        //stopCellEditing();
    }

    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
}
