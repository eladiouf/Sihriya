package tong.sihriya.vfx;

import java.util.List;

public record VFXDefinition(
    String spellId,
    String school,
    ProjectileConfig projectile,
    BeamConfig beam,
    ImpactConfig impact,
    ChargeConfig charge,
    AuraConfig aura,
    PersistentConfig persistent,
    TrailConfig trail,
    SoundConfig sound,
    List<EffectPhase> phases
) {
    public record ProjectileConfig(float scale, String meshType, int meshDetail, float glowIntensity,
        float[] color1, float[] color2, float rotationSpeed, float pulseSpeed, float pulseAmount,
        EmitterConfig trail, EmitterConfig aura, boolean volumetric) {}

    public record BeamConfig(float radius, int segments, int rings, float[] color1, float[] color2,
        float glowIntensity, boolean scrolling, float scrollSpeed, boolean noise, float noiseAmount) {}

    public record ImpactConfig(EmitterConfig burst, boolean shockwave, int shockwaveRings,
        float shockwaveSpeed, float shockwaveRadius, int debris, boolean groundMark, int groundMarkDuration,
        boolean screenShake, float screenShakeIntensity, float[] color1, float[] color2) {}

    public record ChargeConfig(int duration, EmitterConfig emitter, MeshConfig mesh,
        boolean soundLoop, float scaleStart, float scaleEnd) {}

    public record AuraConfig(float radius, MeshConfig mesh, EmitterConfig emitter,
        boolean followEntity, int lifetime) {}

    public record PersistentConfig(int duration, MeshConfig mesh, EmitterConfig emitter,
        boolean groundDecal, float decalAlpha) {}

    public record TrailConfig(int particlesPerTick, int particleLifetime, float spread,
        boolean fade, String emitterType, float radius, float speed) {}

    public record EmitterConfig(String type, int count, float speed, float spread, float radius,
        float startRadius, float endRadius, float height, float angle, float contractionSpeed,
        int particlesPerTick, int particleLifetime) {}

    public record MeshConfig(String type, float radius, float height, int segments, int rings,
        float[] color, float alpha, float glowIntensity, float scaleStart, float scaleEnd, boolean fade) {}

    public record SoundConfig(String charge, String cast, String impact, String loop) {}

    public record EffectPhase(String type, int startTick, int duration, EmitterConfig emitter,
        MeshConfig mesh, TrailConfig trail, SoundConfig sound, ImpactConfig impact) {}
}
