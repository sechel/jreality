//
// Fragment shader for jReality tutorial
//
// Authors: Charles Gunn

uniform sampler2D  sampler;

void main(void)
{
	//following code mimics the standard shader with MODULATE apply mode.  
	// To prove we are executing
	// this fragment shader, we ignore the input color
    //gl_FragColor = gl_Color * texture2D(sampler, gl_TexCoord[0].st);
    gl_FragColor = texture2D(sampler, gl_TexCoord[0].st);
}
