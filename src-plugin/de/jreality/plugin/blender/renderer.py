import bpy
import xml.etree.ElementTree as ET
import xml.sax as SAX

objectTags = ['sceneRoot', 'child', 'camera']

def example_function(scene_file, save_path, render_path):
    
    scene = bpy.context.scene
    
    # Clear existing objects.
    scene.camera = None
    for obj in scene.objects:
        scene.objects.unlink(obj)
    
    create_scene(scene_file)
    
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

def create_scene(scene_file):
    parser = SceneParser()
    SAX.parse(scene_file, parser)
    bpy.context.scene.update()
    # Default Camera
    if not parser.activeCamera == None :
        bpy.context.scene.camera = parser.activeCamera
    else :
        cam_data = bpy.data.cameras.new('Default Camera')
        cam_ob = bpy.data.objects.new(name='Default Camera', object_data=cam_data)
        bpy.context.scene.objects.link(cam_ob)  # instance the camera object in the scene
        bpy.context.scene.camera = cam_ob       # set the active camera
        cam_ob.location = 0.0, 0.0, 10.0

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

    # Run the example function
    example_function(args.scene_path, args.save_path, args.render_path)
                                                                
    print("batch job finished, exiting")


if __name__ == "__main__":
    main()