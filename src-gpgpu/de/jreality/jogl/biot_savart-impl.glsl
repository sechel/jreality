vec3 biotSavart(const vec3 pt, const samplerRect vort) {
  
  vec3 ret;
  
  vec4 data=textureRect(vort, vec2(0.5,0.5));
  
  vec3 v1=data.xyz-pt;
  vec3 v2;
  
  float norm1=length(v1);
  float norm2;
    
  for (int i=0; i < cnt; i++) {
    for (int j=0; j < cnt; j++) {
      // add biot savart on for one edge

      data=textureRect(vort, vec2(j+.5, i+.5));
      
      float strength=data.w;
      
      v2 =data.xyz-pt;
      norm2=length(v2);
      
      vec3 e = v2-v1;
      
      if (strength > 0) {
        ret += strength*biotSavartEdge(e, pt, v1, norm1, v2, norm2);
      }
      vec3 swap = v1;
      v1 = v2;
      v2 = swap;
      norm1 = norm2;
    }
  }
  
  return ret;
  
}

vec3 biotSavartEdge(const vec3 edge, const vec3 point, const vec3 v1, const float norm1, const vec3 v2, const float norm2) {
  float fac1 = dot(v1, edge);
  float fac2 = dot(v2, edge);
  
  vec3 v1CrossS = cross(v1, edge);
  vec3 v2CrossS = cross(v2, edge);
  
  float normC1 = dot(v1CrossS, v1CrossS);
  fac1 /= sqrt(roSquared+norm1*norm1) * (roSquared * dot(edge, edge) + normC1);
  
  float normC2 = dot(v2CrossS, v2CrossS);
  fac2 /= sqrt(roSquared+norm2*norm2) * (roSquared * dot(edge, edge) + normC2);
  
  return fac2*v2CrossS - fac1*v1CrossS;
}


