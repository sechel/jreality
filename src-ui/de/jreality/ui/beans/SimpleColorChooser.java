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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
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
	private JButton previewPanel;
	private Color color;
	private ChangeListener changeListener;
	private boolean updating = false;

	public SimpleColorChooser() {
		this(Color.white);
	}
	
	public SimpleColorChooser(Color color) {
		super(new BorderLayout());
		
		// sliders
		Box sliderBox = new Box(BoxLayout.Y_AXIS);
		redSlider = makeSlider(color.getRed(), "red");
		sliderBox.add(redSlider);
		greenSlider = makeSlider(color.getRed(), "green");
		sliderBox.add(greenSlider);
		blueSlider = makeSlider(color.getRed(), "blue");
		sliderBox.add(blueSlider);
		alphaSlider = makeSlider(color.getRed(), "alpha");
		sliderBox.add(alphaSlider);
		add(sliderBox, BorderLayout.CENTER);
		
		// preview panel
		Icon colorIcon = new Icon() {

			public void paintIcon(Component cmp, Graphics g, int x, int y) {
				Graphics2D g2d = (Graphics2D) g;
				g.setColor(Color.white);
				g.fillRect(x, y, previewPanel.getWidth(), previewPanel.getHeight());
				Color col = getColor();
				if (col != null) {
					g.setColor(Color.black);

					TextLayout tl = new TextLayout(text,font,g2d.getFontRenderContext());
					Rectangle r = tl.getBounds().getBounds();

					int height = r.height;
					int width = r.width;

					final float border = height - tl.getDescent();
					g2d.setFont(font);

					int w=(getIconWidth()-width)/2;
					int h=(getIconHeight()-height)/2;
					g2d.drawString(text,x+w,y+height+h);

					g.setColor(col);
					g.fillRect(x, y, getIconWidth(), getIconHeight());
				}
				g.setColor(Color.black);
				g.drawRect(x, y, getIconWidth(), getIconHeight());
			}

			public int getIconWidth() {
				return previewPanel.getWidth();
			}

			public int getIconHeight() {
				return previewPanel.getHeight();
			}
		};
		previewPanel = new JButton(colorIcon);
		previewPanel.setEnabled(false);
		previewPanel.setPreferredSize(new Dimension(350,50));
		add(previewPanel, BorderLayout.SOUTH);
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
					alphaSlider.getValue()
			);
			previewPanel.repaint();
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
		
		if (color == null || !color.equals(c)) {
			updating = true;
			redSlider.setValue( c.getRed() );
			greenSlider.setValue( c.getGreen() );
			blueSlider.setValue( c.getBlue() );
			alphaSlider.setValue( c.getAlpha() );
			updating = false;
			color = c;
			previewPanel.repaint();
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
		f.show();
	}
}
