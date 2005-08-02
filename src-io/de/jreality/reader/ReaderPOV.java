/*
 * Created on May 8, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.reader;

import java.io.*;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.Cylinder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.math.MatrixBuilder;

/**
 *
 * Simple POV reader. Note: The default is to use
 * Cylinders, see {@link ReaderPOV#setUseCylinders(boolean)}. 
 *
 * @author timh
 *
 */
public class ReaderPOV extends AbstractReader {

  private boolean useCylinders = false;
  private static int UNIT_DISK_DETAIL = 32;

  public void setUseCylinders(boolean useCylinders) {
    this.useCylinders = useCylinders;
  }
  
  public void setInput(Input input) throws IOException {
    super.setInput(input);
    root = load(input.getInputStream());
  }

  private SceneGraphComponent load(InputStream inputStream) {
    Reader r = new BufferedReader(new InputStreamReader(inputStream));
    SceneGraphComponent disk = new SceneGraphComponent();
    if (useCylinders) {
      disk.setGeometry(new Cylinder());
    } else {
      disk.setGeometry(new UnitDisk(UNIT_DISK_DETAIL));
    }
    
    MatrixBuilder.euclidian().scale(1,1,0.2).assignTo(disk);
    
    StreamTokenizer st = new StreamTokenizer(r);

    st.ordinaryChar('{');
    st.ordinaryChar('}');
    st.parseNumbers();

    SceneGraphComponent root = new SceneGraphComponent();
    SceneGraphComponent current = null;
    int bc = 0;
    int oc = 0;
    LoggingSystem.getLogger(this).fine("start.");
    try {
      while (st.ttype != StreamTokenizer.TT_EOF) {
        st.nextToken();
        if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals("object")) {
          while (st.ttype != '{')
            st.nextToken();
          oc = bc;
          current = new SceneGraphComponent();
        }
        if (st.ttype == '{') bc++;
        if (st.ttype == '}') bc--;
        if (bc == oc && current != null) {
          root.addChild(current);
          current = null;
        }
        if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals("Disk")
            && current != null) {
          current.addChild(disk);
        }

        if (st.ttype == StreamTokenizer.TT_WORD && st.sval.equals("matrix")
            && current != null) {
          current.setTransformation(readMatrix(st));
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    LoggingSystem.getLogger(this).fine(
        "made " + root.getChildComponentCount() + " components");
    LoggingSystem.getLogger(this).fine("done.");
    return root;
  }

  private Transformation readMatrix(StreamTokenizer st) throws IOException {
    double[] d = new double[12];
    double[] m = new double[16];
    for (int i = 0; i < 12; i++) {
      int b = 0;
      while (st.ttype != StreamTokenizer.TT_NUMBER && b < 40) {
        st.nextToken();
        b++;
      }
      if (b == 40)
          LoggingSystem.getLogger(this).fine(
              "Error number " + i + " was aborted due to recursion.");
      d[i] = st.nval;
      st.nextToken();
      if (st.ttype == StreamTokenizer.TT_WORD && st.sval.startsWith("E")) {
        int exp = Integer.parseInt(st.sval.substring(1));
        d[i] *= Math.pow(10, exp);
        st.nextToken();
      }
    }
    for (int j = 0; j < 4; j++)
      for (int i = 0; i < 3; i++)
        m[j + 4 * i] = d[i + 3 * j];
    m[3 + 4 * 3] = 1;
    Transformation t = new Transformation();
    t.setMatrix(m);
    return t;
  }

  private static class UnitDisk extends IndexedFaceSet {

    public UnitDisk(int detail) {
      super();

      double r = 1;
      double[] vertices = new double[detail * 3 + 3];
      double[] normals = new double[detail * 3 + 3];
      //int[][] faces =new int[detail][3];
      int[] faces = new int[detail * 3];
      compute(vertices, normals, faces);

      setVertexCountAndAttributes(Attribute.COORDINATES,
          StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(vertices));
      //setVertexAttributes(Attribute.NORMALS,
      //        StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(normals));
      setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY
          .inlined(3).createReadOnly(faces));
      buildEdgesFromFaces();
      GeometryUtility.calculateAndSetFaceNormals(this);
      GeometryUtility.calculateAndSetVertexNormals(this);
    }

    private void compute(double[] vertices, double[] normals, int[] faces) {
      // The disk:
      int k = UNIT_DISK_DETAIL;
      for (int i = 0; i < k; i++) {

        faces[3 * i + 0] = (i);
        faces[3 * i + 1] = (((i + 1) % k));
        faces[3 * i + 2] = (k);

        double theta = 2 * Math.PI * i / k;
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);

        int pos = 3 * (i);
        vertices[pos + 0] = cosT;
        vertices[pos + 1] = sinT;
        vertices[pos + 2] = 0;
        normals[pos + 0] = 0;
        normals[pos + 1] = 0;
        normals[pos + 2] = 1;
      }
      vertices[3 * (k)] = 0;
      vertices[3 * (k) + 1] = 0;
      vertices[3 * (k) + 2] = 0;

      normals[3 * (k)] = 0;
      normals[3 * (k) + 1] = 0;
      normals[3 * (k) + 2] = 1;

    }

  }

}
