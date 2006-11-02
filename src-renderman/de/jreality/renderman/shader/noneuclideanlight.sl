/* noneuclideanlight.sl:	shader for point lights in hyperbolic space
 * Author:	Charlie Gunn
 * Description: 
 *	See hyperbolicpolygonshader.sl also. 
 *	This simulates a point light in hyperbolic space.  The brightness of the light
 *	decays exponentially since the surface area of the sphere of radius r in hyperbolic
 *	space is exp(kr) for some constant k.    Distance is given by Arccosh({p1,p1}),
 *	where {p1,p0} is the Minkowski inner product on projective space.
 */


light
noneuclideanlight(
	float atten1 = 1.0, atten2 = 2.0,
	intensity = 1;
	color lightcolor = 1;
    point from = point "current" (0,0,0);	/* light position */
    float signature = -1.0;
	)
{
	Cl = 0;
    if (signature == 0) {
        illuminate( from )
	    Cl = intensity * lightcolor / L.L;
    } else {
        vector  fromV = from;
        uniform float debug = 1.0;
        float broken = 0;
        color brokeC = 0;
        vector sP = Ps; 
	    float PP =  1.0 + signature* sP.sP;
        if (debug != 0.0 && PP < 0.0)   { //debug != 0.0)
            brokeC = color(1,0,0);
            broken = 1;
            printf("point has norm squared %f\n",PP);
        } 

	    illuminate(from)	{
		    float QQ =  1.0 + signature * fromV.fromV;
            //if (debug != 0.0) printf("Light has norm squared %f\n",PP);
		    if (QQ <= 0 ) {
                if (debug != 0.0) {
                    brokeC = color(0,1,0);
                    broken = 1;
                    printf("point has norm squared %f\n",QQ);
                } 
                //else QQ = 0;       // treat this as a distant light: no attenuation
            }
		    float PQ =  1.0 + signature * sP.fromV;
		    //if (PQ <= 0 ) PQ = 0; 
	    	
		    float d = PP * QQ;
		    if (d > 0) {
		        float inpro = PQ / sqrt (d);
                float t;
                if (signature == -1) {
		            // can't do arccosh directly in shading language 
                    //but can do it using log & sqrt 
		            float hdis = log(inpro + sqrt((abs(inpro*inpro - 1))));
                    t = atten2*exp(-atten1*hdis);
                }
                else {
                    float hdis = acos(inpro);
                    t = atten2 * abs(sin(hdis)); 
                }
		        Cl += intensity * lightcolor * t;
		    } else
                Cl += intensity * lightcolor; 
		}
        if (broken != 0.0) {
            Cl = brokeC;
            printf("Broken color\n");
        }
    }
}


