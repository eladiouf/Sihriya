#version 150

uniform sampler2D DiffuseSampler;
uniform float BrightThreshold;
uniform float BrightMultiplier;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    if (luminance > BrightThreshold) {
        fragColor = color * BrightMultiplier * (luminance - BrightThreshold) / (1.0 - BrightThreshold);
    } else {
        fragColor = vec4(0.0);
    }
}
