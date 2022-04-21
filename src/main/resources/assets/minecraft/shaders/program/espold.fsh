#version 130

uniform sampler2D sampler;
in vec2 texelSize;

//const int borderSize = 14;
//const int innerBorderSize = 10;
//4k lol ^^

uniform float borderSize;
//uniform float innerBorderSize = 3;

uniform vec4 outlineColor;

out vec4 fragColour;

void outlinePass(vec2 fragCoord, vec4 inColour);

void main() {
  vec2 fragCoord = gl_TexCoord[0].st;
  vec4 inColour = texture(sampler, fragCoord);

  if (outlineColor.a > 0) {
    fragColour = vec4(outlineColor.rgb, inColour.a);
  } else {
    outlinePass(fragCoord, inColour);
  }
}

vec4 colourAtOffset(vec2 fragCoord, int x, int y) {
  vec2 offset = vec2(x * texelSize.x, y * texelSize.y);
  //  return texture(sampler, fragCoord + offset);
  vec4 col = texture(sampler, fragCoord + offset);
  if (col.a > 0) {
    //    col.rgb = vec3(1, 0, 0);
  }
  return col;
}

const float sqrt2 = sqrt(2);
vec4 bitchesLoveMyCode(vec2 fragCoord, int i) {
  //
  //  vec4 avCol = vec4(0);
  //  for (int x = -borderSize; x < borderSize; x++) {
  //    for (int y = -borderSize; x < borderSize; x++) {
  //      vec4 col = colourAtOffset(fragCoord, -x, -u);
  //  }
  int u = int(round(float(i) / sqrt2));
  vec4 avCol = vec4(0);
  vec4 tl = colourAtOffset(fragCoord, -u, -u);
  avCol = mix(avCol, tl, tl.a);
  vec4 tr = colourAtOffset(fragCoord, u, -u);
  avCol = mix(avCol, tr, tr.a);
  vec4 bl = colourAtOffset(fragCoord, -u, u);
  avCol = mix(avCol, bl, bl.a);
  vec4 br = colourAtOffset(fragCoord, u, u);
  avCol = mix(avCol, br, br.a);
  vec4 t = colourAtOffset(fragCoord, 0, -i);
  avCol = mix(avCol, t, t.a);
  vec4 b = colourAtOffset(fragCoord, 0, i);
  avCol = mix(avCol, b, b.a);
  vec4 r = colourAtOffset(fragCoord, i, 0);
  avCol = mix(avCol, r, r.a);
  vec4 l = colourAtOffset(fragCoord, -i, 0);
  avCol = mix(avCol, l, l.a);

  avCol.a = (tl.a + tr.a + bl.a + br.a + t.a + b.a + r.a + l.a) / 8.0;
  return avCol;
}

void outlinePass(vec2 fragCoord, vec4 inColour) {
  //    vec4 center     = colourAtOffset(fragCoord, 0, 0);
  //    vec4 up     = colourAtOffset(fragCoord, 0, -1);
  //    vec4 down     = colourAtOffset(fragCoord, 0, 1);
  //    vec4 left     = colourAtOffset(fragCoord, -1, 0);
  //    vec4 right     = colourAtOffset(fragCoord, 1, 0);
  //    vec4 uDiff = center - up;
  //    vec4 dDiff = center - down;
  //    vec4 lDiff = center - left;
  //    vec4 rDiff = center - right;
  //    vec4 sum = uDiff + dDiff + lDiff + rDiff;
  //    vec3 clamped = clamp(center.rgb - sum.rgb, 0.0, 1.0);
  //    fragColour = vec4(clamped, center.a);
  //    return;

  if (inColour.a > 0) {
    // Paint it black
    if (bitchesLoveMyCode(fragCoord, int(borderSize * 0.75)).a < 1|| bitchesLoveMyCode(fragCoord, 1).a < 1) {
      for (int i = 1; i <= int(borderSize * 0.75); i++) {
        vec4 colourAt = bitchesLoveMyCode(fragCoord, i);
        if (colourAt.a < 1) {
          fragColour.rgb = vec3(0);
          fragColour.a = (1.0 - pow(float(i) / float(borderSize * 0.75 + 1), 0.7));
          return;
        }
      }
    }
  } else {
    // Colour the outside
    if (bitchesLoveMyCode(fragCoord, int(borderSize)).a > 0 || bitchesLoveMyCode(fragCoord, 1).a > 0) {
      for (int i = 1; i <= int(borderSize); i++) {
        vec4 colourAt = bitchesLoveMyCode(fragCoord, i);
        if (colourAt.a > 0) {
          fragColour.rgb = colourAt.rgb;
          fragColour.a = (1.0 - pow(float(i) / float(borderSize + 1), 0.7)) * 0.9;
          return;
        }
      }
    }
  }
}
