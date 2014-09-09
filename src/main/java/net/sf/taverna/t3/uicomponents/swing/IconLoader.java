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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Dmitry Repchevsky
 */

public final class IconLoader {
    private static ConcurrentHashMap<String, SoftReference<Icon>> icons = new ConcurrentHashMap<String, SoftReference<Icon>>();

    private IconLoader() {}

    public static Icon load(String path) {
        SoftReference<Icon> ref = icons.get(path);

        Icon icon;
        if (ref != null) {
            icon = ref.get();

            if (icon != null) {
                return icon;
            }
        } else {
            icon = null;
        }

        try {
            InputStream in = IconLoader.class.getClassLoader().getResourceAsStream(path);

            if (in != null) {
                icon = new ImageIcon(ImageIO.read(new BufferedInputStream(in)));
                icons.put(path, new SoftReference<Icon>(icon));
            }
        }
        catch (IOException ex) {}

        return icon;
    }
}
