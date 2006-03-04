package de.jreality.ui.beans;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JDialog;

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