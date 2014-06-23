import bpy
import xml.etree.ElementTree as ET
import xml.sax as SAX
from _ast import For

objectTags = ['sceneRoot', 'child', 'camera']

class SceneParser(SAX.ContentHandler):
    def __init__(self):
        SAX.ContentHandler.__init__(self)
        self.objectStack = []
        self.nameStack = []
        self.activeCamera = None

    def startElement(self, name, attrs):
        self.nameStack.append(name)
        if name == 'name':
            parentName = self.nameStack[-2]
            obj = None
            if 'sceneRoot' == parentName:
                obj = bpy.data.objects.new('Scene Root', None)
            if 'child' == parentName:
                obj = bpy.data.objects.new('Scene Component', None)
            if 'camera' == parentName:
                cam = bpy.data.cameras.new('Camera')
                obj = bpy.data.objects.new(name='Camera Component', object_data=cam)
                self.activeCamera = obj
            if not obj == None:
                self.objectStack.append(obj);
                bpy.context.scene.objects.link(obj)
                if len(self.objectStack) > 1:
                    obj.parent = self.objectStack[-2]

    def endElement(self, name):
        self.nameStack.pop()
        if 'name' == name:
            parentName = self.nameStack[-2]
            if parentName in objectTags:
                self.objectStack.pop()

    def characters(self, content):
        if len(self.nameStack) >= 2 \
        and self.nameStack[-2] in objectTags \
        and self.nameStack[-1] == 'name':
            obj = self.objectStack[-1]
            obj.name = content

def createCameraChild(treeRoot, objectRoot, rootPath, parentObject):
    if objectRoot == None: pass
    objectRoot = resolveReference(treeRoot, objectRoot, rootPath);
    if objectRoot.find('name') == None: pass
    name = objectRoot[0].text
    cam = bpy.data.cameras.new(name)
    camobj = bpy.data.objects.new(name=name, object_data = cam)
    camobj.parent = parentObject
    bpy.context.scene.objects.link(camobj)
    
def createObjectFromXML(treeRoot, objectRoot, rootPath, parentObject):
    objectRoot = resolveReference(treeRoot, objectRoot, rootPath);
    name = objectRoot[0].text
    obj = bpy.data.objects.new(name, None)
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
        print(refPath)
        return treeRoot.find(refPath)
    return objectRoot
        
def createSceneFromXML(scene_file):
    sceneTree = ET.parse(scene_file)
    root = sceneTree.getroot()
    createObjectFromXML(root, root[0], './sceneRoot', None)

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
    import sys       # to get command line args
    import argparse  # to parse options for us and print a nice help message
    
    # get the args passed to blender after "--", all of which are ignored by
    # blender so scripts may receive their own arguments
    argv = sys.argv
    
    if "--" not in argv:
        argv = []  # as if no args are passed
    else:
        argv = argv[argv.index("--") + 1:]  # get all args after "--"
    
    # When --help or no args are given, print this help
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
                                                                
    print("batch job finished, exiting")

if __name__ == "__main__":
    main()