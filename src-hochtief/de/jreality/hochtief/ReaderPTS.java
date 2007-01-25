/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyriight notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.hochtief;

import java.awt.Color;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.AbstractReader;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.Rectangle3D;

/**
 * 
 * simple reader for the PTS file format.
 * 
 * @author Markus Schmies, Niels Bleicher
 * 
 */
public class ReaderPTS extends AbstractReader {

	private static final int minSegmentCount = 20000;

	public ReaderPTS() {
		root = new SceneGraphComponent();
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		app.setAttribute(CommonAttributes.PICKABLE, false);
		app.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		app.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POINT_SIZE, 30.);
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, Color.white);
		root.setAppearance(app);
	}

	public void setInput(Input input) throws IOException {
		super.setInput(input);
		load();
	}

	// double [] t003 = new double[] {
	// 0.898791,-0.438375,-0.000887, 216.043,
	// 0.438376, 0.89879 , 0.001709 , 8029.23,
	// 4.8e-05, -0.001925, 0.999998, 102.783,
	// 0,0,0, 1 };
	//  
	// double [] t002 = new double[] {
	// 0.994756 ,-0.089381,-0.049722,203.604,
	// 0.089768,0.995947,0.005596,8038.29,
	// 0.04902,-0.01003,0.998747, 102.657,
	// 0,0,0, 1 };

	private void load() throws IOException {

		LineNumberReader r = new LineNumberReader(input.getReader());

		String l = null;

		while ((l = r.readLine().trim()).startsWith("#"))
			;

		int colCount = 0;
		;

		double phi_ = 0;
		double theta_ = 0;

		int N = 1010 / 2;
		int M = 431;

		final double[][] depth = new double[M][N];

		byte[][] colorR = new byte[M][N];
		byte[][] colorG = new byte[M][N];
		byte[][] colorB = new byte[M][N];

		long last_n = 0;
		long last_m = 0;

		int vertexCount = 0;

		while ((l = r.readLine()) != null) {

			String[] split = l.split(" ");
			if (split.length != 7)
				continue;

			colCount++;

			double x = Double.parseDouble(split[0]);
			double y = Double.parseDouble(split[1]);
			double z = Double.parseDouble(split[2]);

			double phi = Math.atan2(y, x);
			double theta = Math.atan2(z, Math.sqrt(x * x + y * y));

			int n = (int) Math.round((phi + Math.PI) / 2 / (Math.PI                               ) * (N - 1));
			int m = (int) Math.round((-theta + Math.PI / 2)
					/ (Math.PI - (Math.PI / 2 - 1.1306075316023216)) * (M - 1));

			if (depth[m][n] == 0) {
				vertexCount++;
				depth[m][n] = Math.sqrt(x * x + y * y + z * z) / 1000000;
				colorR[m][n] = (byte) Double.parseDouble(split[4]);
				colorG[m][n] = (byte) Double.parseDouble(split[5]);
				colorB[m][n] = (byte) Double.parseDouble(split[6]);
			}

			// System.out.println(x+" "+y+" "+z);
			// System.out.println( phi + " " + (phi-phi_)+ " " + theta + " " +
			// (theta-theta_) );
			if (last_m < m - 1) {
				System.out.println("skipping line " + (m - 1));
			}

			if (last_m != m) {

				// System.out.println( "New line: " + m + " colCount=" +colCount
				// + " theta="+theta + " phi=" + phi + " lastPhi="+phi_);
				colCount = 0;
			}

			last_n = n;
			last_m = m;

			phi_ = phi;
			theta_ = theta;
		}

		// fill single missing pixels by interpolation
		for (int i = 0; i < M; i++) {
			for (int j = 1; j < N - 1; j++) {
				if (depth[i][j] == 0 && depth[i][j - 1] != 0
						&& depth[i][j] != 0) {
					depth[i][j] = (depth[i][j - 1] + depth[i][j + 1]) / 2;
					vertexCount++;
				}
			}
		}

		final int[][] seg = new int[M][N];

		int segmentCount = generateQuadSegmentation(depth, seg);
		// int segmentCount = generateEdgeSegmentation(depth, seg );

		System.out.println("number of segments = " + segmentCount);

		final int[] stat = segmentationStatistic(seg, segmentCount);

		for (int i = 0; i < stat.length; i++)
			System.out.println(stat[i]);

		QuadCriteria standartDepthSegmentation = new QuadCriteria() {

			public boolean pass(int i, int j) {
				if (seg[i][j] == -1)
					return false;
				
				return stat[seg[i][j]] >= minSegmentCount
						&& faceSegmentCriteria(depth, seg, i, j);
			}
		};
		
		
		int[][] INDICES = new int[M][N];
		
		for( int i = 0; i < M; i++) {
			for( int j = 0; j < N; j++) {
				INDICES[i][j] = -1;
			}
		}
		
		vertexCount = 0;
		int faceCount = 0;
		for( int i = 1; i < M; i++) {
			for( int j = 1; j < N; j++) {
				if( standartDepthSegmentation.pass(i, j)) {
					if( INDICES[i-1][j-1] == -1 ) { INDICES[i-1][j-1] = vertexCount; vertexCount++; };
					if( INDICES[i-1][j  ] == -1 ) { INDICES[i-1][j  ] = vertexCount; vertexCount++; };
					if( INDICES[i  ][j-1] == -1 ) { INDICES[i  ][j-1] = vertexCount; vertexCount++; };
					if( INDICES[i  ][j  ] == -1 ) { INDICES[i  ][j  ] = vertexCount; vertexCount++; };
					faceCount++;
				}
			}
		}

		System.out.println( "vertexcount="+vertexCount);
		System.out.println( "facecount="+faceCount);
		int index;

		int[] faceIndices = new int[faceCount*4];
		
		index = 0;
		for (int i = 1; i < M; i++) {
			for (int j = 1; j < N; j++) {
				if( standartDepthSegmentation.pass(i, j)) {
			
					faceIndices[index + 0] = INDICES[i - 1][j - 1];
					faceIndices[index + 1] = INDICES[i - 1][j];
					faceIndices[index + 3] = INDICES[i][j - 1];
					faceIndices[index + 2] = INDICES[i][j];
				
					index += 4;
				}
			}
		}
	
		
		double[] points = new double[3 * vertexCount];

		double[] colors = new double[3 * vertexCount];

		double[][] textureCoordinates = new double[vertexCount][2];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {

				if( INDICES[i][j] == -1 )
					continue;
				
				double R = depth[i][j];


				// if( generateQuads && !usedInQuad[i][j] )
				// R=1;
				double phi = j * 2 * Math.PI / (N - 1);
				double theta = -i
						* (Math.PI - (Math.PI / 2 - 1.1306075316023216))
						/ (M - 1) + Math.PI / 2;

				final double x = R * Math.cos(phi) * Math.cos(theta);
				final double y = R * Math.sin(phi) * Math.cos(theta);
				final double z = R * Math.sin(theta);

				index = INDICES[i][j];
				
				points[3 * index + 0] = x;
				points[3 * index + 1] = y;
				points[3 * index + 2] = z;

				colors[3 * index] = colorR[i][j] / 255.0;
				colors[3 * index + 1] = colorG[i][j] / 255.0;
				colors[3 * index + 2] = colorB[i][j] / 255.0;

				textureCoordinates[index][0] = -(double) j / N
						+ (phi_ / (2 * Math.PI) + 0.5);
				textureCoordinates[index][1] = (double) i / M;

				if (textureCoordinates[index][0] > 1)
					textureCoordinates[index][0] = textureCoordinates[index][0] - 1;
				if (textureCoordinates[index][0] < 1)
					textureCoordinates[index][0] = 1 + textureCoordinates[index][0];
				if (textureCoordinates[index][1] > 1)
					textureCoordinates[index][1] = textureCoordinates[index][1] - 1;
				if (textureCoordinates[index][1] < 1)
					textureCoordinates[index][1] = 1 + textureCoordinates[index][1];

			}
		}

		// System.out.println( theta_ * 360 / 2 / Math.PI);

		 IndexedFaceSetFactory psf = new IndexedFaceSetFactory();
		//PointSetFactory psf = new PointSetFactory();

		psf.setVertexCount(vertexCount);
		psf.setFaceCount(faceCount);

		psf.setVertexCoordinates(points);
		psf.setFaceIndices(faceIndices,4);

		//psf.setVertexColors(colors);
		psf.setVertexTextureCoordinates(textureCoordinates);
		// psf.setGenerateVertexNormals(true);
		// psf.setGenerateFaceNormals(true);
		psf.update();
		// Rectangle3D bb =
		// GeometryUtility.calculateBoundingBox(psf.getIndexedFaceSet());
		// psf.getPointSet().setGeometryAttributes(GeometryUtility.BOUNDING_BOX,
		// bb);

		root.setGeometry(psf.getPointSet());
		root.setAppearance(new Appearance());
		//root.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, false);
		//root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		//root.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		//root.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		//root.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		root.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR,
				Color.WHITE);
		root.getAppearance().setAttribute(
				CommonAttributes.POLYGON_SHADER + "."
						+ CommonAttributes.DIFFUSE_COLOR, Color.WHITE);

		ImageData img = ImageData.load(Input.getInput(texturePath));
		Texture2D tex = TextureUtility.createTexture(root.getAppearance(),
				"polygonShader", img, false);

		// root.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS,0.005);
		MatrixBuilder.euclidean().scale(1).assignTo(root);
	}


	int generateQuadSegmentation(double depth[][], int seg[][]) {

		final int M = depth.length;
		final int N = depth[0].length;

		int[] idMap = quadWiseSegmentation(depth, seg, M, N);

		return identifyIdMap(seg, M, N, idMap);
	}

	int generateEdgeSegmentation(double depth[][], int seg[][]) {

		final int M = depth.length;
		final int N = depth[0].length;

		int[] idMap = edgeWiseSegmentation(depth, seg, M, N);

		return identifyIdMap(seg, M, N, idMap);
	}

	private int identifyIdMap(int[][] seg, final int M, final int N, int[] idMap) {
		for (int i = 0; i < idMap.length; i++) {
			int target = idMap[i];

			while (idMap[target] != target)
				target = idMap[target];

			idMap[i] = target;
		}

		int segmentCount = 0;
		int[] segmentCountMap = new int[idMap.length];

		for (int i = 0; i < idMap.length; i++) {
			if (idMap[i] == i) {
				segmentCountMap[i] = segmentCount;
				segmentCount++;
			} else {
				segmentCountMap[i] = -1; // marks illegal state
			}
		}

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (seg[i][j] != -1) {
					seg[i][j] = segmentCountMap[idMap[seg[i][j]]];
					if (seg[i][j] == -1)
						throw new IllegalStateException();
				}
			}
		}
		return segmentCount;
	}

	private int[] quadWiseSegmentation(double[][] depth, int[][] seg,
			final int M, final int N) {
		int id = 0;

		for (int i = 1; i < M; i++) {
			for (int j = 0; j < N; j++) {
				seg[0][j] = -1;
			}
			seg[i][0] = -1;
			for (int j = 1; j < N; j++) {

				double R = depth[i][j];

				if (faceCriteria(depth, i, j)) {
					if (seg[i][j - 1] == -1 && seg[i - 1][j - 1] == -1
							&& seg[i - 1][j] == -1) {
						seg[i][j - 1] = seg[i - 1][j - 1] = seg[i - 1][j] = id;
						id++;
					}

					seg[i][j] = seg[i][j - 1] = seg[i - 1][j - 1] = seg[i - 1][j] = Math
							.max(seg[i][j - 1], Math.max(seg[i - 1][j - 1],
									seg[i - 1][j]));

				} else {
					seg[i][j] = -1;
				}
			}
		}

		System.out.println("number of id " + id);

		int[] idMap = new int[id];

		for (int i = 0; i < idMap.length; i++) {
			idMap[i] = i;
		}

		for (int i = 1; i < M; i++) {
			for (int j = 1; j < N; j++) {
				if (seg[i][j] == -1)
					continue;

				int minI = Math.min(Math.min(seg[i][j], seg[i][j - 1]), Math
						.min(seg[i - 1][j], seg[i - 1][j - 1]));

				if (minI == -1)
					continue;

				if (faceCriteria(depth, i, j)) {

					if (minI < seg[i][j])
						setIdMap(idMap, minI, seg[i][j]);
					if (minI < seg[i][j - 1])
						setIdMap(idMap, minI, seg[i][j - 1]);
					if (minI < seg[i - 1][j])
						setIdMap(idMap, minI, seg[i - 1][j]);
					if (minI < seg[i - 1][j - 1])
						setIdMap(idMap, minI, seg[i - 1][j - 1]);

				}
			}
		}

		return idMap;
	}

	private int[] edgeWiseSegmentation(double[][] depth, int[][] seg,
			final int M, final int N) {
		int id = 0;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {

				double R = depth[i][j];

				if (j > 0 && edgeCriteria(R, depth[i][j - 1])) {
					if (seg[i][j - 1] == -1) {
						seg[i][j - 1] = id;
						id++;
					}
					seg[i][j] = seg[i][j - 1];

				} else if (i > 0 && edgeCriteria(R, depth[i - 1][j])) {
					if (seg[i - 1][j] == -1) {
						seg[i - 1][j] = id;
						id++;
					}
					seg[i][j] = seg[i - 1][j];
				} else {
					seg[i][j] = -1;
				}
			}
		}

		System.out.println("number of id " + id);

		int[] idMap = new int[id];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (seg[i][j] == -1)
					continue;

				if (i > 0 && seg[i - 1][j] != -1 && seg[i - 1][j] != seg[i][j]
						&& edgeCriteria(depth[i - 1][j], depth[i][j])) {
					if (seg[i][j] < seg[i - 1][j]) {
						setIdMap(idMap, seg[i][j], seg[i - 1][j]);
					} else {
						setIdMap(idMap, seg[i - 1][j], seg[i][j]);
					}

				}
			}
		}

		return idMap;
	}

	private void setIdMap(int[] idMap, int i, int j) {
		if (!(i < j))
			throw new IllegalArgumentException(i + " < " + j);

		if (idMap[j] == j) {
			idMap[j] = i;
			return;
		}

		if (idMap[j] < i) {
			int tmp = idMap[j];
			idMap[j] = i;
			setIdMap(idMap, tmp, i);
		} else if (i < idMap[j]) {
			setIdMap(idMap, i, idMap[j]);
		}
	}

	int[] segmentationStatistic(int[][] seg, int count) {

		int[] stat = new int[count];

		final int M = seg.length;
		final int N = seg[0].length;

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if (seg[i][j] != -1)
					stat[seg[i][j]]++;
			}
		}

		return stat;
	}

	private boolean edgeCriteria(double d, double e) {

		if (!(d != 0 && e != 0))
			return false;

		double meanDepth = (d + e) / 2;

		final double EPS = eps * meanDepth;

		return Math.abs(meanDepth - d) < EPS && Math.abs(meanDepth - e) < EPS;
	}

	final static double eps = 6.0 / 100;

	private boolean faceSegmentCriteria(double depth[][], int[][] seg, int i,
			int j) {

		int s1 = seg[i - 1][j    ];
		int s2 = seg[i - 1][j - 1];
		int s3 = seg[i    ][j    ];
		int s4 = seg[i    ][j - 1];

		if ( s3 == -1 || !(s1 == s2 && s1 == s3 && s1 == s4))
			return false;

		return faceCriteria(depth, i, j);
	}

	interface QuadCriteria {
		/**
		 * Criteria for a quadraleteral
		 * @param i
		 * @param j
		 * @return
		 */
		public boolean pass( int i, int j );
	}
	
	
	private boolean faceCriteria(double[][] depth, int i, int j) {

		double d1 = depth[i - 1][j];
		double d2 = depth[i - 1][j - 1];
		double d3 = depth[i][j];
		double d4 = depth[i][j - 1];
		if (!(d1 != 0 && d2 != 0 && d3 != 0 && d4 != 0))
			return false;

		double meanDepth = (d1 + d2 + d3 + d3) / 4;

		final double EPS = eps * meanDepth;

		return Math.abs(meanDepth - d1) < EPS && Math.abs(meanDepth - d2) < EPS
				&& Math.abs(meanDepth - d3) < EPS
				&& Math.abs(meanDepth - d4) < EPS;

	}

	private void load_() throws IOException {
		int skip = 10;
		LineNumberReader r = new LineNumberReader(input.getReader());

		String l = null;
		while ((l = r.readLine().trim()).startsWith("#"))
			;

		int pointCount = 10000;// Integer.parseInt(l)/(skip+1);
		double[] points = new double[pointCount * 3];
		double[] colors = new double[pointCount * 3];

		int index = 0;
		double phi_ = 0;
		double theta_ = 0;

		for (int i = 0; i < 10000; i++)
			r.readLine();
		while ((l = r.readLine()) != null) {
			if (index == pointCount)
				break;
			for (int i = 0; i < skip; i++)
				r.readLine();
			String[] split = l.split(" ");
			if (split.length != 7)
				continue;
			double x = Double.parseDouble(split[0]);
			double y = Double.parseDouble(split[1]);
			double z = Double.parseDouble(split[2]);

			double R = Math.sqrt(x * x + y * y + z * z);

			double phi = Math.atan2(y, x);
			double theta = Math.atan2(z, Math.sqrt(x * x + y * y));

			System.out.println(x + "  " + y + "  " + z);
			// System.out.println( phi + " " + (phi-phi_)+ " " + theta + " " +
			// (theta-theta_) );

			phi_ = phi;
			theta_ = theta;

			points[3 * index] = x / R;
			points[3 * index + 1] = y / R;
			points[3 * index + 2] = z / R;

			colors[3 * index] = Double.parseDouble(split[4]) / 255;
			colors[3 * index + 1] = Double.parseDouble(split[5]) / 255;
			colors[3 * index + 2] = Double.parseDouble(split[6]) / 255;
			index++;
		}

		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(pointCount);
		psf.setVertexCoordinates(points);
		psf.setVertexColors(colors);

		psf.update();
		Rectangle3D bb = GeometryUtility
				.calculateBoundingBox(psf.getPointSet());
		psf.getPointSet().setGeometryAttributes(GeometryUtility.BOUNDING_BOX,
				bb);
		root.setGeometry(psf.getPointSet());
		root.setAppearance(new Appearance());
		root.getAppearance().setAttribute(CommonAttributes.SPHERES_DRAW, true);
		root.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, 0.001);
		// MatrixBuilder.euclidean().scale(10).assignTo(root);
	}

	private String texturePath = "";

	public void setTexturePath(String path) {
		texturePath = path;
	}

	public static void main(String[] arg) {
		// ReaderPTS pts002 = new ReaderPTS();
		ReaderPTS pts003 = new ReaderPTS();
		// pts002.setTexturePath("/net/MathVis/data/testData3D/zfs/INHOUSE_II_002_color_quartersize.jpg");
		pts003
				.setTexturePath("/net/MathVis/data/testData3D/zfs/INHOUSE_II_003_color_quartersize2.jpg");
		try {
			// pts002.setInput( Input.getInput(
			// "/net/MathVis/data/testData3D/zfs/INHOUSE_II_002_ss10.pts"));
			pts003
					.setInput(Input
							.getInput("/net/MathVis/data/testData3D/zfs/INHOUSE_II_003_ss10.pts"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SceneGraphComponent sceneRoot = new SceneGraphComponent();
		// sceneRoot.addChild(pts002.root);
		sceneRoot.addChild(pts003.root);

		double[] t003 = new double[] { 0.898791, -0.438375, -0.000887, 216.043,
				0.438376, 0.89879, 0.001709, 8029.23, 4.8e-05, -0.001925,
				0.999998, 102.783, 0, 0, 0, 1 };

		double[] t002 = new double[] { 0.994756, -0.089381, -0.049722, 203.604,
				0.089768, 0.995947, 0.005596, 8038.29, 0.04902, -0.01003,
				0.998747, 102.657, 0, 0, 0, 1 };

		// Matrix trafo=new Matrix(t003);
		// trafo.invert();
		// trafo.multiplyOnRight(new Matrix(t002));
		// MatrixBuilder.euclidean(trafo).assignTo(pts003.root);

		// System.out.println( Matrix.times( new Matrix(t002), new
		// Matrix(t003).getInverse() ) );
		// MatrixBuilder.euclidean().assignTo(pts002.root);
		// //MatrixBuilder.euclidean(new Matrix(t003).getInverse()).times(new
		// Matrix(t002)).assignTo(pts003.root);
		// MatrixBuilder.euclidean(new Matrix(t002)).assignTo(pts002.root);
		// MatrixBuilder.euclidean(new Matrix(t003)).assignTo(pts003.root);

		ViewerApp.display(sceneRoot);
	}
}
