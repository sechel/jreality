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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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

import java.awt.Color;
import java.awt.Component;
import java.awt.image.ColorModel;

public class AlphaColorChooserDialog extends DefaultDialog
{
    public static AlphaColorChooserDialog instance_ = null;
	private AlphaColorChooser content;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of AlphaColorChooser */
    private AlphaColorChooserDialog(String title, Color color)
    {
        super();
        setTitle(title);
        content = new AlphaColorChooser(color, true, true, true);
        setMainComponent(content);
        pack();
    }
    
    public static AlphaColorChooserDialog getInstance()
    {
        if( instance_ == null )
        {
            instance_ = new AlphaColorChooserDialog("Choose Color",Color.RED);
        }
        
        return instance_; 
    }
    
    //----------------------------------------------------------------------------
    
    public static Color showDialog(Component c, String title, Color f)
    {
    AlphaColorChooserDialog d = new AlphaColorChooserDialog(title,f);
    d.setModal(true);
    d.setVisible(true);
    return d.getColor();
    }

	public Color getColor() {
		return content.getColor();
	}

	public ColorModel getColorModel() {
		return content.getColorModel();
	}

	public void setColor(Color new_color) {
		content.setColor(new_color);
	}


}

