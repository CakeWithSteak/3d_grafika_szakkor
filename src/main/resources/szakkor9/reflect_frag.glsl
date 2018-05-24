const int numLights = 1;

uniform samplerCube cubemap;
uniform sampler2D texture;
uniform samplerCube iblCubemap;
uniform vec3 lightDiffuse[numLights];
uniform mat4 viewInv;
//uniform vec3 cameraPos;

/*in vec3 worldNormal;
in vec3 worldPosition; // position*/
in vec3 ecNormal;
in vec3 ecPosition;
in vec2 uv;
in vec3 ecViewDir;
in vec3 ecLightDir[numLights];
in vec3 distance[numLights];

const bool hasExponent = false;
uniform sampler2D exponent; //Unused

const float refMix = 1;
const float iblFactor = 0.0;

const float mixOffset = 0.003;


vec4 calcLightFX() {
    //Camera space
    vec3 normal = normalize(ecNormal);
    vec3 incident = normalize(ecPosition);
    //World space
    vec3 reflection = mat3(viewInv) * reflect(incident,normal);

    const float relativeIOR = 1/1.08;

   /* vec3 tR = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset);
    vec3 tG = mat3(viewInv) * refract(incident,normal,relativeIOR);
    vec3 tB = mat3(viewInv) * refract(incident,normal,relativeIOR - mixOffset);*/
    vec3 tR = mat3(viewInv) * refract(incident,normal,relativeIOR);
    vec3 tY = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset * 1);
    vec3 tG = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset * 2);
    vec3 tC = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset * 3);
    vec3 tB = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset * 4);
    vec3 tV = mat3(viewInv) * refract(incident,normal,relativeIOR + mixOffset * 5);

    float cR = texture(cubemap,tR).r / 2;
    float cY = (texture(cubemap,tY).r * 2 +
                texture(cubemap,tY).g * 2 -
                texture(cubemap,tY).b) / 6;
    float cG = texture(cubemap,tG).g / 2;
    float cC = (texture(cubemap,tC).g * 2 +
                    texture(cubemap,tC).b * 2 -
                    texture(cubemap,tC).r) / 6;
    float cB = texture(cubemap,tB).b / 2;
    float cV = (texture(cubemap,tV).b * 2 +
                    texture(cubemap,tV).r * 2 -
                    texture(cubemap,tV).g) / 6;

    float fresnel = clamp(dot(normal,-incident),0f,1f);
    fresnel = pow(fresnel,.1f);
    if(length(tR) == 0) {
        fresnel = 0;
    }
    //float fresnel = 1;  //Radios only reflect light

    vec3 reflectColor = texture(cubemap,reflection).rgb;

    vec3 refractColor;
    refractColor.r = cR + (2 * cV + 2 * cY - cC) / 3;
    refractColor.g = cG + (2 * cY + 2 * cC - cV) / 3;
    refractColor.b = cB + (2 * cC + 2 * cV - cY) / 3;
    /*refractColor.r = texture(cubemap,tR).r;
    refractColor.g = texture(cubemap,tG).g;
    refractColor.b = texture(cubemap,tB).b;*/

    vec3 color = mix(reflectColor,refractColor,fresnel);

    /*gl_FragColor.rgb = color * texture(texture,uv).rgb / 2;
    gl_FragColor.a = 1;*/
    return vec4(color,1);
}

vec4 bpShade(vec4 baseColor) {
    vec4 ambientColor = baseColor;
    vec4 diffuseColor = vec4(0,0,0,1);//The diffuse color is going to be a mix of each light's color.
    vec4 specularColor = vec4(1,1,1,1);//The specular hightlight is always going to be white.


    float ambientReflection = .15;
    float diffuseReflection = 1;
    float specularReflection = .5;
    float r = 500;

    vec3 realFragNormal = normalize(ecNormal);
    vec3 viewDir = normalize(ecViewDir);

    //Ambient
    float ambientTerm = ambientReflection;

    float specularTerm = 0;
    float shininess = hasExponent ? texture(exponent,uv).r * 149 + 1 : 10;

    for(int i = 0;i < numLights;++i) {
        float x = length(distance[i]);
        if(x > r)
            continue;

        float intensity = pow(clamp(1 - pow(x,2)/(r*r),0.f,1.f),2);
        vec3 lightDir = normalize(ecLightDir[i]);

        //Diffuse
        float diffuseTerm = max(0,dot(lightDir, realFragNormal)) * intensity * diffuseReflection;
        diffuseColor += vec4(lightDiffuse[i] * diffuseTerm,1) * baseColor / 2;//Weird, but looks good enough


        //Specular
        vec3 halfAngle = normalize(lightDir + viewDir);
        float thisSpecularTerm = max(0,dot(halfAngle, realFragNormal));
        specularTerm += pow(thisSpecularTerm,shininess) * intensity * specularReflection;
    }

    diffuseColor += vec4(texture(iblCubemap,realFragNormal).rgb,1) * iblFactor;

    return vec4(
    ((ambientTerm * ambientColor) + diffuseColor + (specularTerm * specularColor)).rgb,1
    );
}

void main() {
    vec4 refLight = min(calcLightFX(),vec4(1,1,1,1));
    vec4 bpLight = min(bpShade(texture(texture,uv)),vec4(1,1,1,1));
    gl_FragColor = mix(bpLight,refLight,refMix);
}