// Dies bestimmt die Intensität (oder Höhe) der Bumpmap
const float bumpDepth = 0.04;

// Der Wert von pi lässt sich mit eingebauten Funktionen bestimmen
const float pi = 2.0 * asin(1.0);

// Licht-Richtung, Dämpfung und Richtung des Betrachters
// werden als interpolierte Werte vom Vertex Shader übergeben
varying vec3  lightDirection[3];
varying float attenuation[3];
varying vec3  eyeDirection;

uniform sampler2D normalMap;
uniform sampler2D diffuseMap;
uniform samplerCube envMap;

void main ()
{
    // varying Vektoren haben nach der Interpolation durch die GPU
    // (in der Regel) keine Einheitslänge.
    vec3 eyeDir = normalize(eyeDirection);

    vec3 normal = texture2D(normalMap, gl_TexCoord[0].xy).xyz * 2.0 - 1.0;

    // Alle drei Lichtquellen beeinflussen die Farbe des Pixels.
    // Die Berechnungen in der Schleife entsprechen den
    // Standard-Berechnungen zum Phong Modell.
    vec4 diffuseColor = texture2D(diffuseMap, gl_TexCoord[0].xy);
    vec4 color = vec4(0.0);
    for (int i = 0; i < 3; ++i)
    {
        vec3  lightDir   = normalize(lightDirection[i]);
        vec3  reflection = normalize(reflect(-lightDir, normal));
        float specular   = pow(max(dot(reflection, eyeDir), 0.0),
                               gl_FrontMaterial.shininess);
        color += gl_FrontMaterial.ambient * gl_LightSource[i].ambient;
        color += attenuation[i] * diffuseColor * gl_FrontMaterial.diffuse * gl_LightSource[i].diffuse
                 * max(dot(lightDir, normal), 0.0);
        color += attenuation[i] * gl_FrontMaterial.specular * gl_LightSource[i].specular
                 * specular;
    }

    // Letztendlich kann dem Pixel seine Farbe zugeordnet werden.
    gl_FragColor = color;
    gl_FragColor.w = 1.0;
}
