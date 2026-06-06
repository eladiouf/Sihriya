package tong.sihriya.client.vfx.mesh;

import net.minecraft.world.phys.Vec3;

public record MeshVertex(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
    public MeshVertex(Vec3 pos, Vec3 normal, float u, float v) {
        this((float)pos.x, (float)pos.y, (float)pos.z,
             (float)normal.x, (float)normal.y, (float)normal.z, u, v);
    }
}
