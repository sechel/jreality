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
    float tm[16] = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
		 color specularcolor = 1;
		 string texturename = ""; 
		 string reflectionmap = ""; 
         float lighting = 1;
		 )
{
  normal Nf;
  vector V, D;
  color Ct, Cr;
  float tr = 1;
  float debug = 0;
  matrix textureMatrix = matrix "current" (tm[0],tm[1],tm[2],tm[3],tm[4],tm[5],tm[6],tm[7],tm[8],tm[9],tm[10],tm[11],tm[12],tm[13],tm[14],tm[15]);

  // evaluate the texture map, if any
  if (texturename != ""){
	point a = point (s,t,0);
    // RenderMan automatically converts the textureMatrix into "current" space
    // so the point has also to be transformed into that space!
	//point b = transform("shader", "current",a); 	 
	point p = transform( textureMatrix , a);
	float ss = xcomp(p);
	float tt = ycomp(p);
    if (debug != 0) printf("texture coords are %f %f",ss,tt);
	
	tr = float texture(texturename[3],ss,tt, "fill",1);
    Ct = color texture (texturename,ss, tt);
    Ct = Cs * Ct; 
  }
  else Ct = Cs;

    Cr = 0;
    // evaluate the reflection map, if any

    // modulate the opacity by the alpha channel of the texture
    Oi = Os*tr;

    if (lighting != 0)  {
        Nf = faceforward (normalize(N),I);
        V = -normalize(I);
        // calculate the diffuse component
        Ct = Ct * (Ka*ambient() + Kd*diffuse(Nf)) ;
    }


    // and add in the reflection map (without modulating by shading)
    if (reflectionmap != "") {
        D = reflect(I, Nf) ;
        D = vtransform ("world", D);
	    Ct = (1-reflectionBlend) * Ct + reflectionBlend * color environment(reflectionmap, D);
    } 

    // the surface color is a sum of ambient, diffuse, specular
    if (lighting != 0)
        Ci = Oi * ( Ct + specularcolor * Ks*specular(Nf,V,roughness) );
    else 
        Ci = Ct;
}

