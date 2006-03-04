package de.jreality.ui.beans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class FontEditor extends JPanel 
  implements java.beans.PropertyEditor, ActionListener
{
  public FontEditor() 
  {
    setLayout(null);
    toolkit = Toolkit.getDefaultToolkit();
    fonts = toolkit.getFontList();
    //fonts = GraphicsEnvironment.getAvailableFontFamilyNames();

    familyChoser = new JComboBox();
    for (int i = 0; i < fonts.length; i++) 
    {
      familyChoser.addItem(fonts[i]);
    }
    add(familyChoser);
    familyChoser.setBounds(20, 5, 100, 30); // reshape(20, 5, 100, 30);
    styleChoser = new JComboBox();
    for (int i = 0; i < styleNames.length; i++) 
    {
      styleChoser.addItem(styleNames[i]);
    }
    add(styleChoser);
    styleChoser.setBounds(145, 5, 70, 30); //reshape(145, 5, 70, 30);
    sizeChoser = new JComboBox();
    for (int i = 0; i < pointSizes.length; i++) 
    {
      sizeChoser.addItem("" + pointSizes[i]);
    }
    add(sizeChoser);
    sizeChoser.setBounds(220, 5, 70, 30); //reshape(220, 5, 70, 30);

    familyChoser.addActionListener(this);
    styleChoser.addActionListener(this);
    sizeChoser.addActionListener(this);
    setSize(300,40);
  }

  public Dimension getPreferredSize() 
  {
    return new Dimension(300, 40);
  }

  public void setValue(Object o) 
  {
    font = (Font) o;
    
    familyChoser.removeActionListener(this);
    styleChoser.removeActionListener(this);
    sizeChoser.removeActionListener(this);
            
    // Update the current GUI choices.
    for (int i = 0; i < fonts.length; i++) 
    {
      if (fonts[i].equals(font.getFamily())) 
      {
        familyChoser.setSelectedIndex(i);
        break;
      }
    }
    for (int i = 0; i < styleNames.length; i++) 
    {
      if (font.getStyle() == styles[i]) 
      {
        styleChoser.setSelectedIndex(i);
        break;
      }
    }
    for (int i = 0; i < pointSizes.length; i++) 
    {
      if (font.getSize() <= pointSizes[i]) 
      {
        sizeChoser.setSelectedIndex(i);
        break;
      }
    }
    familyChoser.addActionListener(this);
    styleChoser.addActionListener(this);
    sizeChoser.addActionListener(this);     
    changeFont(font);
  }

  private void changeFont(Font f) 
  {
    font = f;
    if (sample != null) 
    {
      remove(sample);
    }
    sample = new JLabel(sampleText);
    sample.setFont(font);
    add(sample);
    Component p = getParent();
    if (p != null) 
    {
      p.invalidate();
      p.doLayout(); // would validate() do here? 
    }
    invalidate();
    doLayout();
    repaint();
    support.firePropertyChange("", null, null);
  }

  public Object getValue() 
  {
    return (font);
  }

  public String getJavaInitializationString() 
  {
    return "new java.awt.Font(\"" + font.getFamily() + "\", " +
      font.getStyle() + ", " + font.getSize() + ")";
  }

  public void actionPerformed(ActionEvent e) //action(Event e, Object arg) {
  { 
    String family = (String) familyChoser.getSelectedItem();
    int style = styles[styleChoser.getSelectedIndex()];
    int size = pointSizes[sizeChoser.getSelectedIndex()];
    try {
      Font f = new Font(family, style, size);
      changeFont(f);
    } catch (Exception ex) {
      System.err.println("Couldn't create font " + family + "-" +
        styleNames[style] + "-" + size);
    }
    //return (false);
  }

  public boolean isPaintable() 
  {
    return true;
  }

  public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) 
  {
    // Silent noop.
    Font oldFont = gfx.getFont();
    gfx.setFont(font);
    FontMetrics fm = gfx.getFontMetrics();
    int vpad = (box.height - fm.getAscent())/2;
    gfx.drawString(sampleText, 0, box.height-vpad);
    gfx.setFont(oldFont);
  }

  public String getAsText() 
  {
    return null;
  }

  public void setAsText(String text) throws IllegalArgumentException 
  {
    throw new IllegalArgumentException(text);
  }

  public String[] getTags() 
  {
    return null;
  }

  public java.awt.Component getCustomEditor() 
  {
    return this;
  }

  public boolean supportsCustomEditor() 
  {
    return true;
  }

  public void addPropertyChangeListener(PropertyChangeListener l) 
  {
    support.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) 
  {
    support.removePropertyChangeListener(l);
  }

  private Font font;
  private Toolkit toolkit;
  private String sampleText = "Abcde...";

  private JLabel sample;
  private JComboBox familyChoser;
  private JComboBox styleChoser;
  private JComboBox sizeChoser;

  private String fonts[];
  private String[] styleNames = { "plain", "bold", "italic" };
  private int[] styles = { Font.PLAIN, Font.BOLD, Font.ITALIC };
  private int[] pointSizes = { 3, 5, 8, 10, 12, 14, 18, 24, 36, 48 };

  private PropertyChangeSupport support = new PropertyChangeSupport(this);
}