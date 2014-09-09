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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JScrollPane;

/**
 * This is an extended JScrollPane component that paints a background image
 *
 * @author Dmitry Repchevsky
 */

public class DecoratedJScrollPane extends JScrollPane {
    private BufferedImage logo;

    public DecoratedJScrollPane() {}

    public DecoratedJScrollPane(Component view) {
        super(view);
    }

    public DecoratedJScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    public DecoratedJScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    public void setBackgroundImage(BufferedImage logo) {
        this.logo = logo;
    }

    @Override
    public void paint(Graphics g) {
        viewport.setOpaque(false);

        paintComponent(g);
        paintBorder(g);
        paintChildren(g);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isOpaque()) {
            // paint view background ourselves (if it is declared transparent)
            Component view = viewport.getView();
            if (view != null && !view.isOpaque()) {
                Dimension dim = viewport.getExtentSize();

                g.setColor(view.getBackground());
                g.fillRect(0, 0, dim.width, dim.height);
            }

            // paint background image
            if (logo != null) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Dimension dim = viewport.getExtentSize();

                g2.drawImage(logo, dim.width - logo.getWidth(), dim.height - logo.getHeight(), this);
            }
        }
    }
}
