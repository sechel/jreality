import bpy
from mathutils import Matrix
from mathutils import Euler
import xml.etree.ElementTree as ET
import math

tagToObject = {}
materialStack = []

def parseMatrix(tag):
    mm = [float(mij) for mij in tag.text.split()]
    return Matrix((mm[0:4], mm[4:8], mm[8:12], mm[12:16]))


def parseColor(tag):
    r = float(tag.find('red').text) / 255.0
    g = float(tag.find('green').text) / 255.0
    b = float(tag.find('blue').text) / 255.0
    return [r, g, b]

def parseGeometryAttribute(tag, count, isFloat, normalizeTo3Components):
    if tag is None: return []
    if isFloat: dataTags = tag.findall('double-array')
    else: dataTags = tag.findall('int-array')
    if dataTags:
        if isFloat:
            data = [[float(f) for f in dataTag.text.split()] for dataTag in dataTags]
        else:
            data = [[int(i) for i in dataTag.text.split()] for dataTag in dataTags]    
    else:
        if isFloat:
            dataInline = [float(f) for f in tag.text.split()]
        else:
            dataInline = [int(i) for i in tag.text.split()]    
        l = int(len(dataInline) / count);  
        data = [dataInline[i*l : i*l+l] for i in range(0, count)]
        if normalizeTo3Components:
            if l > 3: data = [[vi/vec[-1] for vi in vec[0:3]] for vec in data]
            if l == 2: data = [[vec[0], vec[1], 0.0] for vec in data] 
            if l == 1: data = [[vec[0], 0.0, 0.0] for vec in data]
    return data    


def createMesh(tag):
    name = tag.find('name').text;
    mesh = bpy.data.meshes.new(name)
    # parse vertices
    vertexAttributes = tag.find('vertexAttributes')
    vertexAttributesSize = int(vertexAttributes.get('size'));
    vertexDataTag = vertexAttributes.find("DataList[@attribute='coordinates']")
    vertexData = parseGeometryAttribute(vertexDataTag, vertexAttributesSize, True, True)
    # parse edges
    edgeData = []
    edgeAttributes = tag.find('edgeAttributes')
    if edgeAttributes is not None:
        edgeAttributesSize = int(edgeAttributes.get('size'));
        edgeDataTag = edgeAttributes.find("DataList[@attribute='indices']")
        edgeData = parseGeometryAttribute(edgeDataTag, edgeAttributesSize, False, False)
    # parse faces
    faceData = []
    faceAttributes = tag.find('faceAttributes')
    if faceAttributes is not None:
        faceAttributesSize = int(faceAttributes.get('size'));
        faceDataTag = tag.find("faceAttributes/DataList[@attribute='indices']")
        faceData = parseGeometryAttribute(faceDataTag, faceAttributesSize, False, False)
    # create mesh
    mesh.from_pydata(vertexData, edgeData, faceData)
    
    # vertex colors
    vertexColorsTag = vertexAttributes.find("DataList[@attribute='colors']")
    if vertexColorsTag is not None:
        vertexColors = parseGeometryAttribute(vertexColorsTag, vertexAttributesSize, True, True)
        colorLayer = mesh.vertex_colors.new(name='Vertex Colors')
        colorIndex = 0
        for poly in mesh.polygons:
            for vertexIndex in poly.vertices:
                colorLayer.data[colorIndex].color = vertexColors[vertexIndex]
                colorIndex += 1
                
    # face colors
    faceColorsTag = tag.find("faceAttributes/DataList[@attribute='colors']")
    if faceColorsTag is not None:
        faceColors = parseGeometryAttribute(faceColorsTag, faceAttributesSize, True, True)
        colorLayer = mesh.vertex_colors.new(name='Face Colors')
        colorIndex = 0
        faceIndex = 0
        for poly in mesh.polygons:
            for vertexIndex in poly.vertices:
                colorLayer.data[colorIndex].color = faceColors[faceIndex]
                colorIndex += 1 
            faceIndex += 1                   
    return mesh


def createMaterial(treeRoot, tag, rootPath, parentMaterial, geometryObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    nameTag = tag.find('name');
    mesh = None if geometryObject is None else geometryObject.data
    vertex_colors = None if mesh is None else mesh.vertex_colors
    if nameTag is None:
        if geometryObject is None or not vertex_colors:
            return None
        else:
            name = "Vertex Paint Material"
    else: 
        name = nameTag.text
    material = bpy.data.materials.new(name)
    material.use_vertex_color_paint = bool(vertex_colors)
    # diffuse color
    diffuseColorTag = tag.find("attribute[@name='polygonShader.diffuseColor']")
    if diffuseColorTag is not None: 
        material.diffuse_color = parseColor(diffuseColorTag.find('awt-color'))
    else: 
        material.diffuse_color = parentMaterial.diffuse_color
    # smooth/flat shading
    smoothShadingTag = tag.find("attribute[@name='polygonShader.smoothShading']")
    if smoothShadingTag is not None:
        smoothShading = smoothShadingTag.find('boolean').text == 'true'
    else:
        smoothShading = parentMaterial['smoothShading']
    material['smoothShading'] = smoothShading        
    if vertex_colors is not None:    
        # TODO: does not work correctly with shared geometry
        if 'Vertex Colors' in vertex_colors:
            vertex_colors['Vertex Colors'].active = smoothShading
            vertex_colors['Vertex Colors'].active_render = smoothShading
        if 'Face Colors' in vertex_colors:
            vertex_colors['Face Colors'].active = not smoothShading
            vertex_colors['Face Colors'].active_render = not smoothShading 
    return material


def createGeometry(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    name = tag.find('name');
    if name == None: return None
    geom = None
    if tag in tagToObject: 
        geom = tagToObject[tag].data
    else:
        geom = createMesh(tag)
    geomobj = bpy.data.objects.new(name=name.text, object_data = geom)
    bpy.context.scene.objects.link(geomobj)
    geomobj.parent = parentObject
    tagToObject[tag] = geomobj
    return geomobj


def createCamera(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    if tag.find('name') == None: return None
    name = tag.find('name').text
    if tag in tagToObject :
        cam = tagToObject[tag].data
    else :
        cam = bpy.data.cameras.new(name)
        cam.clip_start = float(tag.find('near').text);
        cam.clip_end = float(tag.find('far').text);
        cam.angle = math.radians(float(tag.find('fieldOfView').text));
        cam.ortho_scale = float(tag.find('focus').text);
        if tag.find('perspective').text == 'false':
            cam.type = 'ORTHO'
    camobj = bpy.data.objects.new(name=name, object_data = cam)
    trafo = tag.find('orientationMatrix')
    if trafo.text != None:
        camobj.matrix_local = parseMatrix(trafo)
    camobj.parent = parentObject
    bpy.context.scene.objects.link(camobj)
    tagToObject[tag] = camobj
    return camobj


def createLight(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    if tag.find('name') == None: return None
    name = tag.find('name').text
    type = tag.get('type')
    if tag in tagToObject :
        light = tagToObject[tag].data
    else :
        blenderType = 'POINT'
        if type == 'PointLight': 
            light = bpy.data.lamps.new(name, 'POINT')
        elif len(tag.findall('coneAngle')) != 0: 
            light = bpy.data.lamps.new(name, 'SPOT')
            light.spot_size = float(tag.find('coneAngle').text)
            light.show_cone = True
        elif type == 'DirectionalLight':
            light = bpy.data.lamps.new(name, 'HEMI')     
        else:
            light = bpy.data.lamps.new(name, 'POINT')
        light.color = parseColor(tag.find('color'))
        light.energy = float(tag.find('intensity').text)
    lightobj = bpy.data.objects.new(name=name, object_data = light)
    lightobj.parent = parentObject
    bpy.context.scene.objects.link(lightobj)
    tagToObject[tag] = lightobj
    return lightobj

    
def createObjectFromXML(treeRoot, tag, rootPath, parentObject, visible):
    tag = resolveReference(treeRoot, tag, rootPath);
    name = tag[0].text
    obj = bpy.data.objects.new(name, None)
    if not visible:
        obj.hide = True
    else: 
        visible = tag.find('visible').text == 'true'
        obj.hide = not visible
    trafo = tag.find('transformation/matrix')
    if trafo != None:
        obj.matrix_local = parseMatrix(trafo)
    bpy.context.scene.objects.link(obj)
    if parentObject != None :
        obj.parent = parentObject
    camera = createCamera(treeRoot, tag.find('camera'), rootPath + '/camera', obj)
    geometry = createGeometry(treeRoot, tag.find('geometry'), rootPath + '/geometry', obj);
    light = createLight(treeRoot, tag.find('light'), rootPath + '/light', obj);
    material = createMaterial(treeRoot, tag.find('appearance'), rootPath + '/appearance', materialStack[-1], geometry);
    if material is not None: materialStack.append(material)
    if geometry is not None: 
        geometry.hide = obj.hide
        effectiveMaterial = materialStack[-1]
        # do not set twice for multiple occurrences
        geometry.data.materials.append(effectiveMaterial)
        geometry.material_slots[0].link = 'OBJECT'
        geometry.material_slots[0].material = effectiveMaterial
        if len(geometry.data.materials) > 1: geometry.data.materials.pop()
        geometry.data.materials[0] = None
    counter = 1;
    for child in tag.find("./children"):
        path = rootPath + '/children/child[' + str(counter) + ']'
        counter += 1
        createObjectFromXML(treeRoot, child, path, obj, visible);
    if material is not None: materialStack.pop()  
    return obj    


def resolveReference(treeRoot, tag, rootPath):
    if 'reference' in tag.attrib :
        refPath = rootPath + '/' + tag.attrib['reference']
        return treeRoot.find(refPath)
    return tag
        
def createDefaultMaterial():
    mtl = bpy.data.materials[0]
    mtl.name = 'JReality Default Material'
    mtl.diffuse_color = [0, 0, 1]
    mtl['smoothShading'] = True
    return mtl
        
        
def createSceneFromXML(scene_file):
    # parse xml
    sceneTree = ET.parse(scene_file)
    root = sceneTree.getroot()
    # traverse scene xml
    materialStack.append(createDefaultMaterial())
    rootObject = createObjectFromXML(root, root[0], './sceneRoot', None, True)
    # create coordinate conversion root
    sceneObject = bpy.data.objects.new('JReality Scene', None)
    jrealityToBlender = Euler((math.pi/2, 0.0, math.pi/2), 'XYZ')
    sceneObject.matrix_local = jrealityToBlender.to_matrix().to_4x4()
    bpy.context.scene.objects.link(sceneObject)
    rootObject.parent = sceneObject;
    # find active camera
    cameraPath = root.find("scenePaths/path[@name='cameraPath']")
    if cameraPath != None:
        node = cameraPath.find('node[last()]')
        camTag = resolveReference(root, node, "./scenePaths/path[@name='cameraPath']/node[last()]")
        bpy.context.scene.camera = tagToObject[camTag]
    else:
        print('WARNING: no camera path set')  
        

def readJRealityScene(scene_file, save_path, render_path):
    scene = bpy.context.scene
    # Clear existing objects.
    scene.camera = None
    for obj in scene.objects:
        scene.objects.unlink(obj)
    createSceneFromXML(scene_file)
    if save_path:
        try:
            f = open(save_path, 'w')
            f.close()
            ok = True
        except:
            print("Cannot save to path %r" % save_path)
            
            import traceback
            traceback.print_exc()
        
        if ok:
            bpy.ops.wm.save_as_mainfile(filepath=save_path)
    if render_path:
        render = scene.render
        render.use_file_extension = True
        render.filepath = render_path
        bpy.ops.render.render(write_still=True)


def main():
    import sys
    import argparse
    argv = sys.argv
    if "--" not in argv:
        argv = []
    else:
        argv = argv[argv.index("--") + 1:] 
    usage_text = \
    "Run blender in background mode with this script:"
    "  blender --background --python " + __file__ + " -- [options]"
    parser = argparse.ArgumentParser(description=usage_text)
    parser.add_argument("-s", "--save", dest="save_path", metavar='FILE', help="Save the generated file to the specified path")
    parser.add_argument("-r", "--render", dest="render_path", metavar='FILE', help="Render an image to the specified path")
    parser.add_argument("-f", "--file", dest="scene_path", metavar='FILE', help="Render the specified scene")
    args = parser.parse_args(argv)  # In this example we wont use the args
    if not argv:
        parser.print_help()
        return
    readJRealityScene(args.scene_path, args.save_path, args.render_path)


if __name__ == "__main__":
    main()