#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 BlurDir;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;

    for (float i = -Radius; i <= Radius; i += 1.0) {
        float weight = exp(-(i * i) / (2.0 * Radius * Radius / 4.0));
        vec2 offset = BlurDir * i / textureSize(DiffuseSampler, 0);
        color += texture(DiffuseSampler, texCoord + offset) * weight;
        totalWeight += weight;
    }

    fragColor = color / totalWeight;
}
