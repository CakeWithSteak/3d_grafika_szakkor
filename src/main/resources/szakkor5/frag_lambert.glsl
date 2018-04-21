varying vec3 interpNormal;
varying vec4 fragColor;

uniform vec3 lightDir;
int intensity = 1;

void main() {
    float ratio = max(0,dot(lightDir,normalize(interpNormal)));
    gl_FragColor = vec4(ratio * intensity * fragColor.xyz,1);
}
