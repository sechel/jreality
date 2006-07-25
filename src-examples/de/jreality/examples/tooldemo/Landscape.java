package de.jreality.examples.tooldemo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;

public class Landscape implements ActionListener {

  private HashMap boxes=new HashMap();
  
  private static String sideNames= "rt,lf,up,dn,bk,ft";
  
  private static String[][] defaultLandscapes = {
    {"desert","textures/desertstormNew/desertstorm_", sideNames, ".JPG","textures/desertstormNew/desertstorm_dnSeamless.JPG","10"},
    {"plain sky","textures/null_plainsky/desertstorm_", sideNames, ".JPG","textures/grid.jpeg","4"},
    {"mountain","textures/malrav11/malrav11sky_", sideNames, ".jpg", "textures/mxsnow0.jpg","20"},
    {"tropic","textures/tropseadusk/tropseadusk512_", sideNames, ".jpg","textures/tropseadusk/tropseadusk512_dnSeamless.jpg","10"},
    {"dusk","textures/dragonvale/dragonvale_", sideNames, ".jpg","textures/dragonvale/dragonvale_dnSeamless.jpg","10"},
    {"snow","textures/hangingstone/hangingstone512_", sideNames, ".jpg","textures/hangingstone/hangingstone512_dnSeamless.jpg","10"},
    {"night","textures/dragonmoon/dragonmoon_", sideNames, ".jpg","textures/dragonmoon/dragonmoon_dnSeamless.jpg","10"}
  };
  
  Box selectionComponent;
  ToolDemoScene toolScene;
  String selectedBox;

  private String[][] skyboxes;
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
    JScrollPane pane = new JScrollPane(buttonGroupComponent);
    selectionComponent.add("Center", pane);
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < skyboxes.length; i++) {
      JRadioButton button = new JRadioButton(skyboxes[i][0]);
      button.addActionListener(this);
      if ( (selected == null && i==0) || skyboxes[i][0].equals(selected)) {
        selectedBox=skyboxes[i][0];
        button.setSelected(true);
      }
      buttonGroupComponent.add(button);
      group.add(button);
      boxes.put(skyboxes[i][0], new Integer(i));
    }
  }

  public Landscape() {
    this(defaultLandscapes, null);
  }

  public void setToolScene(ToolDemoScene scene) {
    toolScene=scene;
    try {
      applySelection();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void applySelection() throws IOException {
    int i = ((Integer)boxes.get(selectedBox)).intValue();
    toolScene.setSkyBox(TextureUtility.createCubeMapData(skyboxes[i][1], skyboxes[i][2].split(","), skyboxes[i][3]));
    toolScene.setTerrainTexture(ImageData.load(Input.getInput(skyboxes[i][4])), Integer.parseInt(skyboxes[i][5]));
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
    if (toolScene==null) {return;}
    selectedBox = e.getActionCommand();
    try {
      applySelection();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
}
