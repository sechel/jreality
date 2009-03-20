//
// Fragment shader for jReality tutorial
//
// Authors: Charles Gunn

uniform sampler2D  sampler;
uniform sampler2D sampler2;
void main(void)
{
    vec4 currentSample = texture2D(sampler,gl_TexCoord[0].st); 
    vec4 currentSample2 = texture2D(sampler2,gl_TexCoord[1].st); 
    vec4 yellow = vec4(1,1,0,1);
    gl_FragColor = .5 *( currentSample * yellow + currentSample2 * gl_Color); 
}
