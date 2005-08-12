//uniform bool LocalViewer ;
//uniform bool SeparateSpecular ;
//uniform int NumEnabledLights;

void DirectionalLight(in int i,
                    in vec3 normal,
                    inout vec4 ambient,
                    inout vec4 diffuse,
                    inout vec4 specular)
{
    float nDotVP;
    float nDotHV;
    float pf;

    nDotVP = max(0.0, dot(normal, vec3 (gl_LightSource[i].position)));
    nDotHV = max(0.0, dot(normal, vec3 (gl_LightSource[i].halfVector)));

    if (nDotVP == 0.0)
        pf = 0.0;
    else
        pf = pow(nDotHV, gl_FrontMaterial.shininess);
    ambient += gl_LightSource[i].ambient;
    diffuse += gl_LightSource[i].diffuse * nDotVP;
    specular += gl_LightSource[i].specular * pf;
}

void main(void)
{
    vec3 ecPosition3, eye;
    bool LocalViewer = true;
    bool SeparateSpecular = false;
    int NumEnabledLights = 2;

    gl_Position = gl_ModelViewMatrix * gl_Vertex;
    ecPosition3 = (vec3 (gl_Position))/gl_Position.w;
    vec3 normal = gl_NormalMatrix * gl_Normal;
    normal = normalize(normal);
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;

    if (LocalViewer)
        eye = -normalize(ecPosition3);
    else
        eye = vec3 (0.0, 0.0, 1.0);
     
    // do the lighting calculations
    vec4 amb = vec4(0.0);
    vec4 diff = vec4(0.0);
    vec4 spec = vec4(0.0);
    // loop through lights
    int i;
    for (i = 0; i<NumEnabledLights; ++i)    {
        //if (gl_LightSource[i].position.w == 0)
            DirectionalLight(i, normal, amb, diff, spec);
        //else if (gl_LightSource[i].spotCutoff == 180.0)
            //PointLight(i, eye, ecPosition3, normal, amb, diff, spec);
        // TODO also spotlights
    }
    vec4 color = gl_FrontLightModelProduct.sceneColor +
        amb*gl_FrontMaterial.ambient +
        diff * gl_FrontMaterial.diffuse;

//    if (SeparateSpecular)
//        gl_FrontSecondaryColor = vec4 (spec * gl_FrontMaterial.specular, 1.0);
//    else 
        color += spec * gl_FrontMaterial.specular;

    gl_FrontColor = color;

    // TODO do two-sided lighting

}
