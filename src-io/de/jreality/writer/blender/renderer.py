import bpy
from mathutils import Matrix
import xml.etree.ElementTree as ET
import math

tagToObject = {}

def parseMatrix(tag):
    mm = [float(mij) for mij in tag.text.split()]
    return Matrix((mm[0:4], mm[4:8], mm[8:12], mm[12:16]))

def createCameraChild(treeRoot, objectRoot, rootPath, parentObject):
    if objectRoot == None: pass
    objectRoot = resolveReference(treeRoot, objectRoot, rootPath);
    if objectRoot.find('name') == None: return
    name = objectRoot.find('name').text
    if objectRoot in tagToObject :
        cam = tagToObject[objectRoot].data
    else :
        cam = bpy.data.cameras.new(name)
    cam.clip_start = float(objectRoot.find('near').text);
    cam.clip_end = float(objectRoot.find('far').text);
    cam.angle = math.radians(float(objectRoot.find('fieldOfView').text));
    cam.ortho_scale = float(objectRoot.find('focus').text);
    if objectRoot.find('perspective').text == 'false':
        cam.type = 'ORTHO'
    camobj = bpy.data.objects.new(name=name, object_data = cam)
    trafo = objectRoot.find('orientationMatrix')
    if trafo.text != None:
        camobj.matrix_local = parseMatrix(trafo)
    camobj.parent = parentObject
    bpy.context.scene.objects.link(camobj)
    tagToObject[objectRoot] = camobj
    
    
def createObjectFromXML(treeRoot, objectRoot, rootPath, parentObject):
    objectRoot = resolveReference(treeRoot, objectRoot, rootPath);
    name = objectRoot[0].text
    obj = bpy.data.objects.new(name, None)
    if objectRoot.find('visible').text == 'false':
        obj.hide = True
    trafo = objectRoot.find('transformation/matrix')
    if trafo != None:
        obj.matrix_local = parseMatrix(trafo)
    bpy.context.scene.objects.link(obj)
    if parentObject != None :
        obj.parent = parentObject
    createCameraChild(treeRoot, objectRoot.find('camera'), rootPath + '/camera', obj)
    counter = 1;
    for child in objectRoot.find("./children"):
        path = rootPath + '/children/child[' + str(counter) + ']'
        counter += 1
        createObjectFromXML(treeRoot, child, path, obj);


def resolveReference(treeRoot, objectRoot, rootPath):
    if 'reference' in objectRoot.attrib :
        refPath = rootPath + '/' + objectRoot.attrib['reference']
        return treeRoot.find(refPath)
    return objectRoot
        
        
def createSceneFromXML(scene_file):
    sceneTree = ET.parse(scene_file)
    root = sceneTree.getroot()
    createObjectFromXML(root, root[0], './sceneRoot', None)
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
    print("jreality export finished, exiting")


if __name__ == "__main__":
    main()