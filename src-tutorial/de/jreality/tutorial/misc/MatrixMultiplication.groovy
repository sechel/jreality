// 3D Matrix Multiplication
// Author: Michael Pacchioli
// Version 0.2

package de.jreality.tutorial.misc

import java.awt.Color
import javax.swing.SwingConstants

import de.jreality.geometry.GeometryUtility
import de.jreality.geometry.IndexedFaceSetFactory 
import de.jreality.geometry.IndexedFaceSetUtility
import de.jreality.geometry.PointSetFactory
import de.jreality.geometry.Primitives
import de.jreality.geometry.CoordinateSystemFactory

import de.jreality.scene.Appearance
import de.jreality.scene.IndexedFaceSet
import de.jreality.scene.PointSet
import de.jreality.scene.SceneGraphComponent
import de.jreality.scene.data.Attribute
import de.jreality.scene.data.StorageModel

import de.jreality.shader.DefaultGeometryShader
import de.jreality.shader.DefaultLineShader
import de.jreality.shader.DefaultPointShader
import de.jreality.shader.DefaultPolygonShader
import de.jreality.shader.DefaultTextShader
import de.jreality.shader.ShaderUtility

import de.jreality.ui.viewerapp.ViewerApp
import de.jreality.util.SceneGraphUtility

import de.jreality.math.MatrixBuilder

import static de.jreality.shader.CommonAttributes.*



// Face labeling function. //

public static void label(IndexedFaceSet ifs, String text) {
    int n = ifs.getNumFaces();
    String[] labels = new String[n];
    
    for (int i = 0; i < n; i++) labels[i] = text;
    
    ifs.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels))

    // println "ATTRIBUTES"
    // println ifs.getVertexAttributes(Attribute.LABELS);
}

// Point labeling function. //

public static void labelPoint(PointSet ps, String text) {
    int n = ps.getNumPoints();
    String[] labels = new String[n];
    
    // println "${n} POINTS"
    
    for (int i = 0; i < n; i++) labels[i] = text;
    
    ps.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels))
}

// Square generating function. //

def square = {
    IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory()
                
    double[][] vertices = [
        [0.0, 0.0, 0.0], [0.0, 1.0, 0.0], [1.0, 1.0, 0.0], [1.0, 0.0, 0.0]
    ]
                
    int[][] faceIndices = [
        [0, 1, 2, 3] 
    ];
                
    ifsf.setVertexCount(4)
    ifsf.setVertexCoordinates(vertices)
    ifsf.setFaceCount(faceIndices.length)
    ifsf.setFaceIndices(faceIndices)
                
    ifsf.setGenerateEdgesFromFaces(true)
    ifsf.setGenerateFaceNormals(true)
    ifsf.update()

    return ifsf.getIndexedFaceSet()
}

def point = {
    PointSetFactory psf = new PointSetFactory()
    double[] vertices = [0.0, 0.0, 0.0]
    
    psf.setVertexCount(1)
    psf.setVertexCoordinates(vertices)
    psf.update()
    
    return psf.getPointSet()
}



// Initialize data. //

def randomGen = new Random()
def maxSize = 5
def maxRand = 5
def valueModifier = -5

def aWidth = 3 //randomGen.nextInt(maxSize) + 1
def aHeight = 2 //randomGen.nextInt(maxSize) + 1

def bWidth = 4 //randomGen.nextInt(maxSize) + 1
def bHeight = aWidth

def a = []
def b = []

def pieces = []
def useTestData = false

textScale = 0.0075



println "a width: ${aWidth}"
println "a height: ${aHeight}"
println "b width: ${bWidth}"
println "b height: ${bHeight}"



// Populate matrix. //

(1..aHeight).each {
    tempRow = []
    
    (1..aWidth).each {
        tempRow.add(randomGen.nextInt(maxRand) + valueModifier)
    }
    
    a.add(tempRow)
}

(1..bHeight).each {
    tempRow = []
    
    (1..bWidth).each {
        tempRow.add(randomGen.nextInt(maxRand) + valueModifier)
    }
    
    b.add(tempRow)
}

println "a: ${a}"
println "b: ${b}"



// test data //

if (useTestData) {
    a = [[7,7], [2,9]]
    b = [[7],[1]]

    aWidth = 2
    aHeight = 2
    bWidth = 1
    bHeight = 2
}



// Build list of lists of multiplied numbers. //

a.each { row ->
    println "a Row: {$row}"
    rowList = []
    
    (1..bWidth).each { bx ->
        column = []
        (1..bHeight).each { by ->
            column.add(b[by-1][bx-1])
        }
        
        println "b Column: {$column}"
        
        columnList = []
        
        (1..row.size()).each { offset ->
            columnList.add(row[offset-1] * column[offset-1])
        }
        
        rowList.add(columnList)
    }
    
    pieces.add(rowList)
}

println pieces



// Initialize 3D view. //

SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("Matrix Multiplication")
SceneGraphComponent aSgc = SceneGraphUtility.createFullSceneGraphComponent("A Matrix")
SceneGraphComponent bSgc = SceneGraphUtility.createFullSceneGraphComponent("B Matrix")
SceneGraphComponent cSgc = SceneGraphUtility.createFullSceneGraphComponent("C Matrix")

ay = 0
a.each { row ->
    ay++
    SceneGraphComponent aRowSgc = SceneGraphUtility.createFullSceneGraphComponent("A Row")
    
    ax = 0
    row.each { element ->
        ax++
        def aElementSgc = SceneGraphUtility.createFullSceneGraphComponent("A Element")

        def shape = square()
        aElementSgc.setGeometry(shape);
        aElementSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.BLUE)
           

        def aValueSgc = SceneGraphUtility.createFullSceneGraphComponent("A Value")
        
        def ps = point()
        aValueSgc.setGeometry(ps);
        aValueSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.BLUE)

        labelPoint(ps, Integer.toString(element));

        def dgs = ShaderUtility.createDefaultGeometryShader(aValueSgc.getAppearance(), false)
        dgs.getPointShader().setPointRadius(0.0)
        
        def ts = dgs.getPointShader().getTextShader()
        ts.setScale(textScale)
        ts.setAlignment(SwingConstants.CENTER)
        ts.setOffset(0.0, 0.0, 0.0)
        
        MatrixBuilder.euclidean().translate((ax - 1 + 0.5), -1 * (ay - 1 - 0.5), 0.25).assignTo(aValueSgc);
        MatrixBuilder.euclidean().translate((ax-1), -1 * (ay-1), 0).assignTo(aElementSgc);
        //MatrixBuilder.euclidean().translate(-1 * (ay - 1 - 0.5), (ax - 1 + 0.5), 0.25).assignTo(aValueSgc);
        
        aRowSgc.addChild(aElementSgc)
        aRowSgc.addChild(aValueSgc)
    }
    
    aSgc.addChild(aRowSgc)
}
    //MatrixBuilder.euclidean().translate(aHeight-1, -aWidth+1, 0).rotateZ(Math.PI/2).assignTo(aSgc);

by = 0
b.each { row ->
    by++
    SceneGraphComponent bRowSgc = SceneGraphUtility.createFullSceneGraphComponent("B Row")
    
    bx = 0
    row.each { element ->
        bx++
        SceneGraphComponent bElementSgc = SceneGraphUtility.createFullSceneGraphComponent("B Element")

        IndexedFaceSet ifs = square()
        bElementSgc.setGeometry(ifs);
        bElementSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.GREEN)
        
        MatrixBuilder.euclidean().translate((bx-1), -1 * (by-1), 0).assignTo(bElementSgc)
        
        def bValueSgc = SceneGraphUtility.createFullSceneGraphComponent("B Value")
        
        def ps = point()
        bValueSgc.setGeometry(ps);
        bValueSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.GREEN)

        labelPoint(ps, Integer.toString(element));

        def dgs = ShaderUtility.createDefaultGeometryShader(bValueSgc.getAppearance(), false)
        dgs.getPointShader().setPointRadius(0.0)
        
        def ts = dgs.getPointShader().getTextShader();
        ts.setScale(textScale);
        ts.setAlignment(SwingConstants.CENTER)
        ts.setOffset(0.0, 0.0, 0.0)        
        
        MatrixBuilder.euclidean().translate((bx - 1 + 0.5), -1 * (by - 1 - 0.5), -0.25).assignTo(bValueSgc)
        
        bRowSgc.addChild(bElementSgc)
        bRowSgc.addChild(bValueSgc)
    }
    
    bSgc.addChild(bRowSgc)
}

MatrixBuilder.euclidean().rotateY(Math.PI/2).assignTo(bSgc);

SceneGraphComponent factorSgc = SceneGraphUtility.createFullSceneGraphComponent("Factors")

pieces.eachWithIndex { row, i ->
    row.eachWithIndex { column, j -> 
        xOffset = i
        zOffset = -j
        
        yOffset = 1
        
        column.each { element ->
            yOffset--
            println "OFFSET: ${xOffset}, ${yOffset}, ${zOffset}"
            
            def factorElementSgc = SceneGraphUtility.createFullSceneGraphComponent("Factor Element")
    
            def ps = point()
            factorElementSgc.setGeometry(ps);
            factorElementSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.MAGENTA)
    
            labelPoint(ps, Integer.toString(element));

            def dgs = ShaderUtility.createDefaultGeometryShader(factorElementSgc.getAppearance(), false)
            dgs.getPointShader().setPointRadius(0.0)
            
            def ts = dgs.getPointShader().getTextShader();
            ts.setScale(textScale);
            ts.setAlignment(SwingConstants.CENTER)
            ts.setOffset(0.0, 0.0, 0.0)
        
            MatrixBuilder.euclidean().translate(xOffset + 0.5, yOffset + 0.5, zOffset - 0.5).assignTo(factorElementSgc)
            factorSgc.addChild(factorElementSgc)
        }
    }
}

(1..aHeight).each { cx ->
	row = pieces[cx-1]

	(1..bWidth).each { cy ->
		element = row[cy-1]
		
		total = 0
		element.each { partial ->
			total += partial
		}
	
    	SceneGraphComponent cElementSgc = SceneGraphUtility.createFullSceneGraphComponent("C Element")

    	IndexedFaceSet ifs = square()
    	cElementSgc.setGeometry(ifs);
    	cElementSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.RED)
    	
    	MatrixBuilder.euclidean().translate((cx-1), -1 * (cy-1), 0).assignTo(cElementSgc)
    	
    	def cValueSgc = SceneGraphUtility.createFullSceneGraphComponent("C Value")
        
        def ps = point()
        cValueSgc.setGeometry(ps);
        cValueSgc.getAppearance().setAttribute(DIFFUSE_COLOR, Color.RED)

        labelPoint(ps, Integer.toString(total));

        def dgs = ShaderUtility.createDefaultGeometryShader(cValueSgc.getAppearance(), false)
        dgs.getPointShader().setPointRadius(0.0)
        
        def ts = dgs.getPointShader().getTextShader();
        ts.setScale(textScale);
        ts.setAlignment(SwingConstants.CENTER)
        ts.setOffset(0.0, 0.0, 0.0)        
        
        MatrixBuilder.euclidean().translate((cx - 1 + 0.5), -1 * (cy - 1 - 0.5), 0.25).assignTo(cValueSgc)
    	
    	cSgc.addChild(cValueSgc)
    	cSgc.addChild(cElementSgc)
	}
}

zShift = ([aHeight, bHeight].max()) - 1
MatrixBuilder.euclidean().rotateX(Math.PI/2).translate(0.0, -1.0, zShift).assignTo(cSgc);

sgc.addChild(aSgc)
sgc.addChild(bSgc)
sgc.addChild(cSgc)
sgc.addChild(factorSgc)

sgc.getAppearance().setAttribute(TRANSPARENCY_ENABLED, true)
sgc.getAppearance().setAttribute(TRANSPARENCY, 0.85D)
sgc.getAppearance().setAttribute(EDGE_DRAW, true)

// SceneGraphComponent coordSgc = SceneGraphUtility.createFullSceneGraphComponent("Coordinates")
// CoordinateSystemFactory csf = new CoordinateSystemFactory(coordSgc)

def xShift = -1 * aWidth / 2.0
def yShift = aHeight / 4.0
MatrixBuilder.euclidean().translate(xShift, yShift, 0).assignTo(sgc)

ViewerApp.display(sgc)
