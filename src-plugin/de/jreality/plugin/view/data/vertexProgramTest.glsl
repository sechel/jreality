varying vec3 N;
varying vec3 L;
varying vec3 v;           

varying vec2 texCoord0; 
varying vec2 texCoord1; 

void main(void)
{
   vec3 lightPos = gl_LightSource[0].position.xyz;
   v = vec3(gl_ModelViewMatrix * gl_Vertex);
   L = normalize(lightPos - v);
   N = normalize(gl_NormalMatrix * gl_Normal);

   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	texCoord0 = vec2(gl_MultiTexCoord0);
	texCoord1 = vec2(gl_MultiTexCoord1);
}
             