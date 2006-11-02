/* 
 *  noneuclideanpolygonshader.sl:	a generic surface shader for noneuclidean spaces
 *  Author:		Charles Gunn
 *  Description:
 *	The parameters to this shader are the same as the parameters to the regular plastic
 *	shader, but this shader computes angles and distances using the hyperbolic metric.
 *   	This metric is defined on homogeneous coordinates (x,y,z,w) and is induced by the 
 *	quadratic form x*x + y*y +z*z - w*w.
 *	This metric is valid on the interior of the unit ball; the isometries of this metric
 *	are projective transformations that preserve the unit sphere and map the interior to 
 *	itself.  These are represented by 4x4 matrices hence can be implemented using the 
 *	regular geometry/viewing pipeline provided by Renderman (and other rendering systems).
 *  Features:
 *	It would be much easier to implement if there were more general datatypes in the 
 *	shading language.  I mean if I could use 4-tuples as points instead of just 3-tuples
 *	the code would be much more compact.  As it is, I have to drag around a "w" coordinate
 *	for each point through all the computations. Ugly.
 */


    void
    matrixTimesVector(output float dst[4] ; float m[16] ; float v[4])  {
       dst[0] = m[0]*v[0]+m[4]*v[1]+m[8]*v[2]+m[12]*v[3];
       dst[1] = m[1]*v[0]+m[5]*v[1]+m[9]*v[2]+m[13]*v[3];
       dst[2] = m[2]*v[0]+m[6]*v[1]+m[10]*v[2]+m[14]*v[3];
       dst[3] = m[3]*v[0]+m[7]*v[1]+m[11]*v[2]+m[15]*v[3];
    }

    void
    linearCombination(output float a[4]; float v[4]; float t; float w[4])  {
        a[0] = v[0] + t * w[0];
        a[1] = v[1] + t * w[1];
        a[2] = v[2] + t * w[2];
        a[3] = v[3] + t * w[3];
    }

    void
    times(output float a[4];  float t; float b[4])  {
        a[0] =  t * b[0];
        a[1] =  t * b[1];
        a[2] =  t * b[2];
        a[3] =  t * b[3];
    }

surface
noneuclideanpolygonshader ( 
    varying float Nw[4] = {0,0,0,1};
    float Ka = 0, 
	    Kd = .5, 
	    Ks = .5, 
	    roughness = .1, 
	reflectionBlend = .6;
    uniform float objectToCamera[16] = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    uniform float tm[16] = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
	color specularcolor = 1;
	string texturename = ""; 
	string reflectionmap = ""; 
    float lighting = 1,
        signature = -1;         // shader should also work for elliptic case (+1)
	)
{
	color	total;
    color Ct, Cr;
    float tr = 1;
    float Pa[4] = {xcomp(P), ycomp(P), zcomp(P), 1};

	float 
	dot(float v1[4], v2[4]; ) {
        extern uniform float signature;
	    return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2] + signature *  v1[3]*v2[3];
	}
	
	void 
	hnormalize(output float v[4])   {
	    float t = dot(v,v);
	    t = 1.0/sqrt(abs(t));
        times(v,t,v);
	}

	void 
    hmaketangent(output float v[4]) {
        extern float Pa[4];
        extern uniform float signature;
        float t = -signature*dot(v, Pa);
        linearCombination(v,v,t,Pa);
    }

    float PP = dot(Pa,Pa);
    //printf("P.P = %f\n",PP);
	/* handle case that hyperbolic point lies outside unit ball */
	if (signature == -1 && PP > 0)  {
		Ci = color(1,0,0);      // draw it in red: debug mode probably 
		Oi = 1.0;
		}

	else {
        if (texturename != ""){
            matrix textureMatrix = matrix "current" (tm[0],tm[1],tm[2],tm[3],tm[4],tm[5],tm[6],tm[7],tm[8],tm[9],tm[10],tm[11],tm[12],tm[13],tm[14],tm[15]);
	        point a = point (s,t,0);
	        //point b = transform("shader", "current",a); 	 
	        //point p = transform( textureMatrix , a);
            //printf("tex matrix: %m\n",textureMatrix);
            //point p = a;
	        point p = transform( textureMatrix , a);
	        float ss = xcomp(p);
	        float tt = ycomp(p);
    	
	        tr = float texture(texturename[3],ss,tt, "fill",1);
            Ct = color texture (texturename,ss, tt);
            Ct = Cs * Ct; 
        }
        else Ct = Cs;
        Oi = Os*tr;

        // normalize the point
        float Pw = 1.0/sqrt(abs(PP));
        times(Pa, Pw, Pa);
        //printf("P.P = %f\n",dot(Pa,Pa));

        // calculate the normal
        float tN[4];
        matrixTimesVector(tN, objectToCamera, Nw);
        hmaketangent(tN);
        hnormalize(tN);
        //printf("N.P = %f\n",dot(Nh,Ph,Nw3,Pw));
    
	    /* as a difference vector, I has w cord = 0 */
	    /* also, we want the L which points at the eye, not the surface */
        float Ia[4] = {-xcomp(I), -ycomp(I), -zcomp(I), 1};
        hmaketangent(Ia);
        hnormalize(Ia);

        float ndi = dot(Ia,tN);
        // flip the normal to be on same side as eye
        if (ndi < 0) times(Ia, -1, Ia); 

        //printf("point %p %f\nnormal %p %f\neye %p %f\n",Ph,Pw,Nh,Nw3,Ih,Iw);
        //printf("N.I %f\n",dot(Ih,Nh,Iw,Nw3));
        // and add in the reflection map (like a specular highlight, without modulating by shading)
        // TODO implement non-euclidean reflection
        //if (reflectionmap != "") {
            //D = reflect(I, Nf) ;
            //D = vtransform ("world", D);
            //Ct = (1-reflectionBlend) * Ct + reflectionBlend * color environment(reflectionmap, D);
        //} 

        total = 0;
	    uniform float spec = 1.0/roughness;
        illuminance(P)
        {
	        /* first adjust light vector to be tangent at P */
	        /* L is also a difference vector, hence its w-cord = 0 */
            float La[4] = {xcomp(L), ycomp(L), zcomp(L), 0};
            hmaketangent(La);
            hnormalize(La);
	        //if ( abs(1-dot(La,La)) > .001)
                //printf("Normalized L has length squared = %f\n",dot(Lh,Lh,Lw,Lw));
    
	        float d = dot(La, tN);
	        if (d > 1.01)	{       // shouldn't happen
                //printf("big d: %f\n",d); 
		        d = 1.0;
            }

	        /* now compute bisector of angle between L and I */
	        /* important for Lh, Ih to be unit length */
            float M[4];
            linearCombination(M, Ia, 1, La);
	        /* compute cos(angle between normal and mid-vector H) */
	        float ss = dot(M,tN)/sqrt(dot(M,M));

	        if (ss < 0.0)	ss = -ss;
	        if (ss > 1.0)	ss = 1.0;
        
            //printf("Cl: %c\td: %f\n",Cl,d);
	        total = total + Os * Cl * (Ct *  (Ka*ambient() + Kd*d)+
	                specularcolor * Ks*pow(ss,spec)); // 0;
        }
	    Ci = total;
    }
	Oi = Os; 
}		
// old code for calculating specular
	        //float a = -dot(Ih, M, Iw, Mw)/dot(Lh,M,Lw, Mw);
	        //M = Ih + a*Lh;
	        //Mw = Iw + a*Lw;	
	        /* detect very small vectors, reject them */
	        //if (abs(M.M - Mw*Mw) < .0001)	{
	            //M = Lh;
	            //Mw = Lw;
	        //}
	        //if (Mw < 0.0)	{
	            //M = -M;
	            //Mw = -Mw;
	        //}
	

