#version 130

uniform sampler2D sampler;
out vec2 texelSize;

void main() {
  texelSize = 1.0 / textureSize(sampler, 0);

  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  gl_TexCoord[0] = gl_MultiTexCoord0;
} 


