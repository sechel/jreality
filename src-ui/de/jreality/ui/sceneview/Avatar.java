package de.jreality.ui.sceneview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.toolsystem.ToolUtility;
import de.jreality.ui.sceneview.SceneView.RunningEnvironment;

public class Avatar {
	
	public static final double DEFAULT_SPEED = 4;
	
	private SceneView sceneView;
	private SceneGraphComponent avatar;
	private SceneGraphComponent camera;
	private ShipNavigationTool shipNavigationTool;
	private PointerDisplayTool pointerDisplayTool;
	private Tool headTool;
	private Box panel;
	private JSlider speedSlider;
	
	public Avatar(SceneView sceneView, CameraManager cameraManager) {
		this.sceneView = sceneView;
		
		SceneView.RunningEnvironment environment = sceneView.getRunningEnvironment();
		boolean portal = environment == RunningEnvironment.PORTAL;
		boolean portalRemote = environment == RunningEnvironment.PORTAL_REMOTE;
		
		avatar = cameraManager.getCameraBase();
		camera = cameraManager.getCameraComponent();
		
		shipNavigationTool = new ShipNavigationTool();
		avatar.addTool(shipNavigationTool);
		if (portal || portalRemote) {
			pointerDisplayTool = new PointerDisplayTool() {
				{
					setVisible(false);
					setHighlight(true);
				}
				@Override
				public void perform(ToolContext tc) {
					PickResult currentPick = tc.getCurrentPick();
					boolean visible = currentPick != null && currentPick.getPickPath().startsWith(tc.getAvatarPath());
					setVisible(visible);
					if (visible) {
						super.perform(tc);
						// compute length:
						 double[] pickAvatar = ToolUtility.worldToAvatar(tc, currentPick.getWorldCoordinates());
						 Matrix pointer = new Matrix(tc.getTransformationMatrix(AVATAR_POINTER));
						 double f = pointer.getEntry(3, 3);
						 pickAvatar[0]-=pointer.getEntry(0, 3)/f;
						 pickAvatar[1]-=pointer.getEntry(1, 3)/f;
						 pickAvatar[2]-=pointer.getEntry(2, 3)/f;
						 setLength(Rn.euclideanNorm(pickAvatar));
					} 
				}
			};
			avatar.addTool(pointerDisplayTool);
			
			shipNavigationTool.setPollingDevice(false);
		}
		
		if (!portal && !portalRemote) {
			headTool = new HeadTransformationTool();
		} 
		if (portal) {
			try {
				headTool = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (portalRemote) {
			try {
				headTool = (Tool) Class.forName("de.jreality.tools.RemotePortalHeadMoveTool").newInstance();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		camera.addTool(headTool);
		
		sceneView.setAvatarPath(cameraManager.getCameraBasePath());
	}
	
	public void unInstall() {
		avatar.removeTool(shipNavigationTool);
		avatar.removeTool(pointerDisplayTool);
		camera.removeTool(headTool);
		sceneView.setAvatarPath(null);
	}

	public Component getPanel() {
		if (panel == null) {
			panel = new Box(BoxLayout.X_AXIS);
			panel.add(Box.createHorizontalStrut(5));
			JLabel gainLabel = new JLabel("speed");
			panel.add(gainLabel);
			speedSlider = new JSlider(0, 3000, (int) (100 * DEFAULT_SPEED));
			speedSlider.setPreferredSize(new Dimension(200,26));
			speedSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					setNavigationSpeed(getNavigationSpeed());
				}
			});
			//gbc.fill = GridBagConstraints.HORIZONTAL;
			panel.add(speedSlider);
		}
		return panel;
	}
	
	public double getNavigationSpeed() {
		double speed = 0.01*speedSlider.getValue();
		return speed;
	}
	
	public void setNavigationSpeed(double navigationSpeed) {
		int speed = (int)(100*navigationSpeed);
		speedSlider.setValue(speed);
		shipNavigationTool.setGain(navigationSpeed);
	}
	
}
