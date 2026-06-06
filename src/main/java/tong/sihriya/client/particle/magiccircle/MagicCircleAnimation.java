package tong.sihriya.client.particle.magiccircle;

import net.minecraft.util.Mth;

public class MagicCircleAnimation {
    private final int lifetime;
    private float radius;
    private float alpha;
    private float rotRunes;
    private float rotSymbols;
    private float rotGeometry;
    private float rotCenter;

    public MagicCircleAnimation(int lifetime) {
        this.lifetime = lifetime;
        this.radius = 0.5f;
        this.alpha = 0;
        this.rotRunes = 0;
        this.rotSymbols = 0;
        this.rotGeometry = 0;
        this.rotCenter = 0;
    }

    public void tick(int age) {
        float progress = (float) age / lifetime;

        if (progress < 0.25f) {
            float t = progress / 0.25f;
            radius = Mth.lerp(t, 0.5f, 3.0f);
            alpha  = Mth.lerp(t, 0.0f, 1.0f);
            rotRunes    += 1.5f;
            rotSymbols  -= 2.0f;
            rotGeometry += 3.0f;
            rotCenter   += 0.3f;
        } else if (progress < 0.80f) {
            radius = 3.0f;
            alpha  = 1.0f;
            rotRunes    += 2.5f;
            rotSymbols  -= 3.5f;
            rotGeometry += 5.0f;
            rotCenter   += 0.5f;
        } else {
            float t = (progress - 0.80f) / 0.20f;
            radius = Mth.lerp(t, 3.0f, 5.0f);
            alpha  = Mth.lerp(t, 1.0f, 0.0f);
            rotRunes    += 4.0f;
            rotSymbols  -= 5.0f;
            rotGeometry += 7.0f;
            rotCenter   += 1.0f;
        }
    }

    public float getRadius()                { return radius; }
    public float getAlpha()                 { return alpha; }
    public float getRotationRunes()         { return rotRunes; }
    public float getRotationSymbols()       { return rotSymbols; }
    public float getRotationGeometry()      { return rotGeometry; }
    public float getRotationCenter()        { return rotCenter; }
}
