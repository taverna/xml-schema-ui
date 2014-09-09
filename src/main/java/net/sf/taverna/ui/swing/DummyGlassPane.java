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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import net.sf.taverna.ui.swing.painter.WaitPainter;

/**
 * @author Dmitry Repchevsky
 */

public class DummyGlassPane extends JComponent implements ComponentListener {
    private final WaitPainter painter;

    public DummyGlassPane() {
      this.setOpaque(false);

      addKeyListener(new KeyAdapter() { });
      addMouseListener(new MouseAdapter() { });
      addComponentListener(this);

      painter = new WaitPainter(this);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        painter.paint(g);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        painter.start();
        super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        painter.stop();
        super.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override public void componentResized(ComponentEvent e) {}
    @Override public void componentMoved(ComponentEvent e) {}
}
