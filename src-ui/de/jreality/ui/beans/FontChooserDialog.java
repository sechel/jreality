/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.ui.beans;

import java.awt.Component;
import java.awt.Font;

/**
 * This class provides a font chooser dialog. The user can choose from a list
 * of fonts and attributes that are available on the system.
 */
public class FontChooserDialog extends DefaultDialog 
{
    protected FontSelectionPanel fsp;
    protected Font font = null;

    //--- Constructor(s) ---

    public FontChooserDialog(Font font)
    {
    fsp = new FontSelectionPanel(font);
    setMainComponent(fsp);

    pack();

    try {
      fsp.setSelectedFont(font);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    }

    //--- Method(s) ---

    public void apply() 
    {
    try {
      font = fsp.getSelectedFont();
    }
    catch (FontSelectionPanel.InvalidFontException e) {
    }
    
    }

    /**
     * Returns the selected font.
     *
     * @return null, if dialog was canceled; the font, otherwise
     */
    public Font getSelectedFont()
    {
    return font;
    }

    /**
     * Displays a font dialog. The dialog is placed relative to the position
     * of <code>c</code>.
     *
     * @param c the component; if null, the dialog will be placed at the center
     *          of the screen
     * @param f the font that is to be selected, if available
     * @return null, if dialog was canceled; the font, otherwise
     */
    public static Font showDialog(Component c, Font f)
    {
    FontChooserDialog d = new FontChooserDialog(f);
    d.setModal(true);
    d.show(c);
    return d.getSelectedFont();
    }

}