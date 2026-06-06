# Sihriya VFX System — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Build a professional-grade 3D VFX system for Sihriya with custom GLSL shaders, procedural 3D meshes, particle emitters, bloom post-processing, and data-driven visual definitions.

**Architecture:** 6 sequential milestones. M1 (CoreShader + Bloom + DataLoader), M2 (Meshes 3D + VFXEngine), M3 (Emitters + Network), M4 (Projectiles + Beams + Lightning), M5 (Auras + Shields + Composer), M6 (Data-Driven + Polish). Each milestone compiles and produces visible results.

**Tech Stack:** Forge 1.20.1, Java 17, GLSL, BufferBuilder, PostChain

**References:**
- Ars Nouveau (LGPL V3): particle system, helix emitters, custom render types
- CooParticlesAPI (GPL-3.0): RenderEntity lifecycle, PostEffectChain architecture (patterns only)
- Elementals (SCSL): spell projectile rendering patterns

---

## File Structure

### New files to create

```
assets/sihriya/shaders/
├── core/
│   ├── sihriya_glow.vsh          # Vertex shader: Fresnel glow
│   └── sihriya_glow.fsh          # Fragment shader: Fresnel glow + additive
└── post/
    ├── bloom.json                 # PostChain orchestration
    ├── bright.json                # Bright pass definition
    ├── bright.fsh                 # Bright pass: luminance extraction
    ├── combine.json               # Combine pass definition
    ├── combine.fsh                # Combine pass: original + bloom
    └── blur.fsh                   # Blur pass: Gaussian blur

src/main/java/tong/sihriya/vfx/
├── VFXRegistry.java               # Loads JSON definitions, maps spellId→VFXDefinition
├── VFXDefinition.java             # Records: VFXDefinition, ProjectileConfig, BeamConfig, etc.
├── VFXEngine.java                 # Runtime lifecycle: init, tick, render, pool
├── VFXEffect.java                 # Lightweight VFX instance (NOT an Entity)
├── ObjectPool.java                # Generic object pool for VFXEffect reuse
├── PerformanceMonitor.java        # Auto-disable bloom if FPS < 30
│
├── shader/
│   ├── SihriyaCoreShaders.java    # CoreShader registration + uniforms
│   └── SihriyaRenderTypes.java    # Extension: glowMesh, beam, lightning RenderTypes
│
├── mesh/
│   ├── ProceduralMesh.java        # Interface + vertex cache
│   ├── SphereMesh.java            # UV sphere generator
│   ├── TubeMesh.java              # Tube/ribbon generator
│   ├── TorusMesh.java             # Torus/ring generator
│   ├── ConeMesh.java              # Cone generator
│   ├── DiskMesh.java              # Flat disk generator
│   └── MeshRenderer.java          # Generic mesh renderer
│
├── emitter/
│   ├── Emitter.java               # Abstract emitter interface
│   ├── EmitterManager.java        # Manages active emitters
│   ├── SpiralEmitter.java         # Spiral particle pattern
│   ├── VortexEmitter.java         # Vortex/tornado pattern
│   ├── ConeEmitter.java           # Cone spray pattern
│   ├── RingEmitter.java           # Expanding ring pattern
│   ├── HelixEmitter.java          # Double helix pattern
│   └── BurstEmitter.java          # Spherical explosion
│
├── render/
│   ├── BeamRenderer.java          # Tube mesh + scroll texture
│   ├── TrailHandler.java          # Particle trail behavior
│   ├── ImpactHandler.java         # Burst + ring + shockwave
│   └── LightningBoltHelper.java   # Midpoint displacement arc gen
│
├── composer/
│   ├── EffectComposer.java        # Multi-phase effect scheduler
│   └── EffectPhase.java           # Single phase definition
│
├── data/
│   └── SihriyaVFXData.java        # JSON data loader
│
├── network/
│   ├── VFXTriggerPacket.java      # Server→Client: start VFX
│   ├── VFXStopPacket.java         # Server→Client: stop VFX
│   └── VFXUpdatePacket.java       # Server→Client: update position
│
└── post/
    └── BloomPostChain.java        # PostChain bloom wrapper
```

### Files to modify

```
src/main/java/tong/sihriya/
├── Sihriya.java                          # Add VFX mod bus events
├── config/SihriyaClientConfig.java       # Add vfxBloom, vfxBloomIntensity
├── data/SchoolColors.java                # Centralize + add secondary colors
├── client/ClientSetup.java               # Add shader, post-chain, VFXEngine registration
├── core/SpellCastHandler.java            # Replace particle spawn with VFX engine calls
├── projectile/SpellProjectile.java       # Add trail, use VFX impact
├── client/projectile/SpellProjectileRenderer.java  # Upgrade to 3D mesh
├── client/particle/magiccircle/MagicCircleRenderer.java  # Use SchoolColors instead of local map
└── client/particle/magiccircle/SihriyaRenderTypes.java   # Keep as-is, new types in vfx.render
```

### Tests to create

```
src/test/java/tong/sihriya/vfx/
├── SpiralEmitterTest.java           # Verify particle lifecycle
├── SphereMeshTest.java              # Verify vertex count
├── VFXRegistryTest.java             # Verify JSON load
└── VFXEngineTest.java               # Verify lifecycle
```

---

## Milestone 1 — Fondations : CoreShader + Bloom + DataLoader + SchoolColors

### Task 1.1: Shader GLSL files

**Files:**
- Create: `assets/sihriya/shaders/core/sihriya_glow.vsh`
- Create: `assets/sihriya/shaders/core/sihriya_glow.fsh`
- Create: `assets/sihriya/shaders/post/bloom.json`
- Create: `assets/sihriya/shaders/post/bright.json`
- Create: `assets/sihriya/shaders/post/bright.fsh`
- Create: `assets/sihriya/shaders/post/combine.json`
- Create: `assets/sihriya/shaders/post/combine.fsh`
- Create: `assets/sihriya/shaders/post/blur.fsh`

- [ ] **Step 1: Create `sihriya_glow.vsh`**

```glsl
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
```

- [ ] **Step 2: Create `sihriya_glow.fsh`**

```glsl
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
```

- [ ] **Step 3: Create `bloom.json`** (PostChain orchestration — 5 targets, 6 passes)

```json
{
    "targets": [
        "brightPass",
        "blurHorizontal1",
        "blurVertical1",
        "blurHorizontal2",
        "blurVertical2"
    ],
    "passes": [
        {
            "name": "bright",
            "intarget": "minecraft:main",
            "outtarget": "brightPass",
            "auxtargets": [],
            "uniforms": [
                { "name": "BrightThreshold", "values": [0.6] },
                { "name": "BrightMultiplier", "values": [1.5] }
            ]
        },
        {
            "name": "blur",
            "intarget": "brightPass",
            "outtarget": "blurHorizontal1",
            "auxtargets": [],
            "uniforms": [
                { "name": "BlurDir", "values": [1.0, 0.0] },
                { "name": "Radius", "values": [4.0] }
            ]
        },
        {
            "name": "blur",
            "intarget": "blurHorizontal1",
            "outtarget": "blurVertical1",
            "auxtargets": [],
            "uniforms": [
                { "name": "BlurDir", "values": [0.0, 1.0] },
                { "name": "Radius", "values": [4.0] }
            ]
        },
        {
            "name": "blur",
            "intarget": "blurVertical1",
            "outtarget": "blurHorizontal2",
            "auxtargets": [],
            "uniforms": [
                { "name": "BlurDir", "values": [1.0, 0.0] },
                { "name": "Radius", "values": [2.0] }
            ]
        },
        {
            "name": "blur",
            "intarget": "blurHorizontal2",
            "outtarget": "blurVertical2",
            "auxtargets": [],
            "uniforms": [
                { "name": "BlurDir", "values": [0.0, 1.0] },
                { "name": "Radius", "values": [2.0] }
            ]
        },
        {
            "name": "combine",
            "intarget": "minecraft:main",
            "outtarget": "minecraft:main",
            "auxtargets": [
                { "name": "Aux1", "type": "previous" },
                { "name": "Aux2", "type": "previous" },
                { "name": "Aux3", "type": "previous" }
            ],
            "uniforms": [
                { "name": "BloomIntensity", "values": [0.8] }
            ]
        }
    ]
}
```

- [ ] **Step 4: Create `bright.fsh`**

```glsl
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
```

- [ ] **Step 5: Create `combine.fsh`**

```glsl
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
```

- [ ] **Step 6: Create `blur.fsh`**

```glsl
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
```

### Task 1.2: SihriyaCoreShaders

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/shader/SihriyaCoreShaders.java`

- [ ] **Step 1: Create SihriyaCoreShaders with RegisterShadersEvent handler**

```java
package tong.sihriya.vfx.shader;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.event.RegisterShadersEvent;
import tong.sihriya.Sihriya;
import com.mojang.blaze3d.shaders.Uniform;

import java.io.IOException;

public class SihriyaCoreShaders {
    public static final String GLOW_SHADER_NAME = "sihriya_glow";
    private static ShaderInstance glowShader;
    private static Uniform GLOW_TIME;
    private static Uniform GLOW_VIEWPOS;
    private static Uniform GLOW_COLOR;
    private static Uniform GLOW_COLOR2;
    private static Uniform GLOW_INTENSITY;

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceLocation loc = new ResourceLocation("sihriya", GLOW_SHADER_NAME);
            ShaderInstance instance = new ShaderInstance(
                event.getResourceProvider(), loc, DefaultVertexFormat.NEW_ENTITY
            );
            event.registerShader(instance);
            glowShader = instance;
            GLOW_TIME = instance.safeGetUniform("uTime");
            GLOW_VIEWPOS = instance.safeGetUniform("uViewPos");
            GLOW_COLOR = instance.safeGetUniform("uGlowColor");
            GLOW_COLOR2 = instance.safeGetUniform("uGlowColor2");
            GLOW_INTENSITY = instance.safeGetUniform("uGlowIntensity");
        } catch (IOException e) {
            Sihriya.LOGGER.error("Failed to load glow shader", e);
        }
    }

    public static ShaderInstance getGlowShader() {
        return glowShader;
    }

    public static void setGlowUniforms(float time, net.minecraft.world.phys.Vec3 viewPos,
                                        float[] color1, float[] color2, float intensity) {
        if (GLOW_TIME == null) return;
        GLOW_TIME.set(time);
        GLOW_VIEWPOS.set((float)viewPos.x, (float)viewPos.y, (float)viewPos.z);
        GLOW_COLOR.set(color1[0], color1[1], color1[2], 1.0f);
        GLOW_COLOR2.set(color2[0], color2[1], color2[2], 1.0f);
        GLOW_INTENSITY.set(intensity);
    }

    public static void onResourceReload() {
        glowShader = null;
        GLOW_TIME = null;
        GLOW_VIEWPOS = null;
        GLOW_COLOR = null;
        GLOW_COLOR2 = null;
        GLOW_INTENSITY = null;
    }
}
```

### Task 1.3: SihriyaRenderTypes extension

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/shader/SihriyaRenderTypes.java`

- [ ] **Step 1: Create new SihriyaRenderTypes for glow mesh**

```java
package tong.sihriya.vfx.shader;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Function;

public class SihriyaRenderTypes {
    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
        new RenderStateShard.TransparencyStateShard("additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
        );

    private static final Function<ResourceLocation, RenderType> GLOW_MESH =
        Util.memoize(tex -> RenderType.create(
            "sihriya_glow_mesh",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            512, false, false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(SihriyaCoreShaders::getGlowShader))
                .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(NO_OVERLAY))
                .createCompositeState(true)
        ));

    public static RenderType glowMesh(ResourceLocation texture) {
        return GLOW_MESH.apply(texture);
    }
}
```

### Task 1.4: BloomPostChain

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/post/BloomPostChain.java`

- [ ] **Step 1: Create BloomPostChain wrapper**

```java
package tong.sihriya.vfx.post;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;

import java.io.IOException;

public class BloomPostChain {
    private static BloomPostChain INSTANCE;
    private PostChain postChain;
    private boolean active;

    public static void init(Minecraft mc) {
        INSTANCE = new BloomPostChain(mc);
    }

    private BloomPostChain(Minecraft mc) {
        try {
            postChain = new PostChain(
                mc.getTextureManager(),
                mc.getResourceManager(),
                mc.getMainRenderTarget(),
                new ResourceLocation("sihriya:shaders/post/bloom.json")
            );
            postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            active = true;
        } catch (IOException e) {
            Sihriya.LOGGER.warn("Failed to load bloom post-chain", e);
            active = false;
        }
    }

    public static boolean isActive() { return INSTANCE != null && INSTANCE.active; }
    public static void setActive(boolean v) { if (INSTANCE != null) INSTANCE.active = v; }

    public static void processFrame(float partialTick) {
        if (isActive() && INSTANCE.postChain != null) {
            INSTANCE.postChain.process(partialTick);
        }
    }

    public static void onResize(int w, int h) {
        if (INSTANCE != null && INSTANCE.postChain != null) {
            INSTANCE.postChain.resize(w, h);
        }
    }
}
```

### Task 1.5: SchoolColors centralization

**Files:**
- Modify: `src/main/java/tong/sihriya/data/SchoolColors.java`
- Modify: `src/main/java/tong/sihriya/client/particle/magiccircle/MagicCircleRenderer.java`
- Modify: `src/main/java/tong/sihriya/client/ClientSetup.java`

- [ ] **Step 1: Rewrite SchoolColors as single source of truth**

```java
package tong.sihriya.data;

import java.util.Map;

public class SchoolColors {
    private static final Map<String, float[]> COLORS = Map.ofEntries(
        Map.entry("fire",       new float[]{1.0f, 0.35f, 0.05f}),
        Map.entry("water",      new float[]{0.2f, 0.5f, 1.0f}),
        Map.entry("wind",       new float[]{0.8f, 0.85f, 1.0f}),
        Map.entry("earth",      new float[]{0.35f, 0.6f, 0.2f}),
        Map.entry("lightning",  new float[]{1.0f, 0.85f, 0.1f}),
        Map.entry("ice",        new float[]{0.5f, 0.8f, 1.0f}),
        Map.entry("lava",       new float[]{1.0f, 0.2f, 0.0f}),
        Map.entry("necromancy", new float[]{0.55f, 0.0f, 0.75f}),
        Map.entry("lumamancy",  new float[]{1.0f, 0.85f, 0.4f})
    );

    private static final Map<String, float[]> COLORS_SECONDARY = Map.ofEntries(
        Map.entry("fire",       new float[]{1.0f, 0.8f, 0.2f}),
        Map.entry("water",      new float[]{0.6f, 0.85f, 1.0f}),
        Map.entry("wind",       new float[]{0.95f, 0.95f, 1.0f}),
        Map.entry("earth",      new float[]{0.6f, 0.8f, 0.4f}),
        Map.entry("lightning",  new float[]{1.0f, 1.0f, 0.5f}),
        Map.entry("ice",        new float[]{0.75f, 0.9f, 1.0f}),
        Map.entry("lava",       new float[]{1.0f, 0.6f, 0.1f}),
        Map.entry("necromancy", new float[]{0.75f, 0.3f, 1.0f}),
        Map.entry("lumamancy",  new float[]{1.0f, 0.95f, 0.6f})
    );

    public static float[] get(String schoolId) {
        return COLORS.getOrDefault(schoolId, COLORS.get("fire"));
    }

    public static float[] getSecondary(String schoolId) {
        return COLORS_SECONDARY.getOrDefault(schoolId, COLORS_SECONDARY.get("fire"));
    }
}
```

- [ ] **Step 2: Update MagicCircleRenderer to use SchoolColors.get()**
Remove the local `SCHOOL_COLORS` map. Replace all references with `SchoolColors.get(schoolId)`.

- [ ] **Step 3: Update ClientSetup particle providers to use SchoolColors**
Replace hardcoded RGB values in `RegisterParticleProvidersEvent` with `SchoolColors.get(schoolId)`.

### Task 1.6: SihriyaClientConfig — Bloom settings

**Files:**
- Modify: `src/main/java/tong/sihriya/config/SihriyaClientConfig.java`

- [ ] **Step 1: Add bloom config values**

```java
public static final ConfigValue<Boolean> VFX_BLOOM =
    CLIENT_BUILDER.comment("Enable bloom post-processing for spell effects")
        .define("vfxBloom", true);

public static final ConfigValue<Double> VFX_BLOOM_INTENSITY =
    CLIENT_BUILDER.comment("Bloom intensity (0.0 - 2.0)")
        .defineInRange("vfxBloomIntensity", 0.8, 0.0, 2.0);
```

### Task 1.7: ClientSetup — Wire everything together

**Files:**
- Modify: `src/main/java/tong/sihriya/client/ClientSetup.java`

- [ ] **Step 1: Add shader registration event**

```java
@SubscribeEvent
public static void onRegisterShaders(RegisterShadersEvent event) {
    SihriyaCoreShaders.onRegisterShaders(event);
}
```

- [ ] **Step 2: Add bloom init in FMLClientSetupEvent**

```java
@SubscribeEvent
public static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> BloomPostChain.init(Minecraft.getInstance()));
}
```

- [ ] **Step 3: Add bloom render hook — after entities, before GUI**

```java
@SubscribeEvent
public static void onRenderLevelStage(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
        BloomPostChain.processFrame(event.getPartialTick());
    }
}
```

- [ ] **Step 4: Add window resize listener for bloom**

```java
@SubscribeEvent
public static void onWindowResize(WindowResizeEvent event) {
    BloomPostChain.onResize(event.getWindow().getWidth(), event.getWindow().getHeight());
}
```

- [ ] **Step 5: Add resource reload handler**

```java
@SubscribeEvent
public static void onResourceReload(OnResourceReloadEvent event) {
    SihriyaCoreShaders.onResourceReload();
}
```

### Task 1.8: VFXDefinition records

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/VFXDefinition.java`

- [ ] **Step 1: Create all VFXDefinition records**

```java
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
```

### Task 1.9: VFXRegistry + SihriyaVFXData

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/VFXRegistry.java`
- Create: `src/main/java/tong/sihriya/vfx/data/SihriyaVFXData.java`

- [ ] **Step 1: Create VFXRegistry**

```java
package tong.sihriya.vfx;

import java.util.HashMap;
import java.util.Map;

public class VFXRegistry {
    private static final Map<String, VFXDefinition> spellOverrides = new HashMap<>();
    private static final Map<String, VFXDefinition> schoolDefaults = new HashMap<>();
    private static VFXDefinition globalDefaults;

    public static VFXDefinition get(String spellId, String schoolId) {
        VFXDefinition def = spellOverrides.get(spellId);
        if (def != null) return def;
        def = schoolDefaults.get(schoolId);
        if (def != null) return def;
        return globalDefaults;
    }

    public static void register(String spellId, VFXDefinition def) {
        spellOverrides.put(spellId, def);
    }

    public static void setSchoolDefault(String schoolId, VFXDefinition def) {
        schoolDefaults.put(schoolId, def);
    }

    public static void setGlobalDefaults(VFXDefinition def) {
        globalDefaults = def;
    }

    public static void clear() {
        spellOverrides.clear();
        schoolDefaults.clear();
        globalDefaults = null;
    }
}
```

- [ ] **Step 2: Create SihriyaVFXData loader**

```java
package tong.sihriya.vfx.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import tong.sihriya.Sihriya;
import tong.sihriya.vfx.VFXDefinition;
import tong.sihriya.vfx.VFXRegistry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SihriyaVFXData extends SimplePreparableReloadListener<Void> {
    private static final Gson GSON = new Gson();

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        VFXRegistry.clear();
        // Load from assets/sihriya/sihriya_vfx/
        String basePath = "sihriya_vfx";
        for (String school : new String[]{"fire", "water", "wind", "earth", "lightning",
                                            "ice", "lava", "necromancy", "lumamancy"}) {
            ResourceLocation loc = new ResourceLocation("sihriya", basePath + "/" + school + ".json");
            try (InputStream is = manager.getResource(loc).open();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                // Parse and register defaults + overrides
                Sihriya.LOGGER.info("Loaded VFX data for school: {}", school);
            } catch (Exception e) {
                Sihriya.LOGGER.warn("Could not load VFX data for school {}: {}", school, e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager manager, ProfilerFiller profiler) {
        Sihriya.LOGGER.info("VFX data reload complete");
    }
}
```

### Task 1.10: Wire VFX data loading

**Files:**
- Modify: `src/main/java/tong/sihriya/Sihriya.java`

- [ ] **Step 1: Register VFX data reload listener**

```java
// In commonSetup or client constructor:
net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus()
    .addListener((net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) -> {
        event.registerReloadListener(new tong.sihriya.vfx.data.SihriyaVFXData());
    });
```

### Build and verify M1

- [ ] **Step Final: Run build**

```bash
cd C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA
.\gradlew build
```

Expected: BUILD SUCCESSFUL. All shaders compile (no GLSL errors), BloomPostChain initializes, SchoolColors is now single source of truth.

---

## Milestone 2 — Mesh 3D Procédural + VFXEngine

### Task 2.1: ProceduralMesh interface + SphereMesh

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/mesh/ProceduralMesh.java`
- Create: `src/main/java/tong/sihriya/vfx/mesh/SphereMesh.java`

- [ ] **Step 1: Create ProceduralMesh interface**

```java
package tong.sihriya.vfx.mesh;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface ProceduralMesh {
    List<MeshVertex> generate();
    int vertexCount();
    void render(com.mojang.blaze3d.vertex.PoseStack stack, MultiBufferSource buffer,
                ResourceLocation texture, float[] color, float alpha, float scale, boolean fade);
}
```

- [ ] **Step 2: Create MeshVertex record**

```java
package tong.sihriya.vfx.mesh;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record MeshVertex(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
    public MeshVertex(Vec3 pos, Vec3 normal, float u, float v) {
        this((float)pos.x, (float)pos.y, (float)pos.z,
             (float)normal.x, (float)normal.y, (float)normal.z, u, v);
    }
}
```

- [ ] **Step 3: Create SphereMesh generator + cache**

```java
package tong.sihriya.vfx.mesh;

import java.util.ArrayList;
import java.util.List;

public class SphereMesh implements ProceduralMesh {
    private final float radius;
    private final int rings, segments;
    private List<MeshVertex> cached;

    public SphereMesh(float radius, int rings, int segments) {
        this.radius = radius;
        this.rings = rings;
        this.segments = segments;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        for (int i = 0; i < rings; i++) {
            float phi1 = (float)(i * Math.PI / rings);
            float phi2 = (float)((i + 1) * Math.PI / rings);

            for (int j = 0; j < segments; j++) {
                float theta1 = (float)(j * 2 * Math.PI / segments);
                float theta2 = (float)((j + 1) * 2 * Math.PI / segments);

                float x1 = radius * (float)Math.sin(phi1) * (float)Math.cos(theta1);
                float y1 = radius * (float)Math.cos(phi1);
                float z1 = radius * (float)Math.sin(phi1) * (float)Math.sin(theta1);

                float x2 = radius * (float)Math.sin(phi1) * (float)Math.cos(theta2);
                float y2 = radius * (float)Math.cos(phi1);
                float z2 = radius * (float)Math.sin(phi1) * (float)Math.sin(theta2);

                float x3 = radius * (float)Math.sin(phi2) * (float)Math.cos(theta2);
                float y3 = radius * (float)Math.cos(phi2);
                float z3 = radius * (float)Math.sin(phi2) * (float)Math.sin(theta2);

                float x4 = radius * (float)Math.sin(phi2) * (float)Math.cos(theta1);
                float y4 = radius * (float)Math.cos(phi2);
                float z4 = radius * (float)Math.sin(phi2) * (float)Math.sin(theta1);

                float nr = 1.0f / radius;
                float u1 = (float)j / segments, v1 = (float)i / rings;
                float u2 = (float)(j+1) / segments, v2 = (float)(i+1) / rings;

                verts.add(new MeshVertex(x1, y1, z1, x1*nr, y1*nr, z1*nr, u1, v1));
                verts.add(new MeshVertex(x2, y2, z2, x2*nr, y2*nr, z2*nr, u2, v1));
                verts.add(new MeshVertex(x3, y3, z3, x3*nr, y3*nr, z3*nr, u2, v2));
                verts.add(new MeshVertex(x4, y4, z4, x4*nr, y4*nr, z4*nr, u1, v2));
            }
        }
        cached = verts;
        return verts;
    }

    @Override public int vertexCount() { return cached != null ? cached.size() : generate().size(); }

    @Override
    public void render(com.mojang.blaze3d.vertex.PoseStack stack,
                       net.minecraft.client.renderer.MultiBufferSource buffer,
                       ResourceLocation texture, float[] color, float alpha,
                       float scale, boolean fade) {
        MeshRenderer.render(stack, buffer, generate(), texture, color, alpha, scale, fade);
    }
}
```

### Task 2.2: TubeMesh + TorusMesh + ConeMesh + DiskMesh

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/mesh/TubeMesh.java`
- Create: `src/main/java/tong/sihriya/vfx/mesh/TorusMesh.java`
- Create: `src/main/java/tong/sihriya/vfx/mesh/ConeMesh.java`
- Create: `src/main/java/tong/sihriya/vfx/mesh/DiskMesh.java`

Same pattern as SphereMesh — each implements ProceduralMesh with generate() and render().

- [ ] **Step 1: Create TubeMesh** — generates a tube between two Vec3 points with radius, segments, rings. Uses cross product for perpendicular vectors.

- [ ] **Step 2: Create TorusMesh** — generates a torus with major radius, minor radius, segments, rings.

- [ ] **Step 3: Create ConeMesh** — generates a cone with radius, height, segments.

- [ ] **Step 4: Create DiskMesh** — generates a flat disk with radius, rings, segments.

### Task 2.3: MeshRenderer

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/mesh/MeshRenderer.java`

- [ ] **Step 1: Create generic mesh renderer**

```java
package tong.sihriya.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tong.sihriya.vfx.shader.SihriyaRenderTypes;

import java.util.List;

public class MeshRenderer {
    public static void render(PoseStack stack, MultiBufferSource buffer,
                               List<MeshVertex> vertices, ResourceLocation texture,
                               float[] color, float alpha, float scale, boolean fade) {
        VertexConsumer consumer = buffer.getBuffer(SihriyaRenderTypes.glowMesh(texture));
        Matrix4f mat = stack.last().pose();
        Matrix3f normal = stack.last().normal();

        float s = scale;
        stack.pushPose();
        stack.scale(s, s, s);
        mat = stack.last().pose();

        for (MeshVertex v : vertices) {
            consumer.vertex(mat, v.x(), v.y(), v.z())
                .color(color[0], color[1], color[2], alpha)
                .uv(v.u(), v.v())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, v.nx(), v.ny(), v.nz())
                .endVertex();
        }
        stack.popPose();
    }
}
```

### Task 2.4: VFXEngine + VFXEffect + ObjectPool

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/VFXEngine.java`
- Create: `src/main/java/tong/sihriya/vfx/VFXEffect.java`
- Create: `src/main/java/tong/sihriya/vfx/ObjectPool.java`

- [ ] **Step 1: Create VFXEffect** — lightweight Java object with position, direction, age, lifetime, emitter list, mesh list

```java
package tong.sihriya.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.vfx.emitter.Emitter;
import tong.sihriya.vfx.mesh.ProceduralMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VFXEffect {
    private String vfxId;
    private final UUID effectId = UUID.randomUUID();
    private Vec3 position;
    private Vec3 direction;
    private int age;
    private int lifetime;
    private float intensity = 1.0f;
    private boolean active = true;

    private final List<Emitter> emitters = new ArrayList<>();
    private final List<ProceduralMesh> meshes = new ArrayList<>();

    public void tick() {
        if (!active) return;
        age++;
        if (age >= lifetime) { active = false; return; }
        for (Emitter e : emitters) e.tick();
    }

    public void render(PoseStack stack, MultiBufferSource buffer, float partialTick) {
        if (!active) return;
        for (ProceduralMesh m : meshes) {
            // m.render(stack, buffer, ...);
        }
    }

    public void reset() {
        vfxId = null;
        age = 0;
        lifetime = 0;
        intensity = 1.0f;
        active = true;
        emitters.clear();
        meshes.clear();
    }

    public boolean isFinished() { return !active || age >= lifetime; }
    public UUID getEffectId() { return effectId; }
    // ... getters and setters
}
```

- [ ] **Step 2: Create ObjectPool**

```java
package tong.sihriya.vfx;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final int maxSize;

    public ObjectPool(Supplier<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    public T acquire() {
        T obj = pool.poll();
        return obj != null ? obj : factory.get();
    }

    public void release(T obj) {
        if (pool.size() < maxSize) pool.offer(obj);
    }
}
```

- [ ] **Step 3: Create VFXEngine** — singleton with lifecycle, tick, render

```java
package tong.sihriya.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import tong.sihriya.Sihriya;
import tong.sihriya.vfx.post.BloomPostChain;

import java.util.ArrayList;
import java.util.List;

public class VFXEngine {
    private static VFXEngine INSTANCE;

    private final List<VFXEffect> activeEffects = new ArrayList<>();
    private final List<VFXEffect> pendingAdd = new ArrayList<>();
    private final ObjectPool<VFXEffect> effectPool = new ObjectPool<>(VFXEffect::new, 100);
    private final PerformanceMonitor perfMonitor = new PerformanceMonitor();

    private VFXEngine() {}

    public static void init() { INSTANCE = new VFXEngine(); }
    public static VFXEngine getInstance() { return INSTANCE; }

    public void tick() {
        perfMonitor.tick();
        activeEffects.addAll(pendingAdd);
        pendingAdd.clear();

        activeEffects.removeIf(e -> {
            e.tick();
            if (e.isFinished()) {
                effectPool.release(e);
                return true;
            }
            return false;
        });
    }

    public void render(PoseStack stack, MultiBufferSource buffer, float partialTick) {
        for (VFXEffect effect : activeEffects) {
            effect.render(stack, buffer, partialTick);
        }
    }

    public void startEffect(VFXEffect effect) {
        pendingAdd.add(effect);
    }

    public void stopEffect(java.util.UUID effectId) {
        activeEffects.removeIf(e -> e.getEffectId().equals(effectId));
    }

    public void clearAll() {
        activeEffects.clear();
        pendingAdd.clear();
    }
}
```

### Task 2.5: PerformanceMonitor

**Files:**
- Create: `src/main/java/tong/sihriya/vfx/PerformanceMonitor.java`

- [ ] **Step 1: Create auto-disable system for bloom**

```java
package tong.sihriya.vfx;

import net.minecraft.client.Minecraft;
import tong.sihriya.Sihriya;
import tong.sihriya.vfx.post.BloomPostChain;

public class PerformanceMonitor {
    private static final int CHECK_INTERVAL = 100;
    private static final int LOW_FPS_THRESHOLD = 30;

    private int tickCounter;
    private float fpsSum;

    public void tick() {
        tickCounter++;
        fpsSum += Minecraft.getInstance().getFps();

        if (tickCounter >= CHECK_INTERVAL) {
            float avgFps = fpsSum / CHECK_INTERVAL;
            if (avgFps < LOW_FPS_THRESHOLD && BloomPostChain.isActive()) {
                BloomPostChain.setActive(false);
                Sihriya.LOGGER.info("Auto-disabled bloom: avg FPS {}", avgFps);
            }
            tickCounter = 0;
            fpsSum = 0;
        }
    }
}
```

### Build and verify M2

- [ ] **Step Final: Run build**

```bash
cd C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA
.\gradlew build
```

Expected: BUILD SUCCESSFUL. Meshes generate correct vertex counts. VFXEngine ticks without errors.

---

## Milestone 3 — Émetteurs + Trails + Protocole Réseau

**Files to create:**
- `vfx/emitter/Emitter.java`
- `vfx/emitter/EmitterManager.java`
- `vfx/emitter/SpiralEmitter.java`
- `vfx/emitter/VortexEmitter.java`
- `vfx/emitter/ConeEmitter.java`
- `vfx/emitter/RingEmitter.java`
- `vfx/emitter/HelixEmitter.java`
- `vfx/emitter/BurstEmitter.java`
- `vfx/render/TrailHandler.java`
- `vfx/network/VFXTriggerPacket.java`
- `vfx/network/VFXStopPacket.java`
- `vfx/network/VFXUpdatePacket.java`

**Files to modify:**
- `core/SpellCastHandler.java`
- `network/NetworkHandler.java`

**Tasks:**
1. Create `Emitter` abstract class with `tick()`, `render()`, lifecycle management
2. Implement all 6 emitters (Spiral, Vortex, Cone, Ring, Helix, Burst)
3. Create `TrailHandler` — spawns particles behind a moving entity
4. Create `VFXTriggerPacket` — register in NetworkHandler, handle on client via VFXEngine
5. Create `VFXStopPacket` — stop effect by UUID
6. Create `VFXUpdatePacket` — update position of active effect
7. Update `SpellCastHandler` to send VFXTriggerPacket instead of vanilla particles
8. Build and verify

---

## Milestone 4 — Projectiles 3D + Beams + Lightning

**Files to create:**
- `vfx/render/BeamRenderer.java`
- `vfx/render/LightningBoltHelper.java`
- `vfx/render/ImpactHandler.java`

**Files to modify:**
- `projectile/SpellProjectile.java` — add trail in tick(), use VFX for impact
- `client/projectile/SpellProjectileRenderer.java` — use SphereMesh + cross-quads + Fresnel shader

**Tasks:**
1. Upgrade `SpellProjectileRenderer` to render with SphereMesh (volumetric 3D) instead of flat quad → use Fresnel glow shader → add rotation + pulse animation
2. Add particle trail to `SpellProjectile.tick()` — spawn glow particles every 2-3 ticks
3. Create `ImpactHandler` — burst + ring + shockwave on projectile hit
4. Create `BeamRenderer` — TubeMesh between start/end + scroll texture + Fresnel glow
5. Create `LightningBoltHelper` — midpoint displacement arc generation + branching
6. Build and verify: runClient, cast a projectile spell, see 3D orb with trail

---

## Milestone 5 — Auras, Boucliers, EffectComposer

**Files to create:**
- `vfx/composer/EffectComposer.java`
- `vfx/composer/EffectPhase.java`

**Tasks:**
1. Create `EffectComposer` — multi-phase effect scheduler (charge → cast → projectile → impact → persistent)
2. Create `EffectPhase` — single phase with emitter + mesh + trail + sound
3. Wire aura rendering (SphereMesh or TorusMesh around player for buffs)
4. Wire shield rendering (SphereMesh around caster for defensive spells)
5. Wire persistent ground effects (DiskMesh + RingEmitter for lingering area spells)
6. Build and verify

---

## Milestone 6 — Data-Driven + Polish

**Files to create:**
- `assets/sihriya/sihriya_vfx/fire.json`
- `assets/sihriya/sihriya_vfx/water.json`
- `assets/sihriya/sihriya_vfx/wind.json`
- `assets/sihriya/sihriya_vfx/earth.json`
- `assets/sihriya/sihriya_vfx/lightning.json`
- `assets/sihriya/sihriya_vfx/ice.json`
- `assets/sihriya/sihriya_vfx/lava.json`
- `assets/sihriya/sihriya_vfx/necromancy.json`
- `assets/sihriya/sihriya_vfx/lumamancy.json`

**Files to modify:**
- `core/SpellCastHandler.java` — use VFXRegistry.get() for all spell visual triggers
- `projectile/SpellProjectile.java` — read VFX config for trail parameters

**Tasks:**
1. Create JSON definitions for all 9 schools with per-school defaults
2. Add overrides for signature spells (fire.meteor, lightning.chain, ice.storm, etc.)
3. Wire VFX data loading to `SpellCastHandler.castSpell()` — read from VFXRegistry
4. Add particle fallback for machines with bloom disabled
5. Add quality levels (FAST / FANCY / FABULOUS) based on Minecraft graphics settings
6. Update GUIDE-DEV.md with VFX system documentation
7. Full build, runClient, visual regression test all 9 schools
8. Commit and push

---
