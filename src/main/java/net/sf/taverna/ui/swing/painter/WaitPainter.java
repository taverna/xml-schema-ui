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

package net.sf.taverna.ui.swing.painter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

/**
 * @author Dmitry Repchevsky
 */

public class WaitPainter extends Timer implements ActionListener {

    private final static int DIAMETER = 50;
    private final static int RADIUS = DIAMETER >>> 2;

    private final Component c;

    private int tick;
    private BufferedImage mask;

    public WaitPainter(Component c) {
        super(80, null);
        
        this.c = c;
        addActionListener(this);
    }

    public void paint(Graphics g) {
        if (isRunning()) {
            tick = tick > 10 ? 0 : tick + 1;

            Graphics2D graphics = (Graphics2D)g;

            BufferedImage mask = generateMask();

            BufferedImage tmp = new BufferedImage(DIAMETER, DIAMETER, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tmp.createGraphics();
            g2d.translate(RADIUS, RADIUS);
            g2d.rotate(tick * Math.PI/6);

            g2d.drawImage(mask, -RADIUS , -RADIUS, c);

            g2d.dispose();

            graphics.drawImage(tmp, 0, 0, c);

            graphics.dispose();
        }
    }

    private BufferedImage generateMask() {
        if (mask == null) {
            mask = new BufferedImage(DIAMETER, DIAMETER, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = mask.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final int l = (int)(RADIUS * 0.75);
            final int s = RADIUS - l;
            final int w = l * 2/5;

            g.translate(RADIUS, RADIUS);

            for (int i = 0; i < 12; i++) {
                int rgb = Color.HSBtoRGB((float)i/12, 1, 1) & 0x00FFFFFF | 0x7F000000;
                g.setColor(new Color(rgb, true));
                g.fillRoundRect(s, 0 - w/2, l, w, w, w);

                g.rotate(Math.PI/6);
            }

            g.dispose();
        }
        return mask;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        c.repaint();
    }
}
