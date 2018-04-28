uniform sampler2D texture;

varying vec4 vertTexCoord;

void main() {
    //gl_FragColor = texture2D(texture,vertTexCoord.st);    //Actually texture
    //gl_FragColor = vec4(vertTexCoord.rg,1,1);     //Show the uv coords
    //gl_FragColor = vec4(1 - int(gl_FrontFacing),int(gl_FrontFacing),0,1);     //Show the triengles' facing (green = outside, red = inside)
}
