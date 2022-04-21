#version 130

uniform sampler2D sampler;
uniform float textured;
in vec4 color;

out vec4 fragColour;

void main() {
  vec2 fragCoord = gl_TexCoord[0].st;
  if (textured == 1) {
    vec4 inColour = texture(sampler, fragCoord);
    fragColour = inColour * color;
    if (inColour.a > 0f && color.a > 0f) fragColour.a = color.a;
  } else {
    fragColour = color;
  }
}
