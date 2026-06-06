#version 150

uniform sampler2D Sampler0;
uniform float uTime;
uniform vec4 uGlowColor;
uniform vec4 uGlowColor2;
uniform float uGlowIntensity;

in vec4 vertexColor;
in vec2 texCoord;
in float fresnelIntensity;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord) * vertexColor;

    float glow = fresnelIntensity * uGlowIntensity;
    float luminance = dot(baseColor.rgb, vec3(0.299, 0.587, 0.114));
    float mask = 1.0 - luminance * 0.5;

    vec3 glowColor = mix(uGlowColor.rgb, uGlowColor2.rgb, fresnelIntensity);
    float hueShift = sin(uTime * 0.5) * 0.05;
    glowColor += vec3(hueShift);

    vec3 finalColor = baseColor.rgb + glowColor * glow * mask * baseColor.a;
    float finalAlpha = max(baseColor.a, glow * 0.5);

    fragColor = vec4(finalColor, finalAlpha);
}
