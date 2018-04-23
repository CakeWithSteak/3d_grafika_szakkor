uniform sampler2D texture;

varying vec4 vertTexCoord;

void main() {
    //gl_FragColor = texture2D(texture,vertTexCoord.st);
    //gl_FragColor = vec4(vertTexCoord.rg,1,1);
    gl_FragColor = vec4(1 - int(gl_FrontFacing),int(gl_FrontFacing),0,1);
}
