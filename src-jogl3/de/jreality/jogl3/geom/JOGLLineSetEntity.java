package de.jreality.jogl3.geom;

import java.util.HashMap;
import java.util.Set;

import javax.media.opengl.GL3;

import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.GLVBOInt;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.event.GeometryEvent;

public class JOGLLineSetEntity extends JOGLPointSetEntity {

	private HashMap<String, GLVBO> lineVbos = new HashMap<String, GLVBO>();
	
	
	public JOGLLineSetEntity(IndexedLineSet node) {
		super(node);
	}

	@Override
	public void geometryChanged(GeometryEvent ev) {
//		System.out.println("JOGLLineSetEntity.geometryChanged()");
		super.geometryChanged(ev);
	}
	//TODO updateData

	public GLVBO getLineVBO(String s) {
		// TODO Auto-generated method stub
		return lineVbos.get(s);
	}
	public int getNumLineVBOs(){
		return lineVbos.size();
	}
	public GLVBO[] getAllLineVBOs(){
		GLVBO[] ret = new GLVBO[lineVbos.size()];
		Set<String> keys = lineVbos.keySet();
		int i = 0;
		for(String s : keys){
			ret[i] = lineVbos.get(s);
			i++;
		}
		return ret;
	}
	
	public void updateData(GL3 gl) {
		
		//if (!dataUpToDate) {
			super.updateData(gl);
			lineVbos.clear();
			//TODO generate VBOs for line rendering
			IndexedLineSet ls = (IndexedLineSet)getNode();
			//create lineulation and save in indexArray
			int count = 0;
			for(int i = 0; i < ls.getNumEdges(); i++){
				IntArray edge = ls.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
				count += 2*(edge.size()-1);
			}
			
			int[] indexArray = new int[count];
			count = 0;
			for(int i = 0; i < ls.getNumEdges(); i++){
				IntArray edge = ls.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
				for(int j = 0; j < edge.size()-1; j++){
					indexArray[count+2*j] = edge.getValueAt(j);
					indexArray[count+2*j+1] = edge.getValueAt(j+1);
				}
				count += 2*(edge.size()-1);
//				if(ls.getName().equals("face-set 3"))
//					System.out.println("edge " + i + " is from " + edge.getValueAt(0) + " to " + edge.getValueAt(1));
			}
			
			//now read all the other attributes
			//first edge attributes
			Set<Attribute> aS = ls.getEdgeAttributes().storedAttributes();
			for(Attribute a : aS){
				String shaderName = "";
				for(String s : a.getName().split(" ")){
					shaderName = shaderName + s;
				}
				//System.out.println("edge attribute: "+a.getName());
				//skip indices, already done earlier
				if(shaderName.equals("indices"))
					continue;
				DataList attribs = ls.getEdgeAttributes(a);
				if(isDoubleArrayArray(attribs.getStorageModel())){
					//the array containing one item per index
					double[] inflatedAttributeArray = new double[indexArray.length*4];
					//count = 0;
					
					for(int i = 0; i < ls.getNumEdges(); i++){
						DoubleArray dA = (DoubleArray)attribs.get(i);
						//now for each point of the edge
						for(int k = 0; k < 2; k++){
							//for each component of the attribute vector
							inflatedAttributeArray[(2*i+k)*4+0] = dA.getValueAt(0);
							if(dA.size() > 1)
								inflatedAttributeArray[(2*i+k)*4+1] = dA.getValueAt(1);
							else
								inflatedAttributeArray[(2*i+k)*4+1] = 0;
							if(dA.size() > 2)
								inflatedAttributeArray[(2*i+k)*4+2] = dA.getValueAt(2);
							else
								inflatedAttributeArray[(2*i+k)*4+2] = 0;
							if(dA.size() > 3)
								inflatedAttributeArray[(2*i+k)*4+3] = dA.getValueAt(3);
							else
								inflatedAttributeArray[(2*i+k)*4+3] = 1;
						}
					}
					lineVbos.put("edge_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "edge_"+a.getName()));
					//vbos.add(new GLVBOFloat(state.getGL(), Rn.convertDoubleToFloatArray(inflatedAttributeArray), "face_"+a.getName()));
//					System.out.println("creating " + "edge_"+a.getName());
				}else if(isIntArray(attribs.getStorageModel())){
					//the array containing one item per index
					int[] inflatedAttributeArray = new int[indexArray.length*4];
					//count = 0;
					for(int i = 0; i < ls.getNumEdges(); i++){
						IntArray dA = (IntArray)attribs.get(i);
						//now for each point of the edge
						for(int k = 0; k < 2; k++){
							//for each component of the attribute vector
							inflatedAttributeArray[(2*i+k)*4+0] = dA.getValueAt(0);
							if(dA.size() > 1)
								inflatedAttributeArray[(2*i+k)*4+1] = dA.getValueAt(1);
							else
								inflatedAttributeArray[(2*i+k)*4+1] = 0;
							if(dA.size() > 2)
								inflatedAttributeArray[(2*i+k)*4+2] = dA.getValueAt(2);
							else
								inflatedAttributeArray[(2*i+k)*4+2] = 0;
							if(dA.size() > 3)
								inflatedAttributeArray[(2*i+k)*4+3] = dA.getValueAt(3);
							else
								inflatedAttributeArray[(2*i+k)*4+3] = 1;
						}
					}
					lineVbos.put("edge_"+shaderName, new GLVBOInt(gl, inflatedAttributeArray, "edge_"+a.getName()));
//					System.out.println("creating " + "edge_"+a.getName());
				}else{
					System.out.println("LSE1: not knowing what to do with " + attribs.getStorageModel().toString() + ", " + a.getName());
				}
			}
			
			
			//then vertex attributes
			aS = ls.getVertexAttributes().storedAttributes();
			for(Attribute a : aS){
				String shaderName = "";
				for(String s : a.getName().split(" ")){
					shaderName = shaderName + s;
				}
				//System.out.println("vertex attribute: "+a.getName());
				DataList attribs = ls.getVertexAttributes(a);
				
				if(isDoubleArray(attribs.getStorageModel())){
					double[] inflatedAttributeArray = new double[indexArray.length];
					DoubleArray dA = (DoubleArray)attribs;
					for(int i = 0; i < indexArray.length; i++){
						inflatedAttributeArray[i] = dA.getValueAt(indexArray[i]);
					}
					lineVbos.put("vertex_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "vertex_"+a.getName(), 1));
				}else if(isDoubleArrayArray(attribs.getStorageModel())){
					//the array containing one item per index
					double[] inflatedAttributeArray = new double[indexArray.length*4];
					//count = 0;
					//for each index in the indexArray
					for(int i = 0; i < indexArray.length; i++){
						//we retrieve the vertex attribute
						int j = indexArray[i];
						DoubleArray dA = (DoubleArray)attribs.get(j);
						
						inflatedAttributeArray[4*i+0] = dA.getValueAt(0);
						if(dA.size() > 1)
							inflatedAttributeArray[4*i+1] = dA.getValueAt(1);
						else
							inflatedAttributeArray[4*i+1] = 0;
						if(dA.size() > 2)
							inflatedAttributeArray[4*i+2] = dA.getValueAt(2);
						else
							inflatedAttributeArray[4*i+2] = 0;
						if(dA.size() > 3)
							inflatedAttributeArray[4*i+3] = dA.getValueAt(3);
						else
							inflatedAttributeArray[4*i+3] = 1;
					}
					lineVbos.put("vertex_"+shaderName, new GLVBOFloat(gl, Rn.convertDoubleToFloatArray(inflatedAttributeArray), "vertex_"+a.getName()));
					
//					System.out.println("creating " + "vertex_"+a.getName());
				
				}else if(isIntArray(attribs.getStorageModel())){
					//the array containing one item per index
					int[] inflatedAttributeArray = new int[indexArray.length*4];
					//count = 0;
					//for each index in the indexArray
					for(int i = 0; i < indexArray.length; i++){
						//we retrieve the vertex attribute
						int j = indexArray[i];
						IntArray dA = (IntArray)attribs.get(j);
						
						inflatedAttributeArray[4*i+0] = dA.getValueAt(0);
						if(dA.size() > 1)
							inflatedAttributeArray[4*i+1] = dA.getValueAt(1);
						else
							inflatedAttributeArray[4*i+1] = 0;
						if(dA.size() > 2)
							inflatedAttributeArray[4*i+2] = dA.getValueAt(2);
						else
							inflatedAttributeArray[4*i+2] = 0;
						if(dA.size() > 3)
							inflatedAttributeArray[4*i+3] = dA.getValueAt(3);
						else
							inflatedAttributeArray[4*i+3] = 1;
					}
					lineVbos.put("vertex_"+shaderName, new GLVBOInt(gl, inflatedAttributeArray, "vertex_"+a.getName()));
//					System.out.println("creating " + "vertex_"+a.getName());
				
				}else{
					System.out.println("LSE2: not knowing what to do with " + attribs.getStorageModel().toString() + ", " + a.getName());
				}
				//System.out.println("face attribute names: " + a.getName());
			}
			//dataUpToDate = true;
		//}
		
	}
	//TODO write line shader
}
