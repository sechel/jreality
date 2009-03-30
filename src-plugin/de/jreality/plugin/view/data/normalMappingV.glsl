// Für Normalmapping wird pro Vertex eine Tangente benötigt,
// mit deren Hilfe Richtungsvektoren in den Tangentenraum
// transformiert werden können.

// Richtung und Dämpfung des Lichts ändern sich von Fragment zu
// Fragment. Sie werden pro Vertex berechnet und von der GPU
// automatisch für den Fragment Shader interpoliert. Der
// Einfachheit halber werden nur drei Lichtquellen betrachtet.
varying vec3  lightDirection[3];
varying float attenuation[3];

// Auch die Richtung zum Betrachter ist in der Regel
// für jedes Fragment unterschiedlich.
varying vec3 eyeDirection;



void main ()
{
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[0].y *= -1.0; // Why is this?
    gl_Position    = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Die Transformationsmatrix für den Tangentenraum setzt
    // sich aus der Normalen, der Tangente und der Orthogonalen
    // zu den beiden zusammen:
    //
    //       [Tx, Ty, Tz]
    //   M = [Nx, Ny, Nz]
    //       [Bx, By, Bz]
    //
    // Man beachte, dass dies eine Rotationsmatrix ist, wir können
    // mit ihr also keine Translationen durchführen. Entsprechend
    // können mit ihr nur Richtungen transformiert werden, und
    // keine (absoluten) Positionen.
    vec3 n = normalize(gl_NormalMatrix * gl_Normal);
    vec3 t = normalize(gl_NormalMatrix * gl_MultiTexCoord1.xyz);
    vec3 b = gl_MultiTexCoord1.w * cross(n, t);

    // Die Richtung zum Betrachter entspricht dem Negativen der
    // Vertex-Position, da der Betrachter immer am Ursprung
    // angenommen wird.
    vec3 eyeDir = -(gl_ModelViewMatrix * gl_Vertex).xyz;

    // Richtung und Dämpfung pro Lichtquelle berechnen
    for (int i = 0; i < 3; ++i)
    {
        vec3  lightDir = gl_LightSource[i].position.xyz + eyeDir;
        float distance = length(lightDir);

        // Die Dämpfung des Lichts hängt von der Distanz
        // zur Lichtquelle und den Parametern derselben ab.
        attenuation[i] = 1.0 / (gl_LightSource[i].quadraticAttenuation * distance * distance
                                + gl_LightSource[i].linearAttenuation * distance
                                + gl_LightSource[i].constantAttenuation);

        // Licht-Richtung in den Tangentenraum transformieren.
        // Da eine Normalisierung des Vektors ohnehin nach der Interpolation
        // durch die GPU im Fragment Shader durchgeführt werden muss, können
        // wir sie uns hier sparen.
        lightDirection[i].x = dot(t, lightDir);
        lightDirection[i].y = dot(b, lightDir);
        lightDirection[i].z = dot(n, lightDir);
    }

    // Richtung zum Betrachter in den Tangentenraum transformieren.
    // Auch hier können wir uns, analog zur Licht-Richtung,
    // eine Normalisierung des Vektors sparen.
    eyeDirection.x = dot(t, eyeDir);
    eyeDirection.y = dot(b, eyeDir);
    eyeDirection.z = dot(n, eyeDir);
}
