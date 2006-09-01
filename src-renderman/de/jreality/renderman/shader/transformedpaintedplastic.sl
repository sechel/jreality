/* scaledpaintedplastic.sl - like standard paintedplastic 
 * PARAMETERS:
 *    Ka, Kd, Ks, roughness, specularcolor - the usual meaning.
 *    Kr: strength of reflection map
 *    texturename - the name of the texture file.
 *
 * Author: Hoffmann, Gunn
 */


surface
transformedpaintedplastic ( float Ka = 1, Kd = .5, Ks = .5, Kr = .5, roughness = .1, reflectionBlend = .6;
     	 matrix textureMatrix = 1;
		 color specularcolor = 1;
		 string texturename = ""; 
		 string reflectionmap = ""; 
		 )
{
  normal Nf;
  vector V, D;
  color Ct, Cr;
  float tr = 1;
  float debug = 0;

    Nf = faceforward (normalize(N),I);
    D = reflect(I, Nf) ;
    D = vtransform ("world", D);
    V = -normalize(I);

  // evaluate the texture map, if any
  if (texturename != ""){
	point a = point (s,t,0);
    // RenderMan automatically converts the textureMatrix into "current" space
    // so the point has also to be transformed into that space!
	point b = transform("shader", "current",a); 	 
	point p = transform( textureMatrix , b);
	float ss = xcomp(p);
	float tt = ycomp(p);
	
	tr = float texture(texturename[3],ss,tt, "fill",1);
    Ct = color texture (texturename,ss, tt);
    Ct = Cs * Ct; 
  }
  else Ct = Cs;

    Cr = 0;
    // evaluate the reflection map, if any

    // modulate the opacity by the alpha channel of the texture
    Oi = Os*tr;

    // calculate the diffuse component
    Ct = Ct * (Ka*ambient() + Kd*diffuse(Nf)) ;

    // and add in the reflection map (without modulating by shading)
    if (reflectionmap != "") {
	    Ct = (1-reflectionBlend) * Ct + reflectionBlend * color environment(reflectionmap, D);
    } 

    // the surface color is a sum of ambient, diffuse, specular
    Ci = Oi * ( Ct + specularcolor * Ks*specular(Nf,V,roughness) );
}

