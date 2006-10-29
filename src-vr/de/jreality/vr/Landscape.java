package de.jreality.vr;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

public class Landscape {

	private HashMap<String,Integer> boxes=new HashMap<String,Integer>();

	private static String sideNames= "rt,lf,up,dn,bk,ft";

	private static String[][] defaultLandscapes = {
		{"snow","textures/jms_hc/jms_hc_", sideNames, ".png","textures/jms_hc/jms_hc_dn_seamless.png","10", "false", "80 80 120", "0 0 0"},
		{"tropic","textures/tropseadusk_hc/tropseadusk512_hc_", sideNames, ".jpg","textures/tropseadusk_hc/tropseadusk512_hc_dn_seamless.jpg","10", "false", "80 80 120", "0 0 0"},
		//{"mountain","textures/malrav11/malrav11sky_", sideNames, ".jpg", "textures/mxsnow0.jpg","10", "false", "80 80 120", "0 0 0"},
		{"desert","textures/dragonvale_hc/dragonvale_hc_", sideNames, ".jpg","textures/dragonvale_hc/dragonvale_hc_dn_seamless.jpg","10", "false", "80 80 120", "0 0 0"},
		{"night","textures/dragonmoon/dragonmoon_", sideNames, ".jpg","textures/dragonmoon/dragonmoon_dnSeamless.jpg","10", "false", "80 80 120", "0 0 0"},
		{"tiles dark", null, null, null, "textures/recycfloor1_fin.png", "50", "true", "80 80 120", "0 0 0"},
		{"tiles bright", null, null, null, "textures/recycfloor1_clean2.png", "50", "true", "225 225 245", "0 0 0"}
	};

	private final transient ArrayList<ChangeListener> listeners=new ArrayList<ChangeListener>();

	private ButtonGroup group;


	private HashMap<String,ButtonModel> envToButton = new HashMap<String,ButtonModel>();
	Color upColor, downColor;
	Box selectionComponent;
	String selectedBox;

	private int selectionIndex;

	private String[][] skyboxes;

	private ImageData terrainTexture;
	private ImageData[] cubeMap;
	private boolean terrainFlat;

	private double terrainTextureScale;
	/**
	 * 
	 * @param skyboxes an array of skybox descriptions:
	 *  { "name" "pathToFiles/filePrefix", "fileEnding",
	 *    "pathToTerrainTexture/textureFile", "terrainTextureScale" }
	 * @param selected the name of the intially selected sky box
	 */
	public Landscape(String[][] skyboxes, String selected) {
		this.skyboxes=(String[][])skyboxes.clone();
		Box buttonGroupComponent = new javax.swing.Box(BoxLayout.Y_AXIS);
		selectionComponent = new javax.swing.Box(BoxLayout.Y_AXIS);
		selectionComponent.add("Center", buttonGroupComponent);
		group = new ButtonGroup();
		for (int i = 0; i < skyboxes.length; i++) {
			final String name = skyboxes[i][0];
			JRadioButton button = new JRadioButton(skyboxes[i][0]);
			envToButton.put(skyboxes[i][0], button.getModel());
			button.getModel().addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setEvironment(name);
				}
			});
			buttonGroupComponent.add(button);
			group.add(button);
			boxes.put(skyboxes[i][0], new Integer(i));
		}
		if (selected != null) {
			setEvironment(selected);
		}
	}

	public Landscape(String selected) {
		this(defaultLandscapes,selected);
	}

	public Landscape() {
		this(defaultLandscapes, null);
	}

	public static void main(String[] args) {    
		Landscape l=new Landscape();
		JFrame f = new JFrame("test");
		f.add(l.selectionComponent);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private void load(String env) {
		selectionIndex = ((Integer)boxes.get(env)).intValue();
		try {
			String[] selectedLandscape = skyboxes[selectionIndex];
			if (selectedLandscape[1] != null) {
				cubeMap=TextureUtility.createCubeMapData(selectedLandscape[1], selectedLandscape[2].split(","), selectedLandscape[3]);
			} else {
				cubeMap = null;
			}
			if (selectedLandscape[4] != null) {
				terrainTexture=ImageData.load(Input.getInput(selectedLandscape[4]));
			} else {
				terrainTexture = null;
			}

			String upColorString = selectedLandscape[7];
			if (upColorString.equals("null")) {
				upColor = null;
			} else {
				String[] up = selectedLandscape[7].split(" ");
				upColor=new Color(Integer.parseInt(up[0]), Integer.parseInt(up[1]), Integer.parseInt(up[2]));
			}
			String downColorString = selectedLandscape[8];
			if (downColorString.equals("null")) {
				downColor = null;
			} else {
				String[] down = selectedLandscape[8].split(" ");
				downColor=new Color(Integer.parseInt(down[0]), Integer.parseInt(down[1]), Integer.parseInt(down[2]));
			}
			terrainFlat = Boolean.parseBoolean(selectedLandscape[6]);
			terrainTextureScale = Double.parseDouble(skyboxes[selectionIndex][5]);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public JComponent getSelectionComponent() {
		return selectionComponent;
	}

	public ImageData[] getCubeMap() {
		return cubeMap;
	}
	
	public double getTerrainTextureScale() {
		return terrainTextureScale;
	}

	public ImageData getTerrainTexture() {
		return terrainTexture;
	}
	public Color getDownColor() {
		return downColor;
	}

	public Color getUpColor() {
		return upColor;
	}
	
	public boolean isTerrainFlat() {
		return terrainFlat;
	}

	public void addChangeListener(ChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeChangeListener(ChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void fireChange() {
		synchronized (listeners) {
			ChangeEvent e = new ChangeEvent(this);
			for (ChangeListener l : listeners) {
				l.stateChanged(e);
			}
		}
	}

	public String getEnvironment() {
		return skyboxes[selectionIndex][0];
	}
	
	public void setEvironment(String environment) {
		ButtonModel model = envToButton.get(environment);
			group.setSelected(model, true);
			load(environment);
			fireChange();
	}
}
