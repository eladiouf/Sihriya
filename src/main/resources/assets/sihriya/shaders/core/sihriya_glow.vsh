#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float uTime;
uniform vec3 uViewPos;

out vec4 vertexColor;
out vec2 texCoord;
out float fresnelIntensity;
out vec3 worldNormal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vec3 viewDir = normalize(uViewPos - Position);
    float viewAngle = max(dot(viewDir, Normal), 0.0);
    fresnelIntensity = pow(1.0 - viewAngle, 3.0);

    float pulse = 0.5 + 0.5 * sin(uTime * 2.0 + Position.y * 3.0);
    fresnelIntensity += pulse * 0.15;

    vertexColor = Color;
    texCoord = UV0;
    worldNormal = Normal;
}
