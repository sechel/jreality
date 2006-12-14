package de.jreality.vr;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Terrain {

	public enum GeometryType {
		DEFAULT("Default"),
		FLAT("Flat"),
		NON_FLAT("Non-flat"),
		CUSTOM("Custom");
		private String name;
		GeometryType(String name) {
			this.name=name;
		}
		public String getName() {
			return name;
		}
	};
	
	public enum TextureType {
		DEFAULT("Default"),
		CUSTOM("Custom"),
		TILES("Tiles"),
		RUBBER_FLOOR("Rubber floor"),
		NONE("None");
		private String name;
		TextureType(String name) {
			this.name=name;
		}
		public String getName() {
			return name;
		}
	};

	private GeometryType geometryType;
	private TextureType textureType;

	private HashMap<GeometryType, ButtonModel> geometryButtons = new HashMap<GeometryType, ButtonModel>();
	private HashMap<TextureType, ButtonModel> textureButtons = new HashMap<TextureType, ButtonModel>();
	private Vector<ChangeListener> listeners = new Vector<ChangeListener>();
	private JPanel texureSelection;
	private JPanel geometrySelection;
	
	public Terrain() {
		geometrySelection = new JPanel(new GridLayout(4, 1));
		ButtonGroup terrainGeometrySelection = new ButtonGroup();
		JRadioButton button = new JRadioButton(GeometryType.DEFAULT.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setGeometryType(GeometryType.DEFAULT);
			}
		});
		terrainGeometrySelection.add(button);
		geometrySelection.add(button);
		geometryButtons.put(GeometryType.DEFAULT, button.getModel());
		button = new JRadioButton(GeometryType.FLAT.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setGeometryType(GeometryType.FLAT);
			}
		});
		terrainGeometrySelection.add(button);
		geometrySelection.add(button);
		geometryButtons.put(GeometryType.FLAT, button.getModel());
		
		button = new JRadioButton(GeometryType.NON_FLAT.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setGeometryType(GeometryType.NON_FLAT);
			}
		});
		terrainGeometrySelection.add(button);
		geometrySelection.add(button);
		geometryButtons.put(GeometryType.NON_FLAT, button.getModel());
		
		button = new JRadioButton(GeometryType.CUSTOM.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setGeometryType(GeometryType.CUSTOM);
			}
		});
		terrainGeometrySelection.add(button);
		geometrySelection.add(button);
		geometryButtons.put(GeometryType.CUSTOM, button.getModel());
				
		texureSelection = new JPanel(new GridLayout(4, 1));
		ButtonGroup terrainTextureSelection = new ButtonGroup();
		button = new JRadioButton(TextureType.DEFAULT.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextureType(TextureType.DEFAULT);
			}
		});
		terrainTextureSelection.add(button);
		texureSelection.add(button);
		textureButtons.put(TextureType.DEFAULT, button.getModel());
		
		button = new JRadioButton(TextureType.TILES.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextureType(TextureType.TILES);
			}
		});
		terrainTextureSelection.add(button);
		texureSelection.add(button);
		textureButtons.put(TextureType.TILES, button.getModel());

		button = new JRadioButton(TextureType.NONE.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextureType(TextureType.NONE);
			}
		});
		terrainTextureSelection.add(button);
		texureSelection.add(button);
		textureButtons.put(TextureType.NONE, button.getModel());

		button = new JRadioButton(TextureType.CUSTOM.getName());
		button.getModel().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextureType(TextureType.CUSTOM);
			}
		});
		terrainTextureSelection.add(button);
		texureSelection.add(button);
		textureButtons.put(TextureType.CUSTOM, button.getModel());
		
		setGeometryType(GeometryType.DEFAULT);
		setTextureType(TextureType.DEFAULT);
	}
	
	public GeometryType getGeometryType() {
		return geometryType;
	}
	public void setGeometryType(GeometryType geometryType) {
		if (this.geometryType != geometryType) {
			this.geometryType = geometryType;
			geometryButtons.get(geometryType).setSelected(true);
			fireChange();
		}
	}
	public TextureType getTextureType() {
		return textureType;
	}
	public void setTextureType(TextureType textureType) {
		if (this.textureType != textureType) {
			this.textureType = textureType;
			textureButtons.get(textureType).setSelected(true);
			fireChange();
		}
	}
	public void setTextureType(String name) {
		TextureType type = null;
		TextureType t = TextureType.DEFAULT;
		if (t.getName().equals(name)) type = t;
		t = TextureType.NONE;
		if (t.getName().equals(name)) type = t;
		t = TextureType.CUSTOM;
		if (t.getName().equals(name)) type = t;
		t = TextureType.TILES;
		if (t.getName().equals(name)) type = t;
		setTextureType(type);
	}
	public void addChangeListener(ChangeListener cl) {
		listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.add(cl);
	}
	
	void fireChange() {
		ChangeEvent ev = new ChangeEvent(this);
		synchronized (listeners) {
			for (ChangeListener cl : listeners) {
				cl.stateChanged(ev);
			}
		}
	}
	
	public JPanel getGeometrySelection() {
		return geometrySelection;
	}

	public JPanel getTexureSelection() {
		return texureSelection;
	}

}
