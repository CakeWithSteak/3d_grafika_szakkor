attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

uniform mat4 transform;
uniform mat3 normalMatrix;

varying vec3 fragNormal;
varying vec4 fragColor;

void main() {
    fragNormal = normalMatrix * normal;
    fragColor = color;
    gl_Position = transform * position;
}
