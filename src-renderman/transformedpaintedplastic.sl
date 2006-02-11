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
        vector p=vtransform(textureMatrix, vector (s,t,1));
/*
	printf("\n-%f  %f %f %f\n %f  %f %f %f\n %f  %f %f %f\n %f  %f %f %f\n",
	comp(textureMatrix,0,0),
	comp(textureMatrix,1,0),
	comp(textureMatrix,2,0),
	comp(textureMatrix,3,0),
	comp(textureMatrix,0,1),
	comp(textureMatrix,1,1),
	comp(textureMatrix,2,1),
	comp(textureMatrix,3,1),
	comp(textureMatrix,0,2),
	comp(textureMatrix,1,2),
	comp(textureMatrix,2,2),
	comp(textureMatrix,3,2),
	comp(textureMatrix,0,3),
	comp(textureMatrix,1,3),
	comp(textureMatrix,2,3),
	comp(textureMatrix,3,3)
);
*/
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

