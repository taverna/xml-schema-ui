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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;

/**
 * @author Dmitry Repchevsky
 */

public class ScrollableToolTip extends JToolTip
{
    static {
        UIManager.put("ScrollableToolTip", "org.inb.swing.ScrollableToolTip$ScrollableToolTipUI");
        PopupFactory.setSharedInstance(new ScrollableToolTipPopupFactory(PopupFactory.getSharedInstance()));
    }

    private final JTextArea textPane;
    private final JScrollPane scrollPane;

    public ScrollableToolTip() {
       setLayout(new BorderLayout());

       textPane = new JTextArea() {
           @Override
           public Dimension getPreferredScrollableViewportSize() {
               Dimension dim = getPreferredSize();
               dim.height = Math.min(dim.height, 200);
               return dim;
           }
       };

       textPane.setOpaque(false);
       textPane.setBackground(new Color(0,0,0, 255));

       textPane.setEditable(false);

       scrollPane = new JScrollPane(textPane);
       scrollPane.setBorder(BorderFactory.createEmptyBorder());
       scrollPane.setOpaque(false);
       scrollPane.getViewport().setOpaque(false);

       add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void setTipText(String tipText) {
        textPane.setText(tipText);
    }

    @Override
    public String getTipText() {
        return textPane != null ? textPane.getText() : "";
    }

    @Override
    public Dimension getPreferredSize()
    {
        return scrollPane.getPreferredSize();
    }

    @Override
    public String getUIClassID()
    {
        return "ScrollableToolTip";
    }

    public static class ScrollableToolTipUI extends ToolTipUI
    {
        private static ScrollableToolTipUI singleton = new ScrollableToolTipUI();

        public static ComponentUI createUI(JComponent c)
        {
            return singleton;
        }
    }

    public static class ScrollableToolTipPopup extends Popup implements ActionListener, AWTEventListener
    {
        private final static int DELAY = 400;
        
        private JPopupMenu popup;
        private Timer timer;

        private Component owner;
        private int x;
        private int y;

        public ScrollableToolTipPopup(Component owner, ScrollableToolTip smartToolTip, int x, int y)
        {
            this.owner = owner;

            Point point = SwingUtilities.convertPoint(smartToolTip, x, y, owner);

            this.x = point.x;
            this.y = point.y;
            
            popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.setOpaque(false);

            popup.add(smartToolTip, BorderLayout.CENTER);

            timer = new javax.swing.Timer(DELAY, this);
            timer.setRepeats(false);

            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        }

        @Override
        public void show()
        {
            popup.show(owner, x, y);
        }

        @Override
        public void hide()
        {
            timer.start();
        }

        public void actionPerformed(ActionEvent e)
        {
            timer.stop();
            popup.setVisible(false);
        }

        public void eventDispatched(AWTEvent event)
        {
            if (event instanceof MouseEvent)
            {
                MouseEvent e = (MouseEvent)event;

                final int id = e.getID();

                if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_EXITED)
                {
                    Component c = (Component) e.getSource();

                    if (SwingUtilities.isDescendingFrom(c, popup))
                    {
                        Point point = SwingUtilities.convertPoint(c, e.getPoint(), popup);

                        final boolean isInside = popup.getBounds().contains(point);

                        if (id == MouseEvent.MOUSE_EXITED && !isInside)
                        {
                            popup.setVisible(false);
                        }
                        else
                        {
                            timer.stop();
                        }
                    }
                }
            }
        }
    }

    public static class ScrollableToolTipPopupFactory extends PopupFactory
    {
        private PopupFactory factory;

        public ScrollableToolTipPopupFactory(PopupFactory factory)
        {
            this.factory = factory;
        }

        @Override
        public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException
        {
            if (contents instanceof ScrollableToolTip)
            {
                ScrollableToolTip smartToolTip = (ScrollableToolTip)contents;

                return new ScrollableToolTipPopup(owner, smartToolTip, x, y);
            }

            return factory.getPopup(owner, contents, x, y);
        }
    }
}
