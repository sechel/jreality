package de.jreality.vr;

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
		{"snow","textures/jms/jms_", sideNames, ".JPG","textures/jms/jms_dn_seamless.JPG","10"},
		{"tropic","textures/tropseadusk/tropseadusk512_", sideNames, ".jpg","textures/tropseadusk/tropseadusk512_dnSeamless.jpg","10"},
		{"mountain","textures/malrav11/malrav11sky_", sideNames, ".jpg", "textures/mxsnow0.jpg","10"},
		{"desert","textures/dragonvale/dragonvale_", sideNames, ".jpg","textures/dragonvale/dragonvale_dnSeamless.jpg","10"},
		{"night","textures/dragonmoon/dragonmoon_", sideNames, ".jpg","textures/dragonmoon/dragonmoon_dnSeamless.jpg","10"}
	};

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
		cubeMap=TextureUtility.createCubeMapData(skyboxes[selectionIndex][1], skyboxes[selectionIndex][2].split(","), skyboxes[selectionIndex][3]);
		terrainTexture=ImageData.load(Input.getInput(skyboxes[selectionIndex][4]));
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
}
