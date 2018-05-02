const int numLights = 4;

uniform mat4 transform;
uniform mat4 texMatrix;
uniform mat3 normalMatrix;
uniform vec4 lightPosition[numLights];
uniform mat4 modelview;

attribute vec4 texCoord;
attribute vec4 position;
attribute vec3 normal;

varying vec4 vertTexCoord;
varying vec3 ecNormal;
varying vec3 ecViewDir;
varying vec3 ecLightDir[numLights];
varying vec3 distance[numLights];

void main() {
     // normalized device coordinates [-1,1]
     gl_Position = transform * position;
     vertTexCoord = texMatrix * texCoord;
     ecNormal = normalMatrix * normal;
     vec4 ecPostiton = (modelview * position);
     ecViewDir = -(normalize(ecPostiton)).xyz;
     for(int i = 0;i < numLights;++i) {
        distance[i] = (lightPosition[i] - ecPostiton).xyz;
        ecLightDir[i] = normalize(distance[i]);
     }

}
