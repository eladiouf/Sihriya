package tong.sihriya.magiccircle;

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
        this.radius = 2.0f;
        this.alpha = 0.5f;
        this.rotRunes = 0;
        this.rotSymbols = 0;
        this.rotGeometry = 0;
        this.rotCenter = 0;
    }

    public void tick(int age) {
        float progress = (float) age / lifetime;

        if (progress < 0.15f) {
            float t = progress / 0.15f;
            radius = Mth.lerp(t, 2.0f, 6.0f);
            alpha  = Mth.lerp(t, 0.3f, 1.0f);
            rotRunes    += 2.0f;
            rotSymbols  -= 2.5f;
            rotGeometry += 4.0f;
            rotCenter   += 0.5f;
        } else if (progress < 0.75f) {
            radius = 6.0f;
            alpha  = 1.0f;
            rotRunes    += 3.0f;
            rotSymbols  -= 4.0f;
            rotGeometry += 6.0f;
            rotCenter   += 0.8f;
        } else {
            float t = (progress - 0.75f) / 0.25f;
            radius = Mth.lerp(t, 6.0f, 8.0f);
            alpha  = Mth.lerp(t, 1.0f, 0.0f);
            rotRunes    += 5.0f;
            rotSymbols  -= 6.0f;
            rotGeometry += 8.0f;
            rotCenter   += 1.2f;
        }
    }

    public float getRadius()                { return radius; }
    public float getAlpha()                 { return alpha; }
    public float getRotationRunes()         { return rotRunes; }
    public float getRotationSymbols()       { return rotSymbols; }
    public float getRotationGeometry()      { return rotGeometry; }
    public float getRotationCenter()        { return rotCenter; }
}
