package tong.sihriya.client.particle.magiccircle;

import net.minecraft.util.Mth;

public class MagicCircleAnimation {
    private final int lifetime;
    private float radius;
    private float rotation;
    private float alpha;

    public MagicCircleAnimation(int lifetime) {
        this.lifetime = lifetime;
        this.radius = 0.5f;
        this.rotation = 0;
        this.alpha = 0;
    }

    public void tick(int age) {
        float progress = (float) age / lifetime;

        if (progress < 0.25f) {
            float t = progress / 0.25f;
            radius    = Mth.lerp(t, 0.5f, 3.0f);
            alpha     = Mth.lerp(t, 0.0f, 1.0f);
            rotation += 2.0f;
        } else if (progress < 0.80f) {
            radius    = 3.0f;
            alpha     = 1.0f;
            rotation += 4.0f;
        } else {
            float t = (progress - 0.80f) / 0.20f;
            radius    = Mth.lerp(t, 3.0f, 5.0f);
            alpha     = Mth.lerp(t, 1.0f, 0.0f);
            rotation += 6.0f;
        }
    }

    public float getRadius()    { return radius; }
    public float getRotation()  { return rotation; }
    public float getAlpha()     { return alpha; }
}
