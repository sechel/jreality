/*
 * Created on 12.02.2006
 *
 * This file is part of the de.jreality.swing package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.swing;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Toolkit;

public class FakeFrame extends Frame {
    private static final long serialVersionUID = 3258688793266958393L;

    public FakeFrame() throws HeadlessException {
        super();
    }

    public FakeFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public FakeFrame(String title) throws HeadlessException {
        super(title);
    }

    public FakeFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    public Toolkit getToolkit() {
        return FakeToolKit.getDefaultToolkit();
    }
}
