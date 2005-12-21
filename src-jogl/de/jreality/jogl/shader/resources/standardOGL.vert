//uniform bool LocalViewer ;
//uniform bool SeparateSpecular ;
uniform float shininess;
uniform vec4 diffuseColor;
uniform vec4 ambientColor;
uniform vec4 specularColor;
uniform float ambientCoefficient;
uniform float diffuseCoefficient;
uniform float specularCoefficient;

void Light(in int i,
					in vec3 surfaceToCameraVector,
					in vec3 surfaceCameraCoordinates,
                    in vec3 normal,
                    inout vec4 ambient,
                    inout vec4 diffuse,
                    inout vec4 specular)
{
    float nDotVP;
    float nDotHV;
    float pf;
    float spotDot;
    float spotAttenuation;
    float attenuation=1;
    float d;
    vec3 surfaceToLightVector;
    vec3 halfVector;
    vec4 lp4;
    vec4 hv;
   lp4 = gl_LightSource[i].position;
   hv = gl_LightSource[i].halfVector;
   if (lp4.w == 0.0)	{
     	nDotVP = max(0.0, dot(normal, normalize(vec3 (lp4))));
    	nDotHV = max(0.0, dot(normal, normalize(vec3 (hv))));
     } else {
	     // compute vector from surface to light position
	     vec3 lp = (vec3 (lp4))/lp4.w;
	    surfaceToLightVector = lp - surfaceCameraCoordinates;
		d = length(surfaceToLightVector);
		surfaceToLightVector = normalize(surfaceToLightVector);
	
		attenuation = 1.0 / (gl_LightSource[i].constantAttenuation +
					gl_LightSource[i].linearAttenuation*d+
					gl_LightSource[i].quadraticAttenuation*d*d);
					
		if (gl_LightSource[i].spotCutoff != 180.0)	{
			spotDot = dot(-surfaceToLightVector, gl_LightSource[i].spotDirection);
			if (spotDot >= gl_LightSource[i].spotCosCutoff)
				attenuation  *= pow(spotDot, gl_LightSource[i].spotExponent);
		}
		
		halfVector = normalize(surfaceToLightVector+surfaceToCameraVector);
	
	    nDotVP = max(0.0, (dot(normal, surfaceToLightVector)));
	    nDotHV = max(0.0, (dot(normal, halfVector))); 
   }
   	if (nDotVP == 0.0 )	pf = 0.0;
    else  					
    	pf = pow(nDotHV, shininess);
    ambient += gl_LightSource[i].ambient ;
    diffuse += gl_LightSource[i].diffuse * nDotVP * attenuation;
    specular += gl_LightSource[i].specular * pf *attenuation;
}

void doLighting(in vec3 normal, in vec3 surfaceToCameraVector, in vec3 surfaceCameraCoordinates, in bool front, inout vec4 color) {
     bool SeparateSpecular = true;	// false results in funny problems that look like color clamping errors
    int NumEnabledLights = 2;
    // do the lighting calculations
    vec4 amb = vec4(0.0);
    vec4 diff = vec4(0.0);
    vec4 spec = vec4(0.0);
    // loop through lights
    int i;
    for (i = 0; i<NumEnabledLights; ++i)    {
 		Light(i, surfaceToCameraVector, surfaceCameraCoordinates, normal, amb, diff, spec);
    }
    if (front)	{
 	   color += gl_FrontLightModelProduct.sceneColor +
 	       ambientCoefficient*amb*ambientColor+
 	       diffuseCoefficient*diff * diffuseColor; //gl_FrontMaterial.diffuse;
    } else {
 	   color += gl_BackLightModelProduct.sceneColor +
 	       ambientCoefficient*amb*ambientColor+
 	       diffuseCoefficient*diff * diffuseColor; //gl_FrontMaterial.diffuse;
    }    
 
    if (SeparateSpecular)
        if (front) gl_FrontSecondaryColor = specularCoefficient*spec * specularColor;
        else gl_BackSecondaryColor = specularCoefficient*spec * specularColor;
    else 
        color += specularCoefficient*spec * specularColor;
}

void main(void)
{
    vec3 surfaceCameraCoordinates, surfaceToCameraVector;
    bool LocalViewer = true;
   
    gl_Position = ftransform();
    vec4 eye = gl_ModelViewMatrix * gl_Vertex;
   	surfaceCameraCoordinates = (vec3 (eye))/eye.w;
    if (LocalViewer)		{  //gl_Position.w != 0.0) {
         surfaceToCameraVector = -normalize(surfaceCameraCoordinates);
    }
    else  {
         surfaceToCameraVector = vec3 (0.0, 0.0, 1.0);
    }
     
    vec3 normal = gl_NormalMatrix * gl_Normal;
    normal = normalize(normal);
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    doLighting(normal, surfaceToCameraVector, surfaceCameraCoordinates, true, gl_FrontColor);
    normal = normal;
    doLighting(normal, surfaceToCameraVector, surfaceCameraCoordinates, false, gl_BackColor);

}
