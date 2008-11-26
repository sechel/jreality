package de.jreality.ui.sceneview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

@SuppressWarnings("serial")
public class TextureInspector extends JPanel {
	
	// default value of tex panel
	private static final double DEFAULT_TEXTURE_SCALE = 20;
	private static final String DEFAULT_TEXTURE = "none";
	
	// maximal value of texture scale
	private static final double MAXIMAL_TEXTURE_SCALE = 400;
	
	// ratio of maximal value and minimal value of texture scale
	private static final double LOGARITHMIC_RANGE = 400;

	private ButtonGroup textureGroup;
	private JSlider texScaleSlider;
	
	private HashMap<String, String> textureNameToTexture =
		new HashMap<String, String>();
	private HashMap<String, ButtonModel> textureNameToButton =
		new HashMap<String, ButtonModel>();
	private JFileChooser fileChooser;

	// texture of content
	private Texture2D tex;
	
	private Appearance appearance;
	private Component parent;

	public TextureInspector(
			HashMap<String, String> textureNameToTexture,
			Appearance appearance,
			Component parent
	) {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		
		this.textureNameToTexture = textureNameToTexture;
		this.appearance = appearance;
		this.parent = parent;
		
		textureGroup = new ButtonGroup();
		ActionListener texturesListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setTexture(e.getActionCommand());
			}
		};
		for (String name : textureNameToTexture.keySet()) {
			JRadioButton button = new JRadioButton(name);
			button.setActionCommand(name);
			textureNameToButton.put(name, button.getModel());
			button.addActionListener(texturesListener);
			gbc.gridy++;
			add(button, gbc);
			textureGroup.add(button);
		}
		
		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(20,0,10,0));
		JLabel texScaleLabel = new JLabel("scale");
		
		texScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,0);
		texScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTextureScale(getTextureScale());
			}
		});
		texScaleBox.add(texScaleLabel);
		texScaleBox.add(Box.createHorizontalStrut(5));
		texScaleBox.add(texScaleSlider);
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(texScaleBox, gbc);
		
		JButton textureLoadButton = new JButton("load");
		textureLoadButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				loadTexture();
			}
		});
		gbc.gridy++;
		add(textureLoadButton, gbc);
	}

	public double getTextureScale() {
		double d = .01 * texScaleSlider.getValue();
		return Math.exp(Math.log(LOGARITHMIC_RANGE) * d)/LOGARITHMIC_RANGE * MAXIMAL_TEXTURE_SCALE;
	}
	
	public void setTextureScale(double d) {
		texScaleSlider.setValue(
				(int)(Math.log(d / MAXIMAL_TEXTURE_SCALE * LOGARITHMIC_RANGE)/Math.log(LOGARITHMIC_RANGE)*100)
			);
		if (tex != null) {
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(d).getMatrix());
		}
	}

	public String getTexture() {
		return textureGroup.getSelection().getActionCommand();
	}
	
	public void setTexture(String name) {
		textureGroup.setSelected(textureNameToButton.get(name),true);
		try {
			if ("none".equals(name)) {
				appearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.INHERITED, Texture2D.class);
			} else {
				ImageData img = ImageData.load(Input.getInput(textureNameToTexture.get(name)));
				tex = TextureUtility.createTexture(appearance, "polygonShader", img, false);
				setTextureScale(getTextureScale());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void makeFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = Secure.getProperty(SystemProperties.JREALITY_DATA);
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		fileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
	}
	
	private void loadTexture() {
		File file = null;
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				ImageData img = ImageData.load(Input.getInput(file));
				applyTexture(img);
				setTextureScale(getTextureScale());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void applyTexture(ImageData imageData) {
		if (imageData != null) {
			TextureUtility.createTexture(
					appearance,
					CommonAttributes.POLYGON_SHADER,
					imageData);
		} else {
			TextureUtility.removeTexture(appearance, "polygonShader");
		}
	}
}
