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

package net.sf.taverna.t3.uicomponents.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.border.Border;

/*
 * @author Dmitry Repchevsky
 */

public class DecoratorBorder implements Border {

    public final static int ANCLE = 20;
    private final static Insets INSETS = new Insets(4,4,4,4);

    private Color color;
    private final int ancle;
    private final Insets insets;

    public DecoratorBorder() {
        this(Color.RED, ANCLE, INSETS);
    }

    public DecoratorBorder(Color color) {
        this(color, ANCLE, INSETS);
    }
    
    public DecoratorBorder(Color color, int ancle) {
        this(color, ancle, INSETS);
    }

    public DecoratorBorder(Color color, int ancle, Insets insets) {
        this.color = color;
        this.ancle = ancle;
        this.insets = insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(c.getBackground());
        Shape clip = g2.getClip();

        g2.setClip(0, 0, width - 1, insets.top);
        g2.fillRoundRect(x, y, width - 1, height - 1, ancle, ancle);

        g2.setClip(0, height - insets.bottom, width - 1, INSETS.bottom);
        g2.fillRoundRect(x, y, width - 1, height - 1, ancle, ancle);

        g2.setClip(0, 0, insets.left, height - 1);
        g2.fillRoundRect(x, y, width - 1, height - 1, ancle, ancle);

        g2.setClip(width - insets.right, 0, width - 1, height - 1);
        g2.fillRoundRect(x, y, width - 1, height - 1, ancle, ancle);

        g2.setClip(clip);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width - 1, height - 1, ancle, ancle);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
