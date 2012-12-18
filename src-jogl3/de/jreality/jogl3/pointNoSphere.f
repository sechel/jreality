//author Benjamin Kutschan
//default point shader, no spheres draw, fragment
#version 330

uniform mat4 projection;

uniform sampler2D tex;

out vec4 gl_FragColor;
uniform vec4 diffuseColor;

in float x;
in float y;
in float z;
in float w;
in float screenPortion;

vec3 light = vec3(0.57735, 0.57735, 0.57735);

void main(void)
{
	//pixel color from texture
	vec4 tex = texture( tex, gl_PointCoord);
	
	//depth calculations:
	//transform coordinates to the unit circle
	vec2 coordsxy = 2*vec2(gl_PointCoord.x-.5, gl_PointCoord.y-.5);
	//calculate the distance from the origin
	float radius = length(coordsxy);
	//z coordinate in the unit sphere
	float coordsz = sqrt(1-radius*radius);
	float shade2 = dot(vec3(coordsxy, coordsz), light);
	if(radius >= 1){
		discard;
	}else{
		//float offset = sqrt(1-radius*radius);
		float omega = 0.5*coordsz*screenPortion*z;
		vec4 windowCoords = projection * vec4(x, y, z-omega, w);
		gl_FragDepth = 0.5+0.5*windowCoords.z/windowCoords.w;
	}
	
	//either this texture lookup method
	float shade = tex.b;
	if(shade < .5){
		float red = diffuseColor.r*2*shade;
		float green = diffuseColor.g*2*shade;
		float blue = diffuseColor.b*2*shade;
		gl_FragColor = vec4(red, green, blue, tex.a);
	}else{
		shade = (shade-.5)*2;
		float red = diffuseColor.r + shade - diffuseColor.r*shade;
		float green = diffuseColor.g + shade - diffuseColor.g*shade;
		float blue = diffuseColor.b + shade - diffuseColor.b*shade;
		gl_FragColor = vec4(red, green, blue, tex.a);
	}
	
	//or using the normal directly
	//gl_FragColor = vec4((shade2*diffuseColor).rgb, 1);
	//gl_FragDepth = .99;//gl_FragCoord.z;
	//gl_FragColor = vec4(1, 0, 0, 1);
}