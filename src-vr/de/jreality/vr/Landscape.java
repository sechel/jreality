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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

public class Landscape implements ActionListener {

	private HashMap boxes=new HashMap();

	private static String sideNames= "rt,lf,up,dn,bk,ft";

	private static String[][] defaultLandscapes = {
		//{"snow","textures/jms/jms_", sideNames, ".JPG","textures/jms/jms_dn_seamless.JPG","10"},
		{"snow","textures/jms_hc/jms_hc_", sideNames, ".png","textures/jms_hc/jms_hc_dn_seamless.png","10"},
		//{"tropic","textures/tropseadusk/tropseadusk512_", sideNames, ".jpg","textures/tropseadusk/tropseadusk512_dnSeamless.jpg","10"},
		{"tropic","textures/tropseadusk_hc/tropseadusk512_hc_", sideNames, ".jpg","textures/tropseadusk_hc/tropseadusk512_hc_dn_seamless.jpg","10"},
		//{"mountain","textures/malrav11/malrav11sky_", sideNames, ".jpg", "textures/mxsnow0.jpg","10"},
		//{"desert","textures/dragonvale/dragonvale_", sideNames, ".jpg","textures/dragonvale/dragonvale_dnSeamless.jpg","10"},
		{"desert","textures/dragonvale_hc/dragonvale_hc_", sideNames, ".jpg","textures/dragonvale_hc/dragonvale_hc_dn_seamless.jpg","10"},
		{"night","textures/dragonmoon/dragonmoon_", sideNames, ".jpg","textures/dragonmoon/dragonmoon_dnSeamless.jpg","10"},
		{"tiles dark", null, null, null, "textures/recycfloor1_fin.png", "50", "80 80 120", "0 0 0"},
		{"tiles bright", null, null, null, "textures/recycfloor1_fin.png", "50", "225 225 245", "0 0 0"}
	};

	Color upColor, downColor;
	Box selectionComponent;
	String selectedBox;

	private int selectionIndex;

	private String[][] skyboxes;

	private ImageData terrainTexture;
	private ImageData[] cubeMap;
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
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < skyboxes.length; i++) {
			JRadioButton button = new JRadioButton(skyboxes[i][0]);
			button.addActionListener(this);
			if ( (selected == null && i==0) || skyboxes[i][0].equals(selected)) {
				selectedBox=skyboxes[i][0];
				button.setSelected(true);
				selectionIndex=i;
			}
			buttonGroupComponent.add(button);
			group.add(button);
			boxes.put(skyboxes[i][0], new Integer(i));
		}
		try {
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Landscape(String selected) {
		this(defaultLandscapes,selected);
	}

	public Landscape() {
		this(defaultLandscapes, null);
	}

	public double getTerrainTextureScale() {
		return Double.parseDouble(skyboxes[selectionIndex][5]);
	}

	public ImageData[] getCubeMap() {
		return cubeMap;
	}

	public ImageData getTerrainTexture() {
		return terrainTexture;
	}

	public static void main(String[] args) {    
		Landscape l=new Landscape();
		JFrame f = new JFrame("test");
		f.add(l.selectionComponent);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void actionPerformed(ActionEvent e) {
		selectedBox = e.getActionCommand();
		selectionIndex = ((Integer)boxes.get(selectedBox)).intValue();
		try {
			load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		fireChange();
	}

	private void load() throws IOException {
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
		if (selectedLandscape.length == 8) {
			String[] up = selectedLandscape[6].split(" ");
			String[] down = selectedLandscape[7].split(" ");
			upColor=new Color(Integer.parseInt(up[0]), Integer.parseInt(up[1]), Integer.parseInt(up[2]));
			downColor=new Color(Integer.parseInt(down[0]), Integer.parseInt(down[1]), Integer.parseInt(down[2]));			
		} else {
			upColor=downColor=null;
		}
	}

	public JComponent getSelectionComponent() {
		return selectionComponent;
	}

	private final transient ArrayList<ChangeListener> listeners=new ArrayList<ChangeListener>();

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

	public Color getDownColor() {
		return downColor;
	}

	public Color getUpColor() {
		return upColor;
	}
}
