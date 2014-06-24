import bpy
from mathutils import Matrix
from mathutils import Euler
import xml.etree.ElementTree as ET
import math

tagToObject = {}

def parseMatrix(tag):
    mm = [float(mij) for mij in tag.text.split()]
    return Matrix((mm[0:4], mm[4:8], mm[8:12], mm[12:16]))

def parseIndexedFaceSet(tag):
    name = tag.find('name').text;
    me = bpy.data.meshes.new(name)
    # parse vertices
    vertexAttributes = tag.find('vertexAttributes')
    vertexAttributesSize = int(vertexAttributes.get('size'));
    vertexDataText = vertexAttributes.find("DataList[@attribute='coordinates']").text
    vertexDataFloat = [float(vij) for vij in vertexDataText.split()]
    l = int(len(vertexDataFloat) / vertexAttributesSize);
    vertexData = [vertexDataFloat[i*l : i*l+l] for i in range(0, vertexAttributesSize)]
    if l == 4: vertexData = [[vi/v[3] for vi in v[0:3]] for v in vertexData]
    if l == 2: vertexData = [[v[0], v[1], 0.0] for v in vertexData] 
    if l == 1: vertexData = [[v[0], 0.0, 0.0] for v in vertexData]
    # parse edges
    edgeData = []
    edgeAttributes = tag.find('edgeAttributes')
    if edgeAttributes != None:
        edgeAttributesSize = int(edgeAttributes.get('size'));
        if edgeAttributesSize != 0:
            edgeIndexList = edgeAttributes.find("DataList[@attribute='indices']")
            edgeIndexData = edgeIndexList.findall('int-array')
            if len(edgeIndexData) == 0:
                edgeIndexDataInt = [float(eij) for eij in edgeIndexList.text.split()]
                l = int(len(edgeIndexDataInt) / edgeAttributesSize);
                # TODO: blender does not support edge sequences longer than 1
                edgeData = [edgeIndexDataInt[i*l : i*l+l] for i in range(0, edgeAttributesSize)]
            else:    
                edgeData = [[int(index) for index in edgeData.text.split()] for edgeData in edgeIndexData]
    # parse faces
    faceData = []
    faceAttributes = tag.find('faceAttributes')
    if faceAttributes != None:
        faceAttributesSize = int(faceAttributes.get('size'));
        if faceAttributesSize != 0:
            faceIndexData = faceAttributes.find("DataList[@attribute='indices']").findall('int-array')
            faceData = [[int(index) for index in faceData.text.split()] for faceData in faceIndexData]
    # create mesh geometry
    me.from_pydata(vertexData, edgeData, faceData)
    return me

def createGeometry(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    name = tag.find('name');
    if name == None: return
    geom = None
    if tag in tagToObject :
        geom = tagToObject[tag].data
    else :
        if tag.get('type') == 'IndexedFaceSet':
            geom = parseIndexedFaceSet(tag)
        if tag.get('type') == 'IndexedLineSet':
            geom = parseIndexedFaceSet(tag)         
        if tag.get('type') == 'PointSet':
            geom = parseIndexedFaceSet(tag)         
    geomobj = bpy.data.objects.new(name=name.text, object_data = geom)
    geomobj.parent = parentObject
    bpy.context.scene.objects.link(geomobj)
    tagToObject[tag] = geomobj

def createCameraChild(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    if tag.find('name') == None: return
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
    
    
def createObjectFromXML(treeRoot, tag, rootPath, parentObject):
    tag = resolveReference(treeRoot, tag, rootPath);
    name = tag[0].text
    obj = bpy.data.objects.new(name, None)
    if tag.find('visible').text == 'false':
        obj.hide = True
    trafo = tag.find('transformation/matrix')
    if trafo != None:
        obj.matrix_local = parseMatrix(trafo)
    bpy.context.scene.objects.link(obj)
    if parentObject != None :
        obj.parent = parentObject
    createCameraChild(treeRoot, tag.find('camera'), rootPath + '/camera', obj)
    createGeometry(treeRoot, tag.find('geometry'), rootPath + '/geometry', obj);
    counter = 1;
    for child in tag.find("./children"):
        path = rootPath + '/children/child[' + str(counter) + ']'
        counter += 1
        createObjectFromXML(treeRoot, child, path, obj);
    return obj    


def resolveReference(treeRoot, tag, rootPath):
    if 'reference' in tag.attrib :
        refPath = rootPath + '/' + tag.attrib['reference']
        return treeRoot.find(refPath)
    return tag
        
        
def createSceneFromXML(scene_file):
    # parse xml
    sceneTree = ET.parse(scene_file)
    root = sceneTree.getroot()
    # traverse scene xml
    rootObject = createObjectFromXML(root, root[0], './sceneRoot', None)
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