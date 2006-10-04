package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AlphaColorChooser extends JPanel implements ChangeListener {
    private JSlider alphaSlider;
    private JColorChooser colorChooser;
    private ChangeListener changeListener;

    public AlphaColorChooser(Color color, boolean withSamples, boolean withRGB, boolean withHSV) {
    	super(new BorderLayout());
        changeListener = null;
        colorChooser = color != null ? new JColorChooser(color) : new JColorChooser();
        if (!withSamples) colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[0]);
        if (!withRGB) colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[1]);
        if (!withHSV) colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[2]);
        colorChooser.removeChooserPanel(colorChooser.getChooserPanels()[1]);
        AlphaPreviewPanel previewPanel = new AlphaPreviewPanel(color);
        setChangeListener(previewPanel);
        colorChooser.getSelectionModel().addChangeListener(this);
        JPanel frame = new JPanel();
        frame.add(previewPanel);
        frame.setBorder(new EmptyBorder(0,0,5,0));
        colorChooser.setPreviewPanel(frame);
        add(colorChooser, BorderLayout.CENTER);
        alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, color != null ? color.getAlpha() : 255);
        alphaSlider.setMajorTickSpacing(85);
        alphaSlider.setMinorTickSpacing(17);
        alphaSlider.setPaintTicks(true);
        alphaSlider.setPaintLabels(true);
        alphaSlider.setBorder(BorderFactory.createTitledBorder("Alpha"));
        alphaSlider.addChangeListener(this);
        add(alphaSlider, BorderLayout.SOUTH);
	}
    
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e  a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        if (changeListener != null)
        {
                        colorChooser.setColor( getColor() );
            changeListener.stateChanged( new ChangeEvent(this) );
        }
    }
    
    private void setChangeListener(ChangeListener change_listener)
    {
        changeListener = change_listener;
    }
    
    //----------------------------------------------------------------------------
    public void setColor(Color new_color)
    {
        colorChooser.setColor(new_color);
        alphaSlider.setValue( new_color.getAlpha() );
    }
    
    //----------------------------------------------------------------------------
    public Color getColor()
    {
        Color chooser_color = colorChooser.getColor();
        return new Color(chooser_color.getRed(), chooser_color.getGreen(), chooser_color.getBlue(), alphaSlider.getValue());
    }
    
    static class AlphaPreviewPanel extends JButton implements ChangeListener {

        private static final String text="Black";
        private static final Font font=new Font("Sans Serif", Font.PLAIN, 30);
        
        private Color color;
        
        /**
         * @param color
         */
        AlphaPreviewPanel(Color color) {
            //setBorder(new EmptyBorder(5,0,5,5));
            setEnabled(false);
            //setMinimumSize(new Dimension(150,80));
            setPreferredSize(new Dimension(350,50));
            //setMaximumSize(new Dimension(100,50));
            setIcon(colorIcon);
            this.color=color;
        }

        public void stateChanged(ChangeEvent e) {
            AlphaColorChooser csm = (AlphaColorChooser) e.getSource();
            this.color=csm.getColor();
            repaint();
        }
        private Icon colorIcon = new Icon() {

            public void paintIcon(Component cmp, Graphics g, int x, int y) {
              Graphics2D g2d = (Graphics2D) g;
              g.setColor(Color.white);
              g.fillRect(x, y, AlphaPreviewPanel.this.getWidth(), AlphaPreviewPanel.this.getHeight());

              
        	  if (color != null) {
        	      g.setColor(Color.black);
	              
	          	  TextLayout tl = new TextLayout(text,font,g2d.getFontRenderContext());
	        	  Rectangle r = tl.getBounds().getBounds();
	        	  
	        	  // HACK: the previous implementation failed for strings without descent...
	        	  // I got cut-off in the vertical dir, so i added a border of width 2
	        	  int height = r.height; //(int) font.getLineMetrics(text,g2d.getFontRenderContext()).getHeight();//new TextLayout("fg", f, frc).getBounds().getBounds().height;
	              int width = r.width;
	
	        	  final float border = height - tl.getDescent();
	        	  g2d.setFont(font);
	        	  
	        	  int w=(getIconWidth()-width)/2;
	        	  int h=(getIconHeight()-height)/2;
	        	  g2d.drawString(text,x+w,y+height+h);
	
                g.setColor(color);
                g.fillRect(x, y, getIconWidth(), getIconHeight());
              }
              g.setColor(Color.black);
              g.drawRect(x, y, getIconWidth(), getIconHeight());
            }

            public int getIconWidth() {
              return AlphaPreviewPanel.this.getWidth();
            }

            public int getIconHeight() {
              return AlphaPreviewPanel.this.getHeight();
            }
            
          };

    }
    
}
