attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

uniform mat4 transform;
uniform mat3 normalMatrix;

varying vec3 interpNormal;
varying vec4 fragColor;

void main() {
    vec3 realNormal = normalMatrix * normal;
    interpNormal = realNormal;
    fragColor = color;
    gl_Position = transform * position;
}
