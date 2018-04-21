varying vec4 fragColor;
varying vec3 fragNormal;

vec3 viewDir = vec3(0,0,0);
uniform vec3 lightDir;

float intensity = 2;

vec4 ambientColor = vec4(.001,.001,.001,1);
vec4 diffuseColor = vec4(.01,.01,.01,1);
vec4 specularColor = vec4(1,1,1,1);

float ambientReflection = 2;
float diffuseReflection = 1;
float shininess = 1000;
float specularReflection = 1;

void main() {
    vec3 realFragNormal = normalize(fragNormal);

    //Ambient
    float ambientTerm = ambientReflection * intensity;

    //Diffuse
    float diffuseTerm = max(0,dot(lightDir, realFragNormal)) * intensity * diffuseReflection;

    //Specular
    vec3 halfAngle = normalize(lightDir + viewDir);
    float specularTerm = max(0,dot(halfAngle, realFragNormal));
    specularTerm = pow(specularTerm,shininess) * intensity * specularReflection;

    float outIntensity = ambientTerm + diffuseTerm + specularTerm;
    //gl_FragColor = vec4(outIntensity * fragColor.xyz,1);
    gl_FragColor = vec4(
    ((ambientTerm * ambientColor) + (diffuseTerm * diffuseColor) + (specularTerm * specularColor)).xyz,1
    );
}
