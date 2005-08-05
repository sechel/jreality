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
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.*;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;


/**
 *
 * Reader for OOGL files (Geomview format).
 *
 * @author weissman
 *
 */
public class ReaderOOGL extends AbstractReader {

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    root = load(input.getInputStream());
  }
  
  /**
   * @param inputStream
   * @return
   */
  SceneGraphComponent load(InputStream inputStream) {
      Reader r = new BufferedReader(new InputStreamReader(inputStream));
      SceneGraphComponent disk=new SceneGraphComponent();
       
      StreamTokenizer st = new StreamTokenizer(r);
      
      st.resetSyntax();
      st.eolIsSignificant(false);
      st.wordChars('0', '9');
      st.wordChars('A', 'Z');
      st.wordChars('a' , 'z');
      st.wordChars('.','.');
      st.wordChars('-','-');
      st.wordChars('+','+');
      st.wordChars('\u00A0', '\u00FF' );
      st.ordinaryChar('=');
      st.ordinaryChar('{');
      st.ordinaryChar('}');
     st.whitespaceChars('\u0000',  '\u0020');
      st.commentChar('#');
      //st.parseNumbers();
       
      SceneGraphComponent sgc = loadOneLevel(st, 0);
      SceneGraphUtility.setDefaultMatrix(sgc);
    return sgc;

  }
  
  private SceneGraphComponent loadOneLevel(StreamTokenizer st, int bracketDepth)  {
       SceneGraphComponent current= null;
      //int bc = 0;
      //int oc =0;
      //int mode = 0;
      String name = "noName";
      LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"start.");
      try {
              st.nextToken();
              if (st.ttype =='}') {
                  return null;
              }
                 if (st.ttype =='{')  {
                   SceneGraphComponent contents = loadOneLevel(st, bracketDepth+1);
                st.nextToken();
                if (st.ttype != '}')  {
                  LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Unmatched left bracket at  "+st.lineno());
                  return contents;
                } //else  st.nextToken();
                return contents;
               }
               if (bracketDepth > 0)  {
                if (st.ttype == StreamTokenizer.TT_WORD && !isOOGLKeyword(st.sval)) {
                  name = st.sval;
                  st.nextToken();
                }       
                  if (st.ttype == '=')  {
                  st.nextToken();
                } else if (st.ttype == '<') { // read from file
                  ReaderOOGL or = new ReaderOOGL();
                  st.nextToken();
                  Input in = input.getRelativeInput(st.sval);
                  return or.read(in);
                }
               }
              //LoggingSystem.getLogger().log(Level.FINER,"next");
              if (st.ttype ==StreamTokenizer.TT_WORD)   {
                double[][] verts = null, vc =  null, fc = null, vn = null, tc = null;
                  if ( st.sval.indexOf("OFF") != -1) {
                  boolean hasTC = st.sval.indexOf("ST") >= 0;
                  boolean hasVC = st.sval.indexOf("C") >= 0;
                  boolean hasVN = st.sval.indexOf("N") >= 0;
                      int[][] indices = null;
                  current =SceneGraphUtility.createFullSceneGraphComponent("OFF-node");
                    //LoggingSystem.getLogger().log(Level.FINER,"found object!");
                  int numV, numE, numF;
                  int vLength = (st.sval.indexOf("4") != -1) ?  4 : 3;
                  st.nextToken();
                  numV = Integer.parseInt(st.sval);
                  st.nextToken();
                  numF = Integer.parseInt(st.sval);
                  st.nextToken();
                  numE = Integer.parseInt(st.sval);
                  verts = new double[numV][vLength];
                  indices = new int[numF][];
                  if (hasTC) tc = new double[numV][2];
                  if (hasVC) vc = new double[numV][4];
                  if (hasVN) vn = new double[numV][3];
                      for (int i=0; i<numV; ++i)  {
                           for (int j = 0; j<vLength; ++j)  {
                      st.nextToken();
                      //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                      verts[i][j] = Double.parseDouble(st.sval);                        
                    }
                             if (hasVN)
                              for (int j = 0; j<3; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          vn[i][j] = Double.parseDouble(st.sval);                       
                        }
                             if (hasVC)
                              for (int j = 0; j<4; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          vc[i][j] = Double.parseDouble(st.sval);                       
                        }
                             if (hasTC)
                              for (int j = 0; j<2; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          tc[i][j] = Double.parseDouble(st.sval);                       
                        }
                         }
                  for (int i =0; i< numF; ++i)  {
                    st.nextToken();
                    int size = Integer.parseInt(st.sval);
                    indices[i] = new int[size];
                    for (int j = 0; j<size; ++j)  {
                      st.nextToken();
                      //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                      indices[i][j] = Integer.parseInt(st.sval);
                    }
                    st.eolIsSignificant(true);
                    st.nextToken();
                    if (st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) {
                      //has a color
                      if (fc == null) fc = new double[numF][4];
                      //int colComps = Integer.parseInt(st.sval);
                      for (int j = 0; j<4; ++j) {
                               if (st.ttype == StreamTokenizer.TT_EOL ||  st.ttype == StreamTokenizer.TT_EOF) break;
                        fc[i][j] = Double.parseDouble(st.sval);
                        //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                        st.nextToken();
                    }
                      // read the rest of the line if it hasnt' been read
                          while (st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF)  
                            st.nextToken();
                    } 
                    st.eolIsSignificant(false);
                  }
                  LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Read "+numV+" vertices and "+numF+" faces");
                  IndexedFaceSet ifs = IndexedFaceSetUtility.createIndexedFaceSetFrom(indices, verts, vn, vc, tc, null, fc);
                  ifs.setName("OFF Geometry");
                      //GeometryUtility.calculateAndSetFaceNormals(ifs);
                  ifs.buildEdgesFromFaces();
                  current.setGeometry(ifs);
              }
                else if ( st.sval.indexOf("MESH") != -1) {
                  current =SceneGraphUtility.createFullSceneGraphComponent("MESH-node");
                  boolean closedU = st.sval.indexOf("u") >= 0;
                  boolean closedV = st.sval.indexOf("v") >= 0;
                    boolean hasTC = st.sval.indexOf("ST") >= 0;
                  boolean hasVC = st.sval.indexOf("C") >= 0;
                  boolean hasVN = st.sval.indexOf("N") >= 0;
                  int vLength = (st.sval.indexOf("4") != -1) ?  4 : 3;
                  //LoggingSystem.getLogger().log(Level.FINER,"found object!");
                  st.nextToken();
                  int u = Integer.parseInt(st.sval);
                  st.nextToken();
                  int v = Integer.parseInt(st.sval);
                  int n = u*v;
                  verts = new double[n][vLength];
                  if (hasTC) tc = new double[n][2];
                  if (hasVC) vc = new double[n][4];
                  if (hasVN) vn = new double[n][3];
                  for (int i = 0; i<n; ++i) {
                        for (int j = 0; j<vLength; ++j) {
                      st.nextToken();
                      //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                      verts[i][j] = Double.parseDouble(st.sval);                        
                    }
                             if (hasVN)
                              for (int j = 0; j<3; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          vn[i][j] = Double.parseDouble(st.sval);                       
                        }
                             if (hasVC)
                              for (int j = 0; j<4; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          vc[i][j] = Double.parseDouble(st.sval);                       
                        }
                             if (hasTC)
                              for (int j = 0; j<2; ++j) {
                          st.nextToken();
                          //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
                          tc[i][j] = Double.parseDouble(st.sval);                       
                        }
                  }
                  LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Read "+n+" vertices");
                  QuadMeshShape qms = new QuadMeshShape(u,v, closedU, closedV);
                  qms.setName("MESH Geometry");
                      IndexedFaceSetUtility.setIndexedFaceSetFrom(qms, null, verts, vn, vc, tc, null, fc);
                      //qms.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vLength).createReadOnly(verts));
                  qms.buildEdgesFromFaces();
                  //GeometryUtility.calculateAndSetNormals(qms);
                  current.setGeometry(qms);
                  }
        
            else if ( st.sval.indexOf("VECT") != -1) {
              current =SceneGraphUtility.createFullSceneGraphComponent("VECT-node");
                //LoggingSystem.getLogger().log(Level.FINER,"found object!");
              st.nextToken();
              int numCurves = Integer.parseInt(st.sval);
              st.nextToken();
              int totalVerts = Integer.parseInt(st.sval);
              int[] sizes = new int[numCurves];
              int[] colors = new int[numCurves];
              boolean[] closed = new boolean[numCurves];
              int vLength = 3;
              int[][] indices = new int[numCurves][];
              int vertCount = 0;
              for (int i = 0; i<numCurves; ++i) {
                st.nextToken();
                int realCount = 0;
              //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
              int val = Integer.parseInt(st.sval);  
                if (val < 0) {
                val = sizes[i] = -val;
                realCount = sizes[i] + 1;
                closed[i] = true;
              } else {
                sizes[i] = val;
                closed[i] = false;
                realCount = sizes[i];
              }
              indices[i] = new int[realCount];
              for (int j =0; j< val; ++j) {
                indices[i][j] = vertCount+j;
              }
              if (closed[i]) indices[i][val] = vertCount;
              vertCount += val;
              }
              int totalColors = 0;
              for (int i = 0; i<numCurves; ++i) {
                st.nextToken();
              //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
              colors[i] = Integer.parseInt(st.sval);  
              totalColors += colors[i];
              }
              verts = new double[totalVerts][vLength];
              vc = new double[totalVerts][4];
              for (int i = 0; i<totalVerts; ++i)  {
                for (int j = 0; j<vLength; ++j) {
              st.nextToken();
              //LoggingSystem.getLogger().log(Level.FINER,"Token is "+st.sval);
              verts[i][j] = Double.parseDouble(st.sval);                        
            }
          }
              // parse the colors now
              int vertC = 0;
              for (int i = 0; i<numCurves; ++i) {
                int j;
                for (j = 0; j<colors[i]; ++j) {
                    for (int k = 0; k<4; ++k) {
                      st.nextToken();
                      vc[vertC][k] = Double.parseDouble(st.sval);      
                  }
                  vertC++;
                }
                for ( ; j< sizes[i]; ++j) {
                  for (int k = 0; k<4; ++k) {
                      vc[vertC][k] = vc[vertC-1][k];                    
                  }
                  vertC++;
                }
                }
                LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Read "+numCurves+" curves and "+totalVerts+ " vertices");
              IndexedLineSet ils = new IndexedLineSet(totalVerts);
              ils.setName("VECT Geometry");
                  ils.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vLength).createReadOnly(verts));
              ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vc));
              ils.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(indices));
              
              current.setGeometry(ils);
            } 
            else if ( st.sval.indexOf("SPHERE") != -1) {
              double[] x = new double[4];
              for (int i = 0; i<4; ++i) {
                  st.nextToken();
                  x[i] = Double.parseDouble(st.sval); 
              }
              current = Primitives.sphere(x[0], x[1], x[2], x[3]);
              current.setName("SPHERE-node");
            }
              else if ( st.sval.indexOf("TLIST") != -1) {
                current =SceneGraphUtility.createFullSceneGraphComponent("TLIST-node");
                boolean transposed = (st.sval.indexOf("FLIP") != -1);
                if (transposed) LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Matrices are trasnposed");
              double[] mat = new double[16];
              boolean reading  = true;
              int count = 0;
              while ( reading ) {
                  for (int j = 0; j<16; ++j)  {
                  st.nextToken();
                  if (st.ttype != StreamTokenizer.TT_WORD)  {reading = false; break;}
                  mat[j] = Double.parseDouble(st.sval);                       
                }
                  if (transposed) Rn.transpose(mat,mat);
                  if (reading == false) break;
                  SceneGraphComponent sgc = new SceneGraphComponent();
                sgc.setName("tlist child"+count++);
                sgc.setTransformation(new Transformation(mat));
                //LoggingSystem.getLogger().log(Level.FINER,"Matrix read: "+Rn.matrixToString(mat));
              current.addChild(sgc);                }
            }
            else if ( st.sval.indexOf("LIST") != -1) {
                current =SceneGraphUtility.createFullSceneGraphComponent("LIST-node");
              SceneGraphComponent child;
              while ( (child = loadOneLevel(st, bracketDepth)) != null) {
                current.addChild( child);
              }
            }
            else if ( st.sval.indexOf("INST") != -1) {
              current =SceneGraphUtility.createFullSceneGraphComponent("INST-node");
              SceneGraphComponent geom =SceneGraphUtility.createFullSceneGraphComponent("INST-unit");
              
              st.nextToken();
              if (st.ttype != StreamTokenizer.TT_WORD)  {
                LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"Invalid type in INST: "+st.ttype);
                return null;
              }
              // we require that the geometry comes first
              if (st.sval.indexOf("unit") != -1 || st.sval.indexOf("geom") != -1) {
                geom = loadOneLevel(st, bracketDepth);
              } 
              st.nextToken();
              //while (st.ttype == '}') st.nextToken();
              // followed by the matrices
              if (st.sval.indexOf("tlist") != -1 || st.sval.indexOf("transform") != -1) {
                current = loadOneLevel(st, bracketDepth);
              }
              for (int i = 0; i<current.getChildComponentCount(); ++i)  {
                SceneGraphComponent cc = current.getChildComponent(i);
                cc.addChild(geom);
              }

            }
          //current.setName(name);
           }
       } catch (IOException e) {
          e.printStackTrace();
      }
       if (current!= null)  {
          LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"made "+current.getChildComponentCount()+" components");
          LoggingSystem.getLogger(ReaderOOGL.class).log(Level.FINER,"done.");           
       }
      return current;
  }

  static String[] OOGLkeys = {"OFF", "MESH", "VECT", "SKEL", "LIST", "inst", "tlist", "transforms", "unit", "INST","QUAD","BEZ", "BBP"};
/**
 * @param sval
 * @return
 */
private static boolean isOOGLKeyword(String sval) {
  for (int i =0; i<OOGLkeys.length; ++i)  {
    if (sval.indexOf(OOGLkeys[i]) != -1) return true;
  }
  return false;
}

}
