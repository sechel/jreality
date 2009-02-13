package de.jreality.ui;

import static javax.swing.JFileChooser.FILES_ONLY;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.FileFilter;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

@SuppressWarnings("serial")
public class TextureInspector extends JPanel {
	
	private static final double 
		DEFAULT_TEXTURE_SCALE = 20;
	private static final String 
		DEFAULT_TEXTURE = "none";
	
	// maximal value of texture scale
	private double 
		maximalTextureScale = 400;
	// ratio of maximal value and minimal value of texture scale
	private double 
		logarithmicRange = 400;

	private JButton 
		textureLoadButton = new JButton("Load...", ImageHook.getIcon("folder.png")),
		removeButton = new JButton(ImageHook.getIcon("remove.png"));
	private ButtonGroup 
		textureGroup = new ButtonGroup();
	private JSliderVR
		texScaleSlider = new JSliderVR(SwingConstants.HORIZONTAL, 0, 100, 0);
	private HashMap<String, String> 
		textureNameToTexture = new HashMap<String, String>();
	private HashMap<String, ButtonModel> 
		textureNameToButton = new HashMap<String, ButtonModel>();
	private JFileChooser 
		fileChooser = null;

	private Texture2D 
		tex = null;
	private Appearance 
		appearance = null;
	
	private JPanel 
		texPanel = new JPanel();
	private JScrollPane 
		texScroller = new JScrollPane(texPanel);
	private ActionListener 
		texturesListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			updateTexture();
		}
	};
	
	private GridBagConstraints 
		c = new GridBagConstraints();

	
	public TextureInspector() {
		setLayout(new MinSizeGridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		
		texPanel.setLayout(new GridLayout(0, 3, 2, 2));
		texScroller.setMinimumSize(new Dimension(100, 200));
		texScroller.setViewportBorder(null);
		c.weighty = 1.0;
		c.weightx = 1.0;
		add(texScroller, c);
		
		// load button
		textureLoadButton.setToolTipText("Add a new texture");
		textureLoadButton.setMargin(new Insets(0,5,0,5));
		textureLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadTexture();
			}
		});
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add(textureLoadButton, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		removeButton.setToolTipText("Remove the current texture");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTexture();
			}
		});
		add(removeButton, c);
		
		// scale slider
		texScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateTextureScale();
			}
		});
		c.gridwidth = 1;
		c.weightx = 0.0;
		add(new JLabel("Scale"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		add(texScaleSlider, c);
		
		makeFileChooser();
		setTexture(DEFAULT_TEXTURE);
		setTextureScale(DEFAULT_TEXTURE_SCALE);
	}
	
	
	public void addTexture(String name, String resource) {
		TextureJButton texButton = new TextureJButton(resource);
		texButton.setPreferredSize(new Dimension(50, 50));
		texButton.setActionCommand(name);
		texButton.setToolTipText(resource);
		textureNameToButton.put(name, texButton.getModel());
		texButton.addActionListener(texturesListener);
		textureGroup.add(texButton);
		texPanel.add(texButton);
		doLayout();
		updateUI();
	}
	
	
	public String setTextures(HashMap<String, String> textures) {
		textureNameToTexture = textures;
		List<String> keyList = new LinkedList<String>(textureNameToTexture.keySet());
		Collections.sort(keyList);
		
		for (String name : keyList) {
			String tex = textureNameToTexture.get(name);
			addTexture(name, tex);
		}
		if (keyList.size() > 0) {
			return keyList.get(0);
		} else {
			return "";
		}
	}
	
	
	public HashMap<String, String> getTextures() {
		return textureNameToTexture;
	}
	
	public double getTextureScale() {
		double d = .01 * texScaleSlider.getValue();
		return Math.exp(Math.log(logarithmicRange) * d)/logarithmicRange * maximalTextureScale;
	}
	
	public void setTextureScale(double d) {
		texScaleSlider.setValue(
				(int)(Math.log(d / maximalTextureScale * logarithmicRange)/Math.log(logarithmicRange)*100)
		);
	}
	
	private void updateTextureScale() {
		if (tex != null) {
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(getTextureScale()).getMatrix());
		}
	}

	public String getTexture() {
		ButtonModel bm = textureGroup.getSelection();
		if (bm != null) {
			return textureGroup.getSelection().getActionCommand();
		} else {
			return DEFAULT_TEXTURE;
		}
	}
	
	public void setTexture(String name) {
		ButtonModel bm = textureNameToButton.get(name);
		if (bm != null) {
			textureGroup.setSelected(bm ,true);
		}
	}
	
	private void updateTexture() {
		String texture = getTexture();
		if (appearance != null) {
			String texResource = textureNameToTexture.get(texture);
			if (texResource == null) {
				appearance.setAttribute(
						CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D,
						Appearance.INHERITED,
						Texture2D.class
				);
			} else {
				try {
					ImageData texData = ImageData.load(Input.getInput(texResource));
					applyTexture(texData);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				updateTextureScale();
			}
		}
	}
	
	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
		updateTexture();
		updateTextureScale();
	}

	private void makeFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = Secure.getProperty(SystemProperties.JREALITY_DATA);
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		fileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		fileChooser.setDialogTitle("Select a Texture Image");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(FILES_ONLY);
		fileChooser.setFileHidingEnabled(true);
		javax.swing.filechooser.FileFilter[] filters = FileFilter.createImageReaderFilters();
		for (javax.swing.filechooser.FileFilter filter : filters) {
			fileChooser.addChoosableFileFilter(filter);
		}
		fileChooser.setFileFilter(filters[0]);
	}
	
	private void loadTexture() {
		File file = null;
		Window w = SwingUtilities.getWindowAncestor(this);
		if (fileChooser.showOpenDialog(w) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				ImageData img = ImageData.load(Input.getInput(file));
				applyTexture(img);
				setTextureScale(getTextureScale());
				String texName = textureNameToTexture.keySet().size() + " " + file.getName();
				textureNameToTexture.put(texName, file.getAbsolutePath());
				addTexture(texName, file.getAbsolutePath());
				textureNameToButton.get(texName).setSelected(true);
				int maxScroll = texScroller.getVerticalScrollBar().getMaximum();
				texScroller.getVerticalScrollBar().setValue(maxScroll);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void removeTexture() {
		String texture = getTexture();
		String texResource = textureNameToTexture.get(texture);
		if (texResource != null) {
			textureNameToTexture.remove(texture);
			textureNameToButton.clear();
			texPanel.removeAll();
			String firstTexture = setTextures(textureNameToTexture);
			ButtonModel bm = textureNameToButton.get(firstTexture);
			textureGroup.setSelected(bm, true);
			updateTexture();
		}
	}
	
	
	private void applyTexture(ImageData imageData) {
		if (imageData != null) {
			tex = TextureUtility.createTexture(
					appearance,
					CommonAttributes.POLYGON_SHADER,
					imageData
			);
		} else {
			TextureUtility.removeTexture(appearance, "polygonShader");
		}
	}
	
	public double getMaximalTextureScale() {
		return maximalTextureScale;
	}

	public void setMaximalTextureScale(double maximalTextureScale) {
		this.maximalTextureScale = maximalTextureScale;
	}

	public double getLogarithmicRange() {
		return logarithmicRange;
	}
	
	public void setLogarithmicRange(double logarithmicRange) {
		this.logarithmicRange = logarithmicRange;
	}
	
	
	@Override
	public void updateUI() {
		super.updateUI();
		if (fileChooser != null) {
			fileChooser.updateUI();
		}
	}
	
	
}
