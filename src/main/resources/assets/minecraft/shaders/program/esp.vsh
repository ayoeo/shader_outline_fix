#version 130

uniform sampler2D sampler;
out vec2 texelSize;

//uniform sampler2D lowResSampler;
//out vec2 lrSamplerSize;

//uniform vec4 outlineColor;

void main() {
  //texSize = textureSize(sampler, 0);

  texelSize = 1.0 / textureSize(sampler, 0);
  //lrSamplerSize = textureSize(lowResSampler, 0);

  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  gl_TexCoord[0] = gl_MultiTexCoord0;
} 


