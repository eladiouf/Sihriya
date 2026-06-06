#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D Aux1;
uniform sampler2D Aux2;
uniform sampler2D Aux3;
uniform float BloomIntensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 bloom1 = texture(Aux1, texCoord);
    vec4 bloom2 = texture(Aux2, texCoord);
    vec4 bloom3 = texture(Aux3, texCoord);

    vec4 bloom = (bloom1 + bloom2 + bloom3) * BloomIntensity;
    fragColor = vec4(scene.rgb + bloom.rgb, scene.a);
}
