package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimpleColorChooser extends JPanel implements ChangeListener {
	
	private static final long serialVersionUID = -6452509893745135407L;
	private static final String text="Black";
	private static final Font font=new Font("Sans Serif", Font.PLAIN, 30);

	private JSlider redSlider;
	private JSlider greenSlider;
	private JSlider blueSlider;
	private JSlider alphaSlider;
	//private JPanel previewPanel;
	private Color color;
	private ChangeListener changeListener;
	private boolean updating = false;
	private boolean withAlpha;

	public SimpleColorChooser() {
		this(Color.white, false);
	}
	
	public SimpleColorChooser(Color color, boolean withAlpha) {
		super(new BorderLayout());
		this.color = color;
		this.withAlpha = withAlpha;
		// sliders
		Box sliderBox = new Box(BoxLayout.Y_AXIS);
		redSlider = makeSlider(color.getRed(), "red");
		sliderBox.add(redSlider);
		greenSlider = makeSlider(color.getRed(), "green");
		sliderBox.add(greenSlider);
		blueSlider = makeSlider(color.getRed(), "blue");
		sliderBox.add(blueSlider);
		if (withAlpha) {
			alphaSlider = makeSlider(color.getRed(), "alpha");
			sliderBox.add(alphaSlider);
		}
		add(sliderBox, BorderLayout.CENTER);

//		previewPanel = new JPanel() {
//			public void paint(Graphics g) {
//				Graphics2D g2d = (Graphics2D) g;
//				int w0 = previewPanel.getWidth();
//				int h0 = previewPanel.getHeight();
//				Color col = getColor();
//				if (col != null) {
//					g.setColor(Color.black);
//
//					TextLayout tl = new TextLayout(text,font,g2d.getFontRenderContext());
//					Rectangle r = tl.getBounds().getBounds();
//
//					int height = r.height;
//					int width = r.width;
//					
//					g2d.setFont(font);
//
//					int w=(w0-width)/2;
//					int h=(h0-height)/2;
//					g2d.drawString(text,w,height+h);
//
//					g.setColor(col);
//					g.fillRect(0,0,w0, h0);
//				}
//			}
//
//		};
//		previewPanel.setPreferredSize(new Dimension(350,50));
//		add(previewPanel, BorderLayout.SOUTH);
	}

	private JSlider makeSlider(int value, String text) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 255, value);
		slider.setMajorTickSpacing(85);
		slider.setMinorTickSpacing(17);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createTitledBorder(text));
		slider.addChangeListener(this);
		return slider;
	}

	public void stateChanged(ChangeEvent e) {
		
		if (!updating) {
			color = new Color(
					redSlider.getValue(),
					greenSlider.getValue(),
					blueSlider.getValue(),
					withAlpha ? alphaSlider.getValue() : 255
			);
			//previewPanel.repaint();
			if (changeListener != null) changeListener.stateChanged(new ChangeEvent(this));
		}
	}

	public void addChangeListener(ChangeListener listener) {
		changeListener=ChangeEventMulticaster.add(changeListener, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		changeListener=ChangeEventMulticaster.remove(changeListener, listener);
	}

	public void setColor(Color c) {
		if (!color.equals(c)) {
			updating = true;
			redSlider.setValue( c.getRed() );
			greenSlider.setValue( c.getGreen() );
			blueSlider.setValue( c.getBlue() );
			if (withAlpha) alphaSlider.setValue( c.getAlpha() );
			updating = false;
			color = c;
			//previewPanel.repaint();
		}
	}

	public Color getColor() {
		return color;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("SimpleColorChooser");
		SimpleColorChooser scc = new SimpleColorChooser();
		f.getContentPane().add(scc);
		f.pack();
		f.setVisible(true);
	}
}
