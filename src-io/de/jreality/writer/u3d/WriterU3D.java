package de.jreality.writer.u3d;

import static de.jreality.math.MatrixBuilder.euclidean;
import static de.jreality.scene.data.Attribute.COLORS;
import static de.jreality.scene.data.Attribute.COORDINATES;
import static de.jreality.scene.data.Attribute.INDICES;
import static de.jreality.scene.data.Attribute.NORMALS;
import static de.jreality.scene.data.Attribute.TEXTURE_COORDINATES;
import static de.jreality.scene.data.AttributeEntityUtility.createAttributeEntity;
import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR;
import static de.jreality.shader.CommonAttributes.AMBIENT_COLOR_DEFAULT;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR_DEFAULT;
import static de.jreality.shader.CommonAttributes.LIGHTING_ENABLED;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT;
import static de.jreality.shader.CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR;
import static de.jreality.shader.CommonAttributes.SPECULAR_COLOR_DEFAULT;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT;
import static de.jreality.shader.CommonAttributes.SPECULAR_EXPONENT_DEFAULT;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_DEFAULT;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_COLORS_ENABLED;
import static de.jreality.shader.Texture2D.GL_REPEAT;
import static de.jreality.writer.u3d.U3DConstants.CHARACTER_ENCODING;
import static de.jreality.writer.u3d.U3DConstants.EXPORTER_VERSION_MAJOR;
import static de.jreality.writer.u3d.U3DConstants.EXPORTER_VERSION_MINOR;
import static de.jreality.writer.u3d.U3DConstants.PROFILE_OPTIONS;
import static de.jreality.writer.u3d.U3DConstants.TYPE_FILE_HEADER;
import static de.jreality.writer.u3d.U3DConstants.TYPE_GROUP_NODE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_LIGHT_NODE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_LIGHT_RESOURCE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_LINE_SET_CONTINUATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_LIT_TEXTURE_SHADER;
import static de.jreality.writer.u3d.U3DConstants.TYPE_MATERIAL_RESOURCE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_MESH_GENERATOR_CONTINUATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_MESH_GENERATOR_DECLARATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_MODEL_NODE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_MOFIFIER_CHAIN;
import static de.jreality.writer.u3d.U3DConstants.TYPE_POINT_SET_CONTINUATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_POINT_SET_DECLARATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_SHADING_MODIFIER;
import static de.jreality.writer.u3d.U3DConstants.TYPE_TEXTURE_RESOURCE_CONTINUATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_TEXTURE_RESOURCE_DECLARATION;
import static de.jreality.writer.u3d.U3DConstants.TYPE_VIEW_NODE;
import static de.jreality.writer.u3d.U3DConstants.TYPE_VIEW_RESOURCE;
import static de.jreality.writer.u3d.U3DConstants.fLimit;
import static de.jreality.writer.u3d.U3DConstants.uACContextBaseShadingID;
import static de.jreality.writer.u3d.U3DConstants.uACContextNumLocalNormals;
import static de.jreality.writer.u3d.U3DConstants.uACContextNumNewFaces;
import static de.jreality.writer.u3d.U3DConstants.uACContextPositionDiffMagX;
import static de.jreality.writer.u3d.U3DConstants.uACContextPositionDiffMagY;
import static de.jreality.writer.u3d.U3DConstants.uACContextPositionDiffMagZ;
import static de.jreality.writer.u3d.U3DConstants.uACContextPositionDiffSigns;
import static de.jreality.writer.u3d.U3DConstants.uACStaticFull;
import static java.awt.Color.GRAY;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.jreality.io.JrScene;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SpotLight;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.writer.SceneWriter;
import de.jreality.writer.u3d.u3dencoding.BitStreamWrite;
import de.jreality.writer.u3d.u3dencoding.DataBlock;


public class WriterU3D implements SceneWriter { 

	private SceneGraphComponent
		rootNode = null;
	private Collection<SceneGraphComponent>
		nodes = null;
	private HashMap<SceneGraphComponent, Boolean>
		visibilityMap = null;
	private Collection<Geometry>
		geometries = null;
	private Collection<SceneGraphComponent>
		viewNodes = null;
	private Collection<Camera>
		cameras = null;
	private HashMap<Camera, String>
		cameraNameMap = null;
	private Collection<SceneGraphComponent>
		lightNodes = null;
	private Collection<Light>
		lights = null;
	private HashMap<Light, String>
		lightNameMap = new HashMap<Light, String>();
	private HashMap<SceneGraphComponent, Collection<SceneGraphComponent>>
		parentMap = null;
	private HashMap<SceneGraphComponent, String>
		nodeNameMap = null;
	private HashMap<Geometry, String>
		geometryNameMap = null;
//	private HashMap<Geometry, Rectangle3D>
//		geometryBoundingBox = null;
	private HashMap<Geometry, Geometry>
		preparedGeometryMap = null;
	private Collection<EffectiveAppearance>
		appearances = null;
	private HashMap<SceneGraphComponent, EffectiveAppearance>
		appearanceMap = null;
	private HashMap<EffectiveAppearance, String>
		appearanceNameMap = null;
	private Collection<U3DTexture>
		textures = null;
	private HashMap<EffectiveAppearance, U3DTexture>
		textureMap = null;
	private HashMap<U3DTexture, String>
		textureNameMap = null;
	private HashMap<U3DTexture, byte[]>
		texturePNGData = null;
	private HashMap<EffectiveAppearance, U3DTexture>
		sphereMapsMap = null;	

		
	protected DataBlock getLightResource(Light l) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(lightNameMap.get(l));
		// attributes
		w.WriteU32(0x00000001); // enabled
		// light type
		if (l instanceof DirectionalLight) {
			w.WriteU8((short)0x01);
		} else if (l instanceof SpotLight) {
			w.WriteU8((short)0x03);
		} else { // point light
			w.WriteU8((short)0x02);
		} 
		
		w.WriteColor(l.getColor());
		w.WriteF32(1.0f); // reserved
		
		// attenution
		if (l instanceof PointLight) { // point and spot light
			PointLight attenLight = (PointLight) l;
			w.WriteF32((float)attenLight.getFalloffA0());
			w.WriteF32((float)attenLight.getFalloffA1());
			w.WriteF32((float)attenLight.getFalloffA2());
		} else {
			w.WriteF32(1.0f);
			w.WriteF32(1.0f);
			w.WriteF32(1.0f);
		}
		// spot angle
		if (l instanceof SpotLight) {
			SpotLight spotLight = (SpotLight) l;
			w.WriteF32((float)spotLight.getConeAngle());
		} else {
			w.WriteF32(45.0f);
		}
		// intensity
		w.WriteF32((float)l.getIntensity());
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_LIGHT_RESOURCE);
		return b;		
	}
		
		
	protected DataBlock getLightNodeDeclaration(SceneGraphComponent c) {
		Light light = c.getLight();
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c) + ".light");
//		WriteParentNodeData(c, w); 
		w.WriteU32(1);
		w.WriteString(nodeNameMap.get(c));
		WriteMatrix(euclidean().getArray(), w);
		w.WriteString(lightNameMap.get(light));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_LIGHT_NODE);
		return b;		
	}
		
		
		
	protected DataBlock getViewResource(Camera c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(cameraNameMap.get(c));
		// pass count
		w.WriteU32(1);
		w.WriteString(nodeNameMap.get(rootNode));
		// fog enabled
		w.WriteU32(0x00000000); // disabled
		// fog mode
		w.WriteU32(0x00000000); // linear
		// fog color
		w.WriteColor(GRAY); 
		w.WriteF32(1.0f); // alpha
		// for clipping
		w.WriteF32((float)c.getNear());
		w.WriteF32((float)c.getFar());
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_VIEW_RESOURCE);
		return b;		
	}
		
		
	protected DataBlock getViewNodeDeclaration(SceneGraphComponent c) {
		Camera cam = c.getCamera();
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c) + ".camera");
//		WriteParentNodeData(c, w);
		w.WriteU32(1);
		w.WriteString(nodeNameMap.get(c));
		WriteMatrix(euclidean().getArray(), w);
		
		w.WriteString(cameraNameMap.get(cam));
		// view node attributes
		w.WriteU32(0x00000000); // three point projection
		// view clipping
		w.WriteF32((float)cam.getNear());
		w.WriteF32((float)cam.getFar());
		// field of view
		w.WriteF32((float)cam.getFieldOfView());
		
		Rectangle2D viewport = cam.getViewPort();
		if (viewport != null) {
			w.WriteF32((float)viewport.getWidth());
			w.WriteF32((float)viewport.getHeight());
			w.WriteF32((float)viewport.getX());
			w.WriteF32((float)viewport.getY());
		} else {
			w.WriteF32(100.0f);
			w.WriteF32(100.0f);
			w.WriteF32(0);
			w.WriteF32(0);
		}
		
		// backdrop count
		w.WriteU32(0);
		// overlay count
		w.WriteU32(0);
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_VIEW_NODE);
		return b;		
	}
		

		
	protected DataBlock getPointSetContinuation(PointSet g){
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		w.WriteU32(0); // chain index 
		
		DoubleArrayArray vData = (DoubleArrayArray)g.getVertexAttributes(COORDINATES);
		double[][] vertices = null;
		if (vData != null)
			vertices = vData.toDoubleArrayArray(null);
		else
			vertices = new double[0][];
		DoubleArrayArray nData = (DoubleArrayArray)g.getVertexAttributes(NORMALS);
		double[][] normals = null;
		if (nData != null)
			normals = nData.toDoubleArrayArray(null);
		// point resolution range
		w.WriteU32(0); // start
		w.WriteU32(vertices.length);
		
		float m_fQuantPosition = (float)pow(2.0,18.0);
		m_fQuantPosition = (float)max(m_fQuantPosition, fLimit);
		float m_fQuantNormal = (float) pow(2.0,14.0);
		
		// points
		for (int currPosInd = 0; currPosInd < vertices.length; currPosInd++) {
			int splitPosInd = currPosInd-1;
			// split position
			if (splitPosInd == -1) {
				w.WriteCompressedU32(uACStaticFull+1,0);
			} else {
				w.WriteCompressedU32(uACStaticFull+currPosInd, splitPosInd);
			}
			// new position info
			double[] vPosition = vertices[currPosInd];
			double[] vPredictedPosition = new double[3];
			if (splitPosInd >= 0)
				vPredictedPosition = vertices[splitPosInd];
			double[] vPositionDifference = Rn.subtract(null, vPosition, vPredictedPosition);
			double[] v = vPositionDifference;
			
			short u8Signs = (short)((v[0] < 0.0 ? 1 : 0) | ((v[1] < 0.0 ? 1 : 0) << 1) | ((v[2] < 0.0 ? 1 : 0) << 2));
			long udX = (long)(0.5 + m_fQuantPosition * abs(v[0]));
			long udY = (long)(0.5 + m_fQuantPosition * abs(v[1]));
			long udZ = (long)(0.5 + m_fQuantPosition * abs(v[2]));
//			System.out.println(Arrays.toString(vPosition));
//			System.out.println(u8Signs + " - " + udX + " - " + udY + " - " + udZ);
			w.WriteCompressedU8(uACContextPositionDiffSigns, u8Signs);
			w.WriteCompressedU32(uACContextPositionDiffMagX, udX);
			w.WriteCompressedU32(uACContextPositionDiffMagY, udY);
			w.WriteCompressedU32(uACContextPositionDiffMagZ, udZ);
			
			if (nData != null) {
				// new normal count
				w.WriteCompressedU32(uACContextNumLocalNormals, 1);
				double[] nPosition = normals[currPosInd];
				double[] nPredictedPosition = new double[3];
				if ( splitPosInd>=0 )
					nPredictedPosition = normals[splitPosInd];
				double[] nPositionDifference = Rn.subtract(null, nPosition, nPredictedPosition);
				double[] n = nPositionDifference;
				// new normal info
				u8Signs = (short)((n[0] < 0.0 ? 1 : 0) | ((n[1] < 0.0 ? 1 : 0) << 1) | ((n[2] < 0.0 ? 1 : 0) << 2));
				udX = (long)(0.5f + m_fQuantNormal * abs(n[0]));
				udY = (long)(0.5f + m_fQuantNormal * abs(n[1]));
				udZ = (long)(0.5f + m_fQuantNormal * abs(n[2]));
				w.WriteCompressedU8(uACContextPositionDiffSigns,u8Signs);
				w.WriteCompressedU32(uACContextPositionDiffMagX, udX);
				w.WriteCompressedU32(uACContextPositionDiffMagY, udY);
				w.WriteCompressedU32(uACContextPositionDiffMagZ, udZ);
			} else {
				// new normal count
				w.WriteCompressedU32(uACContextNumLocalNormals, 0);
			}
			// no points at all
			w.WriteCompressedU32(uACContextNumNewFaces, 0);
		}
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_POINT_SET_CONTINUATION);
		return b;
	}
	
	
	
	protected DataBlock getLineSetContinuation(IndexedLineSet g){
		System.out.println("writing line set continuation");
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_LINE_SET_CONTINUATION);
		return b;
	}
	

	protected DataBlock getCLODMeshContinuation(IndexedFaceSet g){
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		int vertCount = g.getNumPoints();
		int faceCount = g.getNumFaces();
		
		DoubleArrayArray tvData = (DoubleArrayArray)g.getVertexAttributes(TEXTURE_COORDINATES);
		double[][] tVerts = null;
		int tvertCount = 0;
		if (tvData != null) {
			tvertCount = tvData.size();
			tVerts = tvData.toDoubleArrayArray(null);
		}
		DoubleArrayArray nData = (DoubleArrayArray)g.getVertexAttributes(NORMALS);
		double[][] normals = null;
		int nCount = 0;
		if (nData != null) {
			nCount = nData.size();
			normals = nData.toDoubleArrayArray(null);
		}
		// base mesh description
		w.WriteU32(0); // chain index 
		w.WriteU32(faceCount);
		w.WriteU32(vertCount);
		w.WriteU32(nCount); // no normals
		w.WriteU32(0); // no per vertex diffuse colors
		w.WriteU32(0); // no per vertex specular colors
		w.WriteU32(tvertCount == 0 ? 1 : tvertCount); // no texture coordinates
		DoubleArrayArray vData = (DoubleArrayArray)g.getVertexAttributes(COORDINATES);
		double[][] vertices = vData.toDoubleArrayArray(null); 
		IntArrayArray fData = (IntArrayArray)g.getFaceAttributes(INDICES);
		int[][] faces = fData.toIntArrayArray(null);
		
		// vertices
		for (int i = 0; i < vertCount; i++) {
			double[] v = vertices[i];
			if (v.length > 3)
				Pn.dehomogenize(v, v);
			w.WriteF32((float) v[0]);
			w.WriteF32((float) v[1]);
			w.WriteF32((float) v[2]);
		}
		// normals
		if (nCount != 0) {
			for (int i = 0; i < normals.length; i++) {
				double[] n = normals[i];
				w.WriteF32((float) n[0]);
				w.WriteF32((float) n[1]);
				w.WriteF32((float) n[2]);
			}
		}
		// texture coordinates
		if (tvertCount != 0) {
			for (int i = 0; i < tVerts.length; i++) {
				double[] v = tVerts[i];
				w.WriteF32((float) v[0]);
				w.WriteF32((float) v[1]);
				w.WriteF32(0f);
				w.WriteF32(0f);
			}
		} else {
			w.WriteF32(0f);
			w.WriteF32(0f);
			w.WriteF32(0f);
			w.WriteF32(0f);		
		}
		// faces
		for (int i = 0; i < faceCount; i++) {
			int[] f = faces[i];
			w.WriteCompressedU32(uACContextBaseShadingID, 0);
			for (int j = 0; j < 3; j++) {
				w.WriteCompressedU32(uACStaticFull + vertCount, f[j]);
				if (nData != null)
					w.WriteCompressedU32(uACStaticFull + nCount, f[j]);
				w.WriteCompressedU32(uACStaticFull + (tvertCount == 0 ? 1 : tvertCount), tvertCount == 0 ? 0 : f[j]);
			}
		}
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MESH_GENERATOR_CONTINUATION);
		return b;
	}
	
	
	protected List<DataBlock> getContinuations(Geometry g) {
		LinkedList<DataBlock> r = new LinkedList<DataBlock>();
		DataBlock geomCont = null;
		if (g instanceof IndexedFaceSet)
			geomCont = getCLODMeshContinuation((IndexedFaceSet)g);
		else if (g instanceof IndexedLineSet)
			geomCont = getLineSetContinuation((IndexedLineSet)g);
		else if (g instanceof PointSet)
			geomCont = getPointSetContinuation((PointSet)g);
		if (geomCont != null)
			r.add(geomCont);
		return r;
	}
	
	
	protected DataBlock getTextureContinuation(U3DTexture tex) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(textureNameMap.get(tex));
		w.WriteU32(0); // only one continuation per image
		byte[] pngData = texturePNGData.get(tex);
		for (int i = 0; i < pngData.length; i++) {
			short unsignedByte = 0x00FF;
			unsignedByte &= pngData[i];
			w.WriteU8(unsignedByte);
		}
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_TEXTURE_RESOURCE_CONTINUATION);
		return b;
	}
	
	
	protected List<DataBlock> getContinuations(U3DTexture tex) {
		LinkedList<DataBlock> r = new LinkedList<DataBlock>();
		r.add(getTextureContinuation(tex));
		return r;
	}
	
	
	protected List<DataBlock> getContinuations(SceneGraphComponent c) {
		LinkedList<DataBlock> r = new LinkedList<DataBlock>();
		
		return r;
	}
	
	
	protected DataBlock getPointSetDeclaration(PointSet g) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		w.WriteU32(0);
		
		// reserved
		w.WriteU32(0);
		// number of points
		w.WriteU32(0);//g.getNumPoints());
		w.WriteU32(g.getNumPoints());
		
		// vertex normals
		DoubleArrayArray nData = (DoubleArrayArray)g.getVertexAttributes(NORMALS);
		if (nData != null) {
			w.WriteU32(nData.size());
		} else {
			w.WriteU32(0);
		}
		// diffuse colors
		DoubleArrayArray vColors = (DoubleArrayArray)g.getVertexAttributes(COLORS);
		if (vColors != null) {
			w.WriteU32(vColors.size());
		} else {
			w.WriteU32(0);
		}
		//specular colors
		w.WriteU32(0);
		
		// texture coordinates
		w.WriteU32(0);
		
		// shading count
		w.WriteU32(1);
		// standard shading
		w.WriteU32(0x00000000);
		w.WriteU32(1);
		w.WriteU32(2);
		w.WriteU32(0);
		
		// not relevant for point sets
		w.WriteU32(1000);
		w.WriteU32(1000);
		w.WriteU32(1000);
		
		// Resource Inverse Quantization
		float m_fQuantPosition = (float)pow(2.0,18.0);
		m_fQuantPosition = (float)max(m_fQuantPosition, fLimit);
		float m_fQuantNormal = (float) pow(2.0,14.0);
		float m_fQuantTexCoord = (float) pow(2.0,14.0);
		float m_fQuantDiffuseColor  = (float) pow(2.0,14.0);
		float m_fQuantSpecularColor  = (float) pow(2.0,14.0);
		
		w.WriteF32(1.0f / m_fQuantPosition);
		w.WriteF32(1.0f / m_fQuantNormal);
		w.WriteF32(1.0f / m_fQuantTexCoord);
		w.WriteF32(1.0f / m_fQuantDiffuseColor);
		w.WriteF32(1.0f / m_fQuantSpecularColor);
	
		// Resource parameters
		for (int i = 0; i < 3; i++)
			w.WriteF32(1.0f);
		
		// no bones
		w.WriteU32(0);
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_POINT_SET_DECLARATION);
		return b;
	}
	
	protected DataBlock getLineSetDeclaration(IndexedLineSet g) {
		System.out.println("writing line set declaration");
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_LINE_SET_CONTINUATION);
		return b;
	}
	
	
	protected void WriteResourceDescriptionDummy(BitStreamWrite w) {
		// Resource Description Quality Factors
		w.WriteU32(1000);
		w.WriteU32(1000);
		w.WriteU32(1000);
		
		// Resource Inverse Quantization
		for (int i = 0; i < 5; i++)
			w.WriteF32(1.0f);
	
		// Resource parameters
		for (int i = 0; i < 3; i++)
			w.WriteF32(1.0f);
	}
	
	
	protected DataBlock getCLODMeshGeneratorDeclaration(IndexedFaceSet g){
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		w.WriteU32(0); // chain index 
		
		DoubleArrayArray tvData = (DoubleArrayArray)g.getVertexAttributes(TEXTURE_COORDINATES);
		DoubleArrayArray nData = (DoubleArrayArray)g.getVertexAttributes(NORMALS);
		// Max Mesh Description
		if (nData != null) {
			w.WriteU32(0x00000000); // vertex normals
		} else {
			w.WriteU32(0x00000001); // no vertex normals
		}
		w.WriteU32(g.getNumFaces());
		w.WriteU32(g.getNumPoints());
		w.WriteU32(nData == null ? 0 : nData.size()); // no normals
		w.WriteU32(0); // no per vertex diffuse colors
		w.WriteU32(0); // no per vertex specular colors
		w.WriteU32(tvData == null ? 1 : tvData.size()); // one default coordinate
		
		// shading count
		w.WriteU32(1); // standard shading description
		// Standard shading
		w.WriteU32(0x00000000);
		if (tvData != null)
			w.WriteU32(1);
		else
			w.WriteU32(0);
		w.WriteU32(2);
		w.WriteU32(0);
		
		// CLOD Description
		w.WriteU32(g.getNumPoints());
		w.WriteU32(g.getNumPoints());
		
		// resource description
		WriteResourceDescriptionDummy(w);
		
		w.WriteU32(0); // no bones
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MESH_GENERATOR_DECLARATION);
		return b;
	}
	
	
	
	protected void WriteMatrix(double[] T, BitStreamWrite w) {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				w.WriteF32((float)T[4*j + i]);
	}
	
	
	protected void WriteTransform(SceneGraphComponent c, BitStreamWrite w) {
		double[] T = Rn.setIdentityMatrix(new double[16]);
		if (c.getTransformation() != null) {
			c.getTransformation().getMatrix(T);
		}
		WriteMatrix(T, w);
	}
	
	
	protected void WriteParentNodeData(SceneGraphComponent c, BitStreamWrite w){
		Collection<SceneGraphComponent> parents = parentMap.get(c);
		if (parents.size() == 0) { // root has default node as parent
			w.WriteU32(1);
			w.WriteString("");
			WriteMatrix(euclidean(c).rotate(PI, 0, 0, 1).rotate(PI / 2, 1, 0, 0).getArray(), w);
		} else {
			w.WriteU32(parents.size());
			for (SceneGraphComponent p : parents) {
				w.WriteString(nodeNameMap.get(p));
				WriteTransform(c, w);
			}
		}
	}
	
	
	
	protected DataBlock getGroupNodeDeclaration(SceneGraphComponent c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c));
		WriteParentNodeData(c, w); 
		DataBlock b = null;
		b = w.GetDataBlock();
		b.setBlockType(TYPE_GROUP_NODE);
		return b;
	}
	
	
	
	protected DataBlock getModelNodeDeclaration(SceneGraphComponent c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c));
		WriteParentNodeData(c, w); 

		Geometry g = getPreparedGeometry(c);
		w.WriteString(geometryNameMap.get(g));
		if (!visibilityMap.get(c))
			w.WriteU32(0x00000000); // invisible
		else
			w.WriteU32(0x00000003); // front and back
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MODEL_NODE);
		return b;
	}
	
	
	protected DataBlock getShadingModifier(SceneGraphComponent c, long chainIndex) {
		BitStreamWrite w = new BitStreamWrite();
		EffectiveAppearance ea = appearanceMap.get(c);
		w.WriteString(nodeNameMap.get(c));
		w.WriteU32(chainIndex);
		Geometry p = getPreparedGeometry(c);
		if (p instanceof IndexedFaceSet) {
			w.WriteU32(0x00000001);
		} else if (p instanceof IndexedLineSet) {
			w.WriteU32(0x00000002);
		} else if (p instanceof PointSet) {
			w.WriteU32(0x00000004);
		}
		w.WriteU32(1);
		w.WriteU32(1);
		w.WriteString(appearanceNameMap.get(ea));

		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_SHADING_MODIFIER);
		return b;
	}
	
	
	protected List<DataBlock> getNodeModifiers(SceneGraphComponent c) {
		LinkedList<DataBlock> r = new LinkedList<DataBlock>();
		if (getPreparedGeometry(c) != null) { // a model node
			r.add(getModelNodeDeclaration(c));
			r.add(getShadingModifier(c, 1));
		} 
		else { // a group node
			r.add(getGroupNodeDeclaration(c));
		}
		return r;
	}
	
	
	protected DataBlock getMaterialResource(EffectiveAppearance a) {
		BitStreamWrite w = new BitStreamWrite();
		// name
		w.WriteString(appearanceNameMap.get(a));
		
		// material attributes (ambient diffuse specular opacity)
		long attribs = 0x00000001 | 0x00000002 | 0x00000004 | 0x00000020;
		w.WriteU32(attribs);
		
		U3DTexture envMap = sphereMapsMap.get(a);
		// ambient
		Color ambient = (Color)a.getAttribute(POLYGON_SHADER + "." + AMBIENT_COLOR, AMBIENT_COLOR_DEFAULT);
		float ambiCoeff = (float)a.getAttribute(POLYGON_SHADER + "." + AMBIENT_COEFFICIENT, AMBIENT_COEFFICIENT_DEFAULT);
		float[] amb = ambient.getColorComponents(null);
		if (envMap != null) ambiCoeff *= (1 - envMap.getIntesity());
		ambient = new Color(amb[0] * ambiCoeff, amb[1] * ambiCoeff, amb[2] * ambiCoeff);
		w.WriteColor(ambient);
		
		// diffuse
		Color diffuse = (Color)a.getAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, DIFFUSE_COLOR_DEFAULT);
		float diffCoeff = (float)a.getAttribute(POLYGON_SHADER + "." + DIFFUSE_COEFFICIENT, DIFFUSE_COEFFICIENT_DEFAULT);
		if (envMap != null) diffCoeff *= (1 - envMap.getIntesity());
		float[] dif = diffuse.getColorComponents(null);
		diffuse = new Color(dif[0] * diffCoeff, dif[1] * diffCoeff, dif[2] * diffCoeff);
		w.WriteColor(diffuse);
		
		// specular
		Color specular = (Color)a.getAttribute(POLYGON_SHADER + "." + SPECULAR_COLOR, SPECULAR_COLOR_DEFAULT);
		float specCoeff = (float)a.getAttribute(POLYGON_SHADER + "." + SPECULAR_COEFFICIENT, SPECULAR_COEFFICIENT_DEFAULT);
		if (envMap != null) specCoeff *= (1 - envMap.getIntesity());
		float[] s = specular.getColorComponents(null);
		specular = new Color(s[0] * specCoeff, s[1] * specCoeff, s[2] * specCoeff);
		w.WriteColor(specular);
		
		// emission
		w.WriteColor(ambient);
		
		// reflectivity
		double reflectivity = a.getAttribute(POLYGON_SHADER + "." + SPECULAR_EXPONENT, SPECULAR_EXPONENT_DEFAULT) / 128.0;
		w.WriteF32((float)reflectivity);
		
		// opacity
		boolean alphaEnabled = a.getAttribute(POLYGON_SHADER + "." + TRANSPARENCY_ENABLED, false);
		double opacity = 1.0 - a.getAttribute(POLYGON_SHADER + "." + TRANSPARENCY, TRANSPARENCY_DEFAULT);
		if (!alphaEnabled)
			opacity = 1.0;
		w.WriteF32((float)opacity);
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MATERIAL_RESOURCE);
		return b;		
	}
	
	
	protected DataBlock getLitTextureShader(EffectiveAppearance a) {
		BitStreamWrite w = new BitStreamWrite();
		// name
		w.WriteString(appearanceNameMap.get(a));
		
		// attributes
		boolean lightingEnabled = a.getAttribute(POLYGON_SHADER + "." + LIGHTING_ENABLED, true);
		boolean alphaEnabled = a.getAttribute(POLYGON_SHADER + "." + TRANSPARENCY_ENABLED, false);
		boolean useVertexColors = a.getAttribute(POLYGON_SHADER + "." + VERTEX_COLORS_ENABLED, false);
		long attributes = 0;
		if (lightingEnabled) attributes |= 0x00000001;
		if (alphaEnabled) attributes |= 0x00000002;
		if (useVertexColors) attributes |= 0x00000004;
		w.WriteU32(attributes);
		
		// alpha test reference
		w.WriteF32(0.0f);
		
		// alpha test function
		w.WriteU32(0x00000617); // accept all pixels
		
		// color blend function
		w.WriteU32(0x00000606); // FB_ALPHA_BLEND
		
		// render pass flags
		w.WriteU32(0x00000001);
		
		// shader channels and alpha texture channels
		U3DTexture tex = textureMap.get(a);
		U3DTexture envMap = sphereMapsMap.get(a);
		long shaderChannels = 0;
		long alphatexChannels = 0;
		float texIntensity = 1.0f;
		float envIntensity = 1.0f;
		if (tex != null) {
			shaderChannels |= 0x00000001;
			alphatexChannels |= 0x00000001;
			texIntensity *= tex.getIntesity();
		}
		if (envMap != null) {
			shaderChannels |= 0x00000002;
			alphatexChannels |= 0x00000001;
			envIntensity *= envMap.getIntesity();
			texIntensity *= (1 - envIntensity);
		}
		w.WriteU32(shaderChannels);
		w.WriteU32(alphatexChannels);
		
		// material name (same name as lit texture shader)
		w.WriteString(appearanceNameMap.get(a));
		
		// texture
		if (tex != null) {
			Texture2D texInfo = (Texture2D) createAttributeEntity(Texture2D.class, POLYGON_SHADER + "." + TEXTURE_2D, a);
			w.WriteString(textureNameMap.get(tex));
			w.WriteF32(texIntensity); // intensity
			// blend function
			switch (texInfo.getApplyMode()) {
			case Texture2D.GL_MODULATE:
				w.WriteU8((short)0); break;
			case Texture2D.GL_ADD:
				w.WriteU8((short)1); break;
			case Texture2D.GL_REPLACE:
				w.WriteU8((short)2); break;
			case Texture2D.GL_BLEND:
				w.WriteU8((short)3); break;
			default:
				w.WriteU8((short)2); break;
			}
			// blend source
			w.WriteU8((short)0); // alpha combine
			// blend constant dummy
			w.WriteF32(1.0f);
			// texture mode
			w.WriteU8((short)0x00);
			// texture transform matrix
			Matrix T = texInfo.getTextureMatrix();
			WriteMatrix(T.getArray(), w);
			// texture wrap transform matrix element
			WriteMatrix(euclidean().getArray(), w); // not implemented
			short repeat = 0;
			if (texInfo.getRepeatS() == GL_REPEAT)
				repeat |= 0x01;
			if (texInfo.getRepeatT() == GL_REPEAT)
				repeat |= 0x02;
			w.WriteU8(repeat);
		}
		// environment texture
		if (envMap != null) {
			w.WriteString(textureNameMap.get(envMap));
			w.WriteF32(envIntensity); // intensity
			// blend function
//			switch (texInfo.getApplyMode()) {
//			case Texture2D.GL_MODULATE:
//				w.WriteU8((short)0); break;
//			case Texture2D.GL_ADD:
//				w.WriteU8((short)1);// break;
//			case Texture2D.GL_REPLACE:
				w.WriteU8((short)2);// break;
//			case Texture2D.GL_BLEND:
//				w.WriteU8((short)3); //break;
//			default:
//				w.WriteU8((short)2); break;
//			}
			// blend source
			w.WriteU8((short)0); // alpha combine
			// blend constant dummy
			w.WriteF32(1.0f);
			// texture mode
			w.WriteU8((short)0x04); // spherical reflection
			// texture transform matrix
			WriteMatrix(euclidean().getArray(), w); // identity
			// texture wrap transform matrix element
			WriteMatrix(euclidean().getArray(), w); // identity
			short repeat = 0x01 | 0x02;
			w.WriteU8(repeat);	
		}
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_LIT_TEXTURE_SHADER);
		return b;
	}
	
	
	protected DataBlock getTextureResourceDeclaration(U3DTexture tex) {
		ImageData img = tex.getImage();
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(textureNameMap.get(tex));
		w.WriteU32(img.getHeight());
		w.WriteU32(img.getWidth());
		w.WriteU8((short)0x0F);
		
		w.WriteU32(1); // one big image block
		w.WriteU8((short)0x02); // PNG compression
		w.WriteU8((short)(0x01 | 0x02 | 0x04 | 0x08)); // rgba channels
		w.WriteU16(0x0000); // inlined image data
		byte[] pngData = texturePNGData.get(tex);
		w.WriteU32(pngData.length);

		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_TEXTURE_RESOURCE_DECLARATION);
		return b;
	}
	
	
	protected DataBlock getModifierChain(U3DTexture tex) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(textureNameMap.get(tex));
		w.WriteU32(0x00000002); // texture resource modifier chain
		w.WriteU32(0x00000000); // no attributes
		w.AlignTo4Byte(); // modifier chain padding
		
		w.WriteU32(1); /// modifier count
		w.WriteDataBlock(getTextureResourceDeclaration(tex));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MOFIFIER_CHAIN);
		return b;
	}
	
	
	
	protected DataBlock getModifierChain(Geometry g) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(geometryNameMap.get(g));
		w.WriteU32(0x00000001); // model resource modifier chain
		w.WriteU32(0x00000000); // no attributes
		w.AlignTo4Byte(); // modifier chain padding
		
		w.WriteU32(1); /// modifier count
		DataBlock geomDecl = null;
		if (g instanceof IndexedFaceSet)
			geomDecl = getCLODMeshGeneratorDeclaration((IndexedFaceSet)g);
		else if (g instanceof IndexedLineSet)
			geomDecl = getLineSetDeclaration((IndexedLineSet)g);
		else if (g instanceof PointSet)
			geomDecl = getPointSetDeclaration((PointSet)g);
		if (geomDecl != null) 
			w.WriteDataBlock(geomDecl);
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MOFIFIER_CHAIN);
		return b;
	}
	
	
	protected DataBlock getLightNodeModifierChain(SceneGraphComponent c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c));
		w.WriteU32(0x00000000); // node modifier chain
		w.WriteU32(0x00000000); // no attributes
		w.AlignTo4Byte(); // modifier chain padding
		
		w.WriteU32(1); // modifier count (only hierarchy)
		w.WriteDataBlock(getLightNodeDeclaration(c));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MOFIFIER_CHAIN);
		return b;
	}
	
	
	protected DataBlock getViewNodeModifierChain(SceneGraphComponent c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c));
		w.WriteU32(0x00000000); // node modifier chain
		w.WriteU32(0x00000000); // no attributes
		w.AlignTo4Byte(); // modifier chain padding
		
		w.WriteU32(1); // modifier count (only hierarchy)
		w.WriteDataBlock(getViewNodeDeclaration(c));
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MOFIFIER_CHAIN);
		return b;
	}
	
	
	protected DataBlock getModifierChain(SceneGraphComponent c) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteString(nodeNameMap.get(c));
		w.WriteU32(0x00000000); // node modifier chain
		w.WriteU32(0x00000000); // no attributes
		w.AlignTo4Byte(); // modifier chain padding
		
		List<DataBlock> modifiers = getNodeModifiers(c);
		w.WriteU32(modifiers.size()); // modifier count (only hierarchy)
		for (DataBlock modBlock : modifiers)
			w.WriteDataBlock(modBlock);
		
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_MOFIFIER_CHAIN);
		return b;
	}
	
	
	protected List<DataBlock> getMaterialBlocks(EffectiveAppearance a) {
		List<DataBlock> r = new LinkedList<DataBlock>();
		U3DTexture tex = textureMap.get(a);
		if (tex != null)
			r.add(getModifierChain(tex));
	    r.add(getLitTextureShader(a));
	    r.add(getMaterialResource(a));
		return r;
	}
	
	
	protected DataBlock getHeaderBlock(int declSize, long contSize) {
		BitStreamWrite w = new BitStreamWrite();
		w.WriteI16(EXPORTER_VERSION_MAJOR);
		w.WriteI16(EXPORTER_VERSION_MINOR);
		w.WriteU32(PROFILE_OPTIONS);
		w.WriteU32(declSize);
		w.WriteU64(36 + contSize + declSize);
		w.WriteU32(CHARACTER_ENCODING);
		DataBlock b = w.GetDataBlock();
		b.setBlockType(TYPE_FILE_HEADER);
		return b;
	}
	
	public void writeDataBlock(DataBlock b, WritableByteChannel o) throws IOException {
		int dataSize = (int)Math.ceil(b.getDataSize() / 4.0); // include padding
		int metaDataSize = (int)Math.ceil(b.getMetaDataSize() / 4.0); // include padding
		ByteBuffer buffer = ByteBuffer.allocate((int)(12 + 4 * (dataSize + metaDataSize)));
		buffer.order(LITTLE_ENDIAN);
		buffer.putInt((int)b.getBlockType());
		buffer.putInt((int)b.getDataSize());
		buffer.putInt((int)b.getMetaDataSize());

		for (int i = 0; i < dataSize; i++)
			buffer.putInt((int)b.getData()[i]);
		for (int i = 0; i < metaDataSize; i++)
			buffer.putInt((int)b.getMetaData()[i]);
		buffer.rewind();
		o.write(buffer);
	}
	
	
	public void writeScene(JrScene scene, OutputStream out) throws IOException {
		System.out.print("U3D Export: prepare data...");
		SceneGraphComponent skyBox = U3DSceneUtility.getSkyBox(scene);
		if (skyBox != null) scene.getSceneRoot().addChild(skyBox);
		prepareSceneData(scene);
		if (skyBox != null) scene.getSceneRoot().removeChild(skyBox);
		System.out.print("writing...");
		WritableByteChannel o = Channels.newChannel(out);
		writeDataBlock(getHeaderBlock(0, 0), o);
		
		// declarations
		for (SceneGraphComponent c : nodes) {
			writeDataBlock(getModifierChain(c), o);
			if (getPreparedGeometry(c) != null) {
				EffectiveAppearance a = appearanceMap.get(c);
				for (DataBlock b : getMaterialBlocks(a))
					writeDataBlock(b, o);
			}
		}
		for (SceneGraphComponent viewNode : viewNodes) {
			writeDataBlock(getViewNodeModifierChain(viewNode), o);
		}
		for (SceneGraphComponent lightNode: lightNodes) {
			writeDataBlock(getLightNodeModifierChain(lightNode), o);
		}
		for (Geometry g : geometries){
			Geometry p = preparedGeometryMap.get(g);
			if (p != null)
				writeDataBlock(getModifierChain(p), o);
		}
		for (U3DTexture tex : textures) 
			writeDataBlock(getModifierChain(tex), o);
		
		// continuations
		for (SceneGraphComponent c : nodes)
			for (DataBlock b : getContinuations(c))
				writeDataBlock(b, o);
		for (Camera c : cameras)
			writeDataBlock(getViewResource(c), o);	
		for (Light l : lights) 
			writeDataBlock(getLightResource(l), o);
		for (Geometry g : geometries){
			Geometry p = preparedGeometryMap.get(g);
			if (p != null)
				for (DataBlock b : getContinuations(p)) 
					writeDataBlock(b, o);
		}
		for (U3DTexture tex : textures) {
			for (DataBlock b : getContinuations(tex))
				writeDataBlock(b, o);
		}
		o.close();
		System.out.println("done.");
	}

	public void writeScene(JrScene scene, Writer out) throws IOException {
		throw new UnsupportedOperationException("U3D is a binary file format");
	}
	
	public void write(SceneGraphNode c, OutputStream out) throws IOException {
		SceneGraphComponent root = null;
		if (c instanceof SceneGraphComponent) root = (SceneGraphComponent) c;
		else {
			root = new SceneGraphComponent();
			SceneGraphUtility.addChildNode(root, c);
		}
		JrScene scene = new JrScene(root);
		writeScene(scene, out);
	}
	
	public static void write(JrScene scene, OutputStream out) throws IOException {
		WriterU3D writer = new WriterU3D();
		writer.writeScene(scene, out);
	}
	
	private Geometry getPreparedGeometry(SceneGraphComponent c) {
		Geometry g = c.getGeometry();
		if (g == null) return null;
		Geometry p = preparedGeometryMap.get(g);
		return p;
	}
	
	
	private void prepareSceneData(JrScene scene) {
		rootNode = scene.getSceneRoot();
		
		nodes = U3DSceneUtility.getSceneGraphComponents(scene);
		visibilityMap = U3DSceneUtility.getVisibility(scene);
		parentMap = U3DSceneUtility.getParentsMap(nodes);
		nodeNameMap = U3DSceneUtility.getUniqueNames(nodes);
		
		viewNodes = U3DSceneUtility.getViewNodes(scene);
		cameras = U3DSceneUtility.getCameras(scene);
		cameraNameMap = U3DSceneUtility.getUniqueNames(cameras);

		lightNodes = U3DSceneUtility.getLightNodes(scene);
		lights = U3DSceneUtility.getLights(scene);
		lightNameMap = U3DSceneUtility.getUniqueNames(lights);
		
		geometries = U3DSceneUtility.getGeometries(scene);
		preparedGeometryMap = U3DSceneUtility.prepareGeometry(geometries);
		geometryNameMap = U3DSceneUtility.getUniqueNames(preparedGeometryMap.values());
//		geometryBoundingBox = U3DSceneUtility.getBoundingBoxes(geometries);
		
		appearanceMap = U3DSceneUtility.getAppearanceMap(scene);
		appearances = new HashSet<EffectiveAppearance>(appearanceMap.values());
		appearanceNameMap = U3DSceneUtility.getAppearanceNames(appearances);
		
		textureMap = U3DSceneUtility.getTextureMap(appearances);
		textures = new HashSet<U3DTexture>(textureMap.values());
		sphereMapsMap = U3DSceneUtility.getSphereMapsMap(appearances);
		textures.addAll(sphereMapsMap.values());
		textureNameMap = U3DSceneUtility.getTextureNames("Texture", textures);
		texturePNGData = U3DSceneUtility.preparePNGTextures(textures);
		/*		
		U3DSceneUtility.printNodes("SceneGraphComponents", nodes);
		U3DSceneUtility.printNameMap(nodeNameMap);
		U3DSceneUtility.printNodes("View Nodes", viewNodes);
		U3DSceneUtility.printNodes("Cameras", cameras);
		U3DSceneUtility.printNameMap(cameraNameMap);
		
		U3DSceneUtility.printNodes("Light Nodes", lightNodes);
		U3DSceneUtility.printNodes("Lights", lights);
		U3DSceneUtility.printNameMap(lightNameMap);
		
		U3DSceneUtility.printNodes("Geometries", geometries);
		U3DSceneUtility.printNameMap(geometryNameMap);
		U3DSceneUtility.printAppearanceNameMap(appearanceNameMap);
		U3DSceneUtility.printTextures(textures);
		U3DSceneUtility.printTextureNameMap(textureNameMap);
		*/
	}
	

}