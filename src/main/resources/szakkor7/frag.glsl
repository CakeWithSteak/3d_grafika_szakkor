#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

const int numLights = 4;

uniform sampler2D texture;
uniform int repeat;

uniform sampler2D exponent;
uniform bool hasExponent;//Decides whether to use the exponent map or a constant shininess value
uniform vec3 lightDiffuse[numLights];

varying vec4 vertTexCoord;
varying vec3 ecNormal;
varying vec3 ecViewDir;
varying vec3 ecLightDir[numLights];
varying vec3 distance[numLights];


vec4 bpShade(vec4 baseColor) {
    vec4 ambientColor = baseColor;
    vec4 diffuseColor = vec4(0,0,0,1);//The diffuse color is going to be a mix of each light's color.
    vec4 specularColor = vec4(1,1,1,1);//The specular hightlight is always going to be white.


    float ambientReflection = .05;
    float diffuseReflection = 1;
    float specularReflection = .5;
    float r = 500;

    vec3 realFragNormal = normalize(ecNormal);
    vec3 viewDir = normalize(ecViewDir);

    //Ambient
    float ambientTerm = ambientReflection;

    float specularTerm = 0;
    float shininess = hasExponent ? texture2D(exponent,vertTexCoord.st).r * 149 + 1 : 10;


    for(int i = 0;i < numLights;++i) {
        float x = length(distance[i]);
        if(x > r)
            continue;

        float intensity = pow(clamp(1 - pow(x,2)/(r*r),0.f,1.f),2);
        vec3 lightDir = normalize(ecLightDir[i]);

        //Diffuse
        float diffuseTerm = max(0,dot(lightDir, realFragNormal)) * intensity * diffuseReflection;
        diffuseColor += vec4(lightDiffuse[i] * diffuseTerm,1) * baseColor;//Weird, but looks good enough


        //Specular
        vec3 halfAngle = normalize(lightDir + viewDir);
        float thisSpecularTerm = max(0,dot(halfAngle, realFragNormal));
        specularTerm += pow(thisSpecularTerm,shininess) * intensity * specularReflection;
    }

    return vec4(
    ((ambientTerm * ambientColor) + diffuseColor + (specularTerm * specularColor)).rgb,1
    );
}

void main() {
    vec4 texColor = vec4(texture2D(texture,vertTexCoord.st * repeat).rgb,1);
    gl_FragColor = bpShade(texColor);
}
