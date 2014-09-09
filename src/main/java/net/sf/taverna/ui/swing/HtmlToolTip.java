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

package net.sf.taverna.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

/**
 * @author Dmitry Repchevsky
 */

public class HtmlToolTip extends JToolTip {
    
    static {
        PopupFactory.setSharedInstance(new HtmlToolTipPopupFactory(PopupFactory.getSharedInstance()));
    }

    private int dismissDelay;
    private final JEditorPane editorPane;
    
    public HtmlToolTip() {
        this(ToolTipManager.sharedInstance().getDismissDelay());
    }
    
    public HtmlToolTip(final int dismissDelay) {
        this.dismissDelay = dismissDelay;
        
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(null);
        editorPane = new JEditorPane();
        editorPane.setEditorKit(new PreWrapHTMLEditorKit());
        editorPane.setBorder(null);
        editorPane.setEditable(false);
        
        add(editorPane, BorderLayout.CENTER);
    }
    
    public int getDismissDelay() {
        return dismissDelay;
    }
    
    public void setDismissDelay(final int dismissDelay) {
        this.dismissDelay = dismissDelay;
    }
    
    @Override
    public void setTipText(String tipText) {
        if (tipText != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='color: #00007F; background-color: #FFFFCC'>");
            sb.append(tipText);
            sb.append("</body></html>");
            editorPane.setText(sb.toString());
        }
        super.setTipText(tipText);
    }

    @Override
    public String getTipText() {
        return editorPane == null ? "" : editorPane.getText();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return editorPane != null ? editorPane.getPreferredSize() : super.getPreferredSize();
    }
    
    protected static class PreWrapHTMLEditorKit extends HTMLEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new PreWrapHTMLFactory();
        }
    }

    protected static class PreWrapHTMLFactory extends HTMLFactory {
        @Override
        public View create(Element elem) {
            AttributeSet attrs = elem.getAttributes();
            Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
            Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                HTML.Tag kind = (HTML.Tag) o;
                if (kind == HTML.Tag.IMPLIED) {
                    return new javax.swing.text.html.ParagraphView(elem);
                }
            }
            return super.create(elem);
        }
    }

    public static class HtmlToolTipPopup extends Popup {
        
        private final HtmlToolTip htmlTooltip;
        private final JPopupMenu popup;
        
        private final Component owner;
        private final int x;
        private final int y;

        private int dismissDelay;
        
        public HtmlToolTipPopup(Component owner, HtmlToolTip htmlTooltip, int x, int y) {
            this.owner = owner;
            this.htmlTooltip = htmlTooltip;
            
            Point point = SwingUtilities.convertPoint(htmlTooltip, x, y, owner);

            this.x = point.x;
            this.y = point.y;

            popup = new HtmlPopup();
            popup.add(htmlTooltip, BorderLayout.CENTER);
        }

        @Override
        public void show() {
            popup.show(owner, x, y);
            Window w = SwingUtilities.getWindowAncestor(popup);
            if (w != null && w != SwingUtilities.getWindowAncestor(owner)) {
                Shape shape = new RoundRectangle2D.Float(0, 0, w.getWidth(), w.getHeight(), DecoratorBorder.ANCLE, DecoratorBorder.ANCLE);
                w.setShape(shape);
            }
            
            dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
            ToolTipManager.sharedInstance().setDismissDelay(htmlTooltip.getDismissDelay());
        }

        @Override
        public void hide() {
            popup.setVisible(false);
            ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
        }
    }

    public static class HtmlPopup extends JPopupMenu {

        public HtmlPopup() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new DecoratorBorder(Color.LIGHT_GRAY));
            setBackground(new Color(0xFFFFCC));
        }
        
        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override 
        public void paint(Graphics g) {
            paintChildren(g);
            paintBorder(g);
        }
    }
    
    public static class HtmlToolTipPopupFactory extends PopupFactory {
        private final PopupFactory factory;

        public HtmlToolTipPopupFactory(PopupFactory factory) {
            this.factory = factory;
        }

        @Override
        public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
            if (contents instanceof HtmlToolTip) {
                HtmlToolTip htmlTooltip = (HtmlToolTip)contents;
                return new HtmlToolTipPopup(owner, htmlTooltip, x, y);
            }

            return factory.getPopup(owner, contents, x, y);
        }
    }
}
