/* scaledpaintedplastic.sl - like standard paintedplastic but has  s and t scale
 * PARAMETERS:
 *    Ka, Kd, Ks, roughness, specularcolor - the usual meaning.
 *    texturename - the name of the texture file.
 *    sScale ', tScale
 *
 */


surface
transformedpaintedplastic ( float Ka = 1, Kd = .5, Ks = .5, roughness = .1;
     		 matrix textureMatrix = 1;
		 color specularcolor = 1;
		 string texturename = ""; 
		 float useTextureAlpha = 0;)
{
  normal Nf;
  vector V;
  color Ct;
  float tr = 1;

  if (texturename != ""){
	float ss = s;
	point a = point (s,t,0);
	point b = vtransform("world","object",a);
	point p = transform( textureMatrix , b);

/*	printf("%p -> %p -> %p\n\n%m\n\n",a,b,p,textureMatrix);*/
	
       Ct = color texture (texturename,(xcomp(p)),(ycomp(p)));
/*
        float cnum;
	if(1.0 == textureinfo(texturename ,"channels", cnum)) {
		if(cnum>=4)
		       tr = texture(texturename [3],(xcomp(p)),(ycomp(p)));
	}
*/ 
	if(useTextureAlpha>0)
	   tr = texture(texturename [3],(xcomp(p)),(ycomp(p)));
  }
  else Ct = 1;

  Nf = faceforward (normalize(N),I);
  V = -normalize(I);
  Oi = Os*tr;

/*
  Ci = Oi * ( Ct * (Ka*ambient() + Kd*diffuse(Nf)) +
	      specularcolor * Ks*specular(Nf,V,roughness));
*/
  Ci = Oi * ( Cs * Ct * (Ka*ambient() + Kd*diffuse(Nf)) +
	      specularcolor * Ks*specular(Nf,V,roughness));

}

