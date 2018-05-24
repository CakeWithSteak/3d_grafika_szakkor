const int numLights = 1;

uniform mat4 transform;
uniform mat4 viewInv;
uniform mat4 modelview;
uniform mat3 normalMatrix;
uniform mat4 texMatrix;
uniform vec4 lightPosition[numLights];

attribute vec4 position;
attribute vec3 normal;
in vec2 texCoord;

out vec3 ecNormal;
out vec3 ecPosition;
out vec2 uv;
out vec3 ecViewDir;
out vec3 distance[numLights];
out vec3 ecLightDir[numLights];


void main() {
    gl_Position = transform * position;

    uv = (texMatrix*vec4(texCoord, 1,1)).st;

    ecNormal = normalize(normalMatrix * normal); // Vertex in eye coordinates
    ecPosition = vec3(modelview * position); // Normal vector in eye coordinates
    ecViewDir = -(normalize(ecPosition)).xyz;

    for(int i = 0;i < numLights;++i) {
        distance[i] = (lightPosition[i] - vec4(ecPosition,1)).xyz;
        ecLightDir[i] = normalize(distance[i]);
    }
}
