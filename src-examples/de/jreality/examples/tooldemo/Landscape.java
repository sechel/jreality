package de.jreality.examples.tooldemo;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

public class Landscape {

  private static String[][] defaultLandscapes = {
    {"desert","textures/desertstormNew/desertstorm_", ".JPG","textures/desertstormNew/desertstorm_dnSeamless.JPG","10"},
    {"plain sky","textures/null_plainsky/desertstorm_", ".JPG","textures/grid.jpeg","4"},
    {"mountain","textures/malrav11/malrav11sky_", ".jpg", "textures/mxsnow0.jpg","20"},
    {"tropic","textures/tropseadusk/tropseadusk512_", ".jpg","textures/tropseadusk/tropseadusk512_dnSeamless.jpg","10"},
    {"dusk","textures/dragonvale/dragonvale_", ".jpg","textures/dragonvale/dragonvale_dnSeamless.jpg","10"},
    {"snow","textures/hangingstone/hangingstone512_", ".jpg","textures/hangingstone/hangingstone512_dnSeamless.jpg","10"},
    {"night","textures/dragonmoon/dragonmoon_", ".jpg","textures/dragonmoon/dragonmoon_dnSeamless.jpg","10"}
  };
  
  Box selectionComponent;
  
  /**
   * 
   * @param skyboxes an array of skybox descriptions:
   *  { "name" "pathToFiles/filePrefix", "fileEnding",
   *    "pathToTerrainTexture/textureFile", "terrainTextureScale" }
   * @param selected the name of the intially selected sky box
   */
  public Landscape(String[][] skyboxes, String selected) {
    Box buttonGroupComponent = new javax.swing.Box(BoxLayout.Y_AXIS);
    selectionComponent = new javax.swing.Box(BoxLayout.Y_AXIS);
    JScrollPane pane = new JScrollPane(buttonGroupComponent);
    selectionComponent.add("Center", pane);
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < skyboxes.length; i++) {
      JRadioButton button = new JRadioButton(skyboxes[i][0]);
      if ( (selected == null && i==0) || skyboxes[i][0].equals(selected)) button.setSelected(true);
      buttonGroupComponent.add(button);
      group.add(button);
    }
  }

  
  
  public static void main(String[] args) {
    
    Landscape l=new Landscape(defaultLandscapes, null);
    JFrame f = new JFrame("test");
    f.add(l.selectionComponent);
    f.pack();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
