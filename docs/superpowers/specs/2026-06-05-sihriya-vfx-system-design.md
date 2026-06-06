# Design Système VFX Sihriya — Moteur de Rendu Magique 3D

> **Document de design.** Chaque section est à valider par l'utilisateur avant implémentation.
> Objectif : des effets visuels compétitifs avec les meilleurs mods de magie (Iron's Spells, Ars Nouveau).
>
> **Note :** Ars Nouveau est sous licence LGPL V3 (open source). Son code de rendu et ses systèmes de particules sont une référence librement consultable et adaptable. La plupart des techniques décrites ici s'inspirent de son approche (particules glow, émetteurs helix, shaders custom). Iron's Spells est All Rights Reserved mais le code est publiquement visible sur GitHub pour référence.

---

## Table des Matières

1. [Philosophie et Objectifs](#1-philosophie-et-objectifs)
2. [Architecture Générale](#2-architecture-générale)
3. [CoreShader Glow Fresnel](#3-coreshader-glow-fresnel)
4. [Mesh 3D Procédural](#4-mesh-3d-procédural)
5. [Système d'Émetteurs](#5-système-démetteurs)
6. [Composition d'Effets](#6-composition-deffets)
7. [Post-Processing Bloom](#7-post-processing-bloom)
8. [Système Data-Driven VFX](#8-système-data-driven-vfx)
9. [Effets par École](#9-effets-par-école)
10. [Centralisation des Couleurs](#10-centralisation-des-couleurs)
11. [Protocole Réseau VFX](#11-protocole-réseau-vfx)
12. [VFXEngine Lifecycle](#12-vfxengine-lifecycle)
13. [Plan d'Implémentation (Milestones)](#13-plan-dimplémentation-milestones)
14. [Ressources Textures](#14-ressources-textures)
15. [Performance et Optimisation](#15-performance-et-optimisation)

---

## 1. Philosophie et Objectifs

### Principes directeurs

- **Zéro sprite plat** — tout effet utilise soit du mesh 3D, soit des particules avec comportements complexes, soit des shaders
- **Chaque sort a une identité visuelle unique** — pas juste une couleur d'école différente
- **Professional-grade** — le rendu doit être compétitif avec Iron's Spells et Ars Nouveau
- **Data-driven** — les paramètres visuels sont dans des fichiers JSON, pas dans le code
- **Performance d'abord** — les effets coûteux sont réservés aux sorts de haut niveau (T4-T5)

### Ce qu'on NE fait PAS

- Pas de modèles `.json` Minecraft vanilla (trop limités)
- Pas de dépendance OptiFine (même si le bloom fonctionne mieux avec)
- Pas de particules vanilla pour les effets principaux (trop basiques)
- Pas de billboards 2D pour les projectiles principaux

### Rendu cible

```
Avant:    [Sprite 2D plat → cible] — particule glow statique
Après:    [Orbe 3D avec glow Fresnel + trail hélicoïdal → beam continu → burst anneau + onde de choc]
```

---

## 2. Architecture Générale

### Diagramme des composants

```
┌─────────────────────────────────────────────────────┐
│                   Data Layer                         │
│  spells.json  │  schools.json  │  vfx_definitions.json│
└──────────────┬────────────────┬──────────────────────┘
               │                │
               ▼                ▼
┌─────────────────────────────────────────────────────┐
│               VFX Registry (serveur + client)        │
│  - Charge les définitions VFX                        │
│  - Mappe spellId → VFXDefinition                    │
│  - Gère les fallbacks par école                      │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│               VFX Engine (runtime)                   │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Emitters  │  │  Meshes  │  │  Effect Composer  │  │
│  │ Manager   │  │  Manager │  │  (phases)         │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │  Trails   │  │ Impacts  │  │  Auras/Shields   │  │
│  │  Manager  │  │  Manager │  │  Manager          │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└──────┬──────────────────────┬───────────────────────┘
       │                      │
       ▼                      ▼
┌─────────────────┐  ┌─────────────────┐
│  Render Pipeline  │  │  Shaders        │
│  (BufferBuilder)  │  │  (CoreShader)   │
└─────────────────┘  └─────────────────┘
       │                      │
       ▼                      ▼
┌─────────────────────────────────────────────────────┐
│                 Sortie Rendu                         │
│  MultiBufferSource → Minecraft framebuffer          │
│  → PostChain Bloom → écran                          │
└─────────────────────────────────────────────────────┘
```

### Packages

```
src/main/java/tong/sihriya/vfx/
├── VFXRegistry.java           # Charge et sert les définitions VFX
├── VFXDefinition.java         # Records des paramètres visuels
├── VFXEngine.java             # Moteur runtime (lifecycle, tick)
│
├── emitter/
│   ├── EmitterManager.java    # Gère les émetteurs actifs
│   ├── Emitter.java           # Interface émetteur de particules
│   ├── SpiralEmitter.java     # Particules en spirale
│   ├── VortexEmitter.java     # Particules en vortex (tornade)
│   ├── ConeEmitter.java       # Particules en cône (cône de feu)
│   ├── RingEmitter.java       # Particules en anneau expansif
│   ├── HelixEmitter.java      # Particules en double hélice
│   └── BurstEmitter.java      # Explosion sphérique de particules
│
├── mesh/
│   ├── MeshManager.java       # Gère les meshes actifs
│   ├── ProceduralMesh.java    # Interface mesh procédural
│   ├── SphereMesh.java        # Sphère UV (orbes, boucliers)
│   ├── TubeMesh.java          # Tube (faisceaux, rayons)
│   ├── TorusMesh.java         # Anneau (boucliers, portails)
│   ├── ConeMesh.java          # Cône (projectiles)
│   └── DiskMesh.java          # Disque (cercles au sol, auras)
│
├── render/
│   ├── SihriyaCoreShaders.java    # Enregistrement des shaders custom
│   ├── SihriyaRenderTypes.java    # RenderTypes (amélioré)
│   ├── MeshRenderer.java          # Renderer générique pour meshes
│   ├── BeamRenderer.java          # Rendu de faisceau
│   ├── TrailRenderer.java         # Rendu de trail
│   └── ImpactRenderer.java        # Rendu d'impact
│
├── composer/
│   ├── EffectComposer.java    # Ordonnanceur de phases d'effet
│   ├── EffectPhase.java       # Une phase d'un effet composé
│   └── PhaseType.java         # Types de phases
│
├── post/
│   ├── BloomPostChain.java    # Post-processing bloom
│   └── shaders/               # Fichiers GLSL
│       ├── sihriya_glow.vsh   # Vertex shader glow Fresnel
│       └── sihriya_glow.fsh   # Fragment shader glow Fresnel
│
└── data/
    ├── SihriyaVFXData.java    # DataLoader pour les JSON VFX
    └── vfx_definitions/       # Dossier des définitions VFX
        ├── defaults.json      # Fallbacks par école
        ├── fire.json          # Sorts de feu
        ├── water.json         # Sorts d'eau
        ├── wind.json          # Sorts de vent
        ├── earth.json         # Sorts de terre
        ├── lightning.json     # Sorts de foudre
        ├── ice.json           # Sorts de glace
        ├── lava.json          # Sorts de lave
        ├── necromancy.json    # Sorts de nécromancie
        └── lumamancy.json     # Sorts de lumagie
```

---

## 3. CoreShader Glow Fresnel

### Concept

Un shader GLSL custom qui applique un **effet Fresnel** à toute géométrie marquée. Le Fresnel c'est : plus la surface est vue de côté, plus elle brille. C'est ce qui donne l'effet "bord lumineux" aux orbes magiques, boucliers, et projectiles.

### Vertex Shader (`sihriya_glow.vsh`)

```glsl
#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float uTime;          // temps écoulé en secondes (shader uniform custom)
uniform vec3 uViewPos;        // position de la caméra

out vec4 vertexColor;
out vec2 texCoord;
out float fresnelIntensity;
out vec3 worldNormal;

void main() {
    // Position standard
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    
    // Calcul Fresnel : angle entre le normal et le vecteur vue
    vec3 viewDir = normalize(uViewPos - Position);
    float viewAngle = max(dot(viewDir, Normal), 0.0);
    fresnelIntensity = pow(1.0 - viewAngle, 3.0);  // puissance 3 = bord très marqué
    
    // Pulsation temporelle du glow
    float pulse = 0.5 + 0.5 * sin(uTime * 2.0 + Position.y * 3.0);
    fresnelIntensity += pulse * 0.15;
    
    vertexColor = Color;
    texCoord = UV0;
    worldNormal = Normal;
}
```

### Fragment Shader (`sihriya_glow.fsh`)

```glsl
#version 150

uniform sampler2D Sampler0;
uniform float uTime;
uniform vec4 uGlowColor;      // couleur primaire du glow (école)
uniform vec4 uGlowColor2;     // deuxième couleur (dégradé)
uniform float uGlowIntensity; // intensité globale (0.0 - 2.0)

in vec4 vertexColor;
in vec2 texCoord;
in float fresnelIntensity;
in vec3 worldNormal;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord) * vertexColor;
    
    // Glow Fresnel sur les bords
    float glow = fresnelIntensity * uGlowIntensity;
    
    // Masquage : moins de glow sur les faces très éclairées par la texture
    float luminance = dot(baseColor.rgb, vec3(0.299, 0.587, 0.114));
    float mask = 1.0 - luminance * 0.5;
    
    // Dégradé de glow : centre → bord
    vec3 glowColor = mix(uGlowColor.rgb, uGlowColor2.rgb, fresnelIntensity);
    
    // Animation : micro-oscillation de la teinte du glow
    float hueShift = sin(uTime * 0.5) * 0.05;
    glowColor += vec3(hueShift);   // conversion float → vec3 correcte
    
    // Composition additive
    vec3 finalColor = baseColor.rgb + glowColor * glow * mask * baseColor.a;
    
    // Alpha : préserve la transparence du mesh
    float finalAlpha = max(baseColor.a, glow * 0.5);
    
    fragColor = vec4(finalColor, finalAlpha);
}
```

### Enregistrement du CoreShader

L'enregistrement se fait via `RegisterShadersEvent`. Les fichiers GLSL sont placés dans `assets/sihriya/shaders/core/sihriya_glow.vsh` et `sihriya_glow.fsh`.

```java
// SihriyaCoreShaders.java
public class SihriyaCoreShaders {
    public static final String GLOW_SHADER_NAME = "sihriya_glow";
    private static CoreShader glowShader;
    private static Uniform GLOW_TIME;
    private static Uniform GLOW_VIEWPOS;
    private static Uniform GLOW_COLOR;
    private static Uniform GLOW_COLOR2;
    private static Uniform GLOW_INTENSITY;
    
    // Appelé par RegisterShadersEvent (dans ClientSetup)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceLocation shaderLoc = new ResourceLocation("sihriya", GLOW_SHADER_NAME);
            ShaderInstance instance = new ShaderInstance(
                event.getResourceProvider(),
                shaderLoc,
                DefaultVertexFormat.NEW_ENTITY
            );
            event.registerShader(instance);
            glowShader = instance;
            
            // Récupération des uniforms
            GLOW_TIME = instance.safeGetUniform("uTime");
            GLOW_VIEWPOS = instance.safeGetUniform("uViewPos");
            GLOW_COLOR = instance.safeGetUniform("uGlowColor");
            GLOW_COLOR2 = instance.safeGetUniform("uGlowColor2");
            GLOW_INTENSITY = instance.safeGetUniform("uGlowIntensity");
        } catch (IOException e) {
            Sihriya.LOGGER.error("Failed to load glow shader", e);
        }
    }
    
    public static CoreShader getGlowShader() {
        return glowShader;
    }
    
    public static void setGlowUniforms(float time, Vec3 viewPos, 
                                        float[] color1, float[] color2, float intensity) {
        if (GLOW_TIME == null) return;
        GLOW_TIME.set(time);
        GLOW_VIEWPOS.set((float)viewPos.x, (float)viewPos.y, (float)viewPos.z);
        GLOW_COLOR.set(color1[0], color1[1], color1[2], 1.0f);
        GLOW_COLOR2.set(color2[0], color2[1], color2[2], 1.0f);
        GLOW_INTENSITY.set(intensity);
    }
    
    // Appelé après un resource reload (F3+T) pour rafraîchir les références
    public static void onResourceReload() {
        // Le RegisterShadersEvent est re-firé automatiquement par Forge
        // après un resource reload. glowShader est ré-assigné dans onRegisterShaders().
        // Les anciennes références deviennent invalides, d'où le null-check
        // dans setGlowUniforms() qui Skip si le shader n'est pas encore prêt.
        glowShader = null;
    }
}
```

**Dans ClientSetup.java, ajouter :**

```java
@SubscribeEvent
public static void onRegisterShaders(RegisterShadersEvent event) {
    SihriyaCoreShaders.onRegisterShaders(event);
}
```

### RenderType pour le shader glow

```java
// SihriyaRenderTypes.java (extension)
public static RenderType glowMesh(ResourceLocation texture, float intensity) {
    return RenderType.CompositeRenderType.create(
        "sihriya_glow_mesh",
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        512, false, false,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(SihriyaCoreShaders::getGlowShader))
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
            .setTransparencyState(ADDITIVE_TRANSPARENCY)  // GL_ONE / GL_ONE
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setDepthTestState(EQUAL_DEPTH)  // ou LEQUAL_DEPTH
            .createCompositeState(true)
    );
}
```

---

## 4. Mesh 3D Procédural

### Principe

Au lieu de charger des modèles depuis des fichiers, on **génère les vertices en code** à l'aide de `BufferBuilder`. Chaque type de mesh a sa propre fonction de génération qui produit des vertices avec position, normale, UV, couleur.

### SphereMesh

```java
public class SphereMesh {
    /**
     * Génère une sphère UV (latitude/longitude subdivisions)
     * @param radius    rayon de la sphère
     * @param rings     nombre d'anneaux verticaux (latitude)
     * @param segments  nombre de segments horizontaux (longitude)
     * @return liste de vertices [x, y, z, nx, ny, nz, u, v] × 4 par quad
     */
    public static List<MeshVertex> generate(float radius, int rings, int segments) {
        List<MeshVertex> vertices = new ArrayList<>();
        
        for (int i = 0; i < rings; i++) {
            float phi1 = (float)(i * Math.PI / rings);
            float phi2 = (float)((i + 1) * Math.PI / rings);
            
            for (int j = 0; j < segments; j++) {
                float theta1 = (float)(j * 2 * Math.PI / segments);
                float theta2 = (float)((j + 1) * 2 * Math.PI / segments);
                
                // 4 coins de la face quadrilatérale
                float x1 = radius * sin(phi1) * cos(theta1);
                float y1 = radius * cos(phi1);
                float z1 = radius * sin(phi1) * sin(theta1);
                
                float x2 = radius * sin(phi1) * cos(theta2);
                float y2 = radius * cos(phi1);
                float z2 = radius * sin(phi1) * sin(theta2);
                
                float x3 = radius * sin(phi2) * cos(theta2);
                float y3 = radius * cos(phi2);
                float z3 = radius * sin(phi2) * sin(theta2);
                
                float x4 = radius * sin(phi2) * cos(theta1);
                float y4 = radius * cos(phi2);
                float z4 = radius * sin(phi2) * sin(theta1);
                
                // Normales = position normalisée (sphère unitaire)
                // UV (u = longitude, v = latitude)
                float u1 = (float)j / segments;
                float v1 = (float)i / rings;
                float u2 = (float)(j + 1) / segments;
                float v2 = (float)(i + 1) / rings;
                
                vertices.add(new MeshVertex(x1, y1, z1, x1/radius, y1/radius, z1/radius, u1, v1));
                vertices.add(new MeshVertex(x2, y2, z2, x2/radius, y2/radius, z2/radius, u2, v1));
                vertices.add(new MeshVertex(x3, y3, z3, x3/radius, y3/radius, z3/radius, u2, v2));
                vertices.add(new MeshVertex(x4, y4, z4, x4/radius, y4/radius, z4/radius, u1, v2));
            }
        }
        return vertices;
    }
}
```

### TubeMesh (pour les faisceaux)

```java
public class TubeMesh {
    /**
     * Génère un tube entre deux points
     * @param start     point de départ
     * @param end       point d'arrivée
     * @param radius    rayon du tube
     * @param segments  nombre de segments autour du tube
     * @param rings     nombre d'anneaux le long du tube
     */
    public static List<MeshVertex> generate(Vec3 start, Vec3 end, 
                                            float radius, int segments, int rings) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 up = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 forward = right.cross(direction).normalize();
        
        List<MeshVertex> vertices = new ArrayList<>();
        
        for (int i = 0; i < rings - 1; i++) {
            float t0 = (float)i / (rings - 1);
            float t1 = (float)(i + 1) / (rings - 1);
            
            Vec3 center0 = start.add(end.subtract(start).scale(t0));
            Vec3 center1 = start.add(end.subtract(start).scale(t1));
            
            // Rayon variable le long du tube (parabole = effet cigare)
            float r0 = radius * (1 - 4 * (t0 - 0.5f) * (t0 - 0.5f));
            float r1 = radius * (1 - 4 * (t1 - 0.5f) * (t1 - 0.5f));
            
            for (int j = 0; j < segments; j++) {
                float theta1 = (float)(j * 2 * Math.PI / segments);
                float theta2 = (float)((j + 1) * 2 * Math.PI / segments);
                
                // 4 coins du quad : deux sur l'anneau courant, deux sur l'anneau suivant
                Vec3 p00 = center0.add(right.scale(r0 * (float)Math.cos(theta1)))
                                  .add(forward.scale(r0 * (float)Math.sin(theta1)));
                Vec3 p01 = center0.add(right.scale(r0 * (float)Math.cos(theta2)))
                                  .add(forward.scale(r0 * (float)Math.sin(theta2)));
                Vec3 p10 = center1.add(right.scale(r1 * (float)Math.cos(theta1)))
                                  .add(forward.scale(r1 * (float)Math.sin(theta1)));
                Vec3 p11 = center1.add(right.scale(r1 * (float)Math.cos(theta2)))
                                  .add(forward.scale(r1 * (float)Math.sin(theta2)));
                
                // Normale = direction centre→point, normalisée
                Vec3 n00 = p00.subtract(center0).normalize();
                Vec3 n01 = p01.subtract(center0).normalize();
                Vec3 n10 = p10.subtract(center1).normalize();
                Vec3 n11 = p11.subtract(center1).normalize();
                
                float u1 = (float)j / segments;
                float u2 = (float)(j + 1) / segments;
                float v0 = t0;
                float v1 = t1;
                
                vertices.add(new MeshVertex(p00, n00, u1, v0));
                vertices.add(new MeshVertex(p01, n01, u2, v0));
                vertices.add(new MeshVertex(p11, n11, u2, v1));
                vertices.add(new MeshVertex(p10, n10, u1, v1));
            }
        }
        return vertices;
    }
}
```

### Autres meshes

| Mesh | Usage | Paramètres |
|------|-------|------------|
| **TorusMesh** | Anneaux, boucliers, portails | `radius`, `tube`, `segments`, `rings` |
| **ConeMesh** | Cônes de feu/glace, projectiles pointus | `radius`, `height`, `segments` |
| **DiskMesh** | Cercles au sol, auras, marques | `radius`, `rings`, `segments` |
| **HelixMesh** | Trails en hélice, spires lumineuses | `radius`, `height`, `turns`, `segments` |
| **IcosahedronMesh** | Boucliers facets, cristaux | `radius`, `subdivisions` |

### Rendu des meshes

```java
// MeshRenderer.java — renderer générique
public class MeshRenderer {
    
    public static void render(PoseStack stack, MultiBufferSource buffer,
                              List<MeshVertex> vertices, ResourceLocation texture,
                              float[] color, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(
            SihriyaRenderTypes.glowMesh(texture, 1.0f));
        Matrix4f mat = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        
        for (MeshVertex v : vertices) {
            consumer.vertex(mat, v.x, v.y, v.z)
                .color(color[0], color[1], color[2], alpha)
                .uv(v.u, v.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, v.nx, v.ny, v.nz)
                .endVertex();
        }
    }
}
```

---

## 5. Système d'Émetteurs

### Principe

Les émetteurs produisent des particules (des `VFXEffect` objets Java légers gérés par `VFXEngine`, PAS des `Entity` Minecraft ni des `Particle` vanilla) avec des comportements définis. Chaque effet a une lifecycle : spawn → évolution → mort → éventuellement spawn secondaire. Voir section 12 pour l'architecture complète du cycle de vie.

### Architecture

```java
public abstract class Emitter {
    protected final Level level;
    protected final Vec3 position;
    protected final String schoolId;
    protected int age;          // ticks depuis le début
    protected int lifetime;     // durée de vie en ticks
    protected boolean active;
    
    public abstract void tick();                    // spawn + update des particules
    public abstract void render(PoseStack stack, MultiBufferSource buffer, float partialTick);
    
    // Méthodes utilitaires
    protected void spawnParticle(Vec3 pos, Vec3 vel, float size, int lifetime, float[] color) { }
    protected void spawnTrail(Vec3 from, Vec3 to, int count, float spread) { }
}
```

### SpiralEmitter

```java
public class SpiralEmitter extends Emitter {
    private float radius;
    private float height;
    private float speed;
    private int particlesPerTick;
    
    @Override
    public void tick() {
        if (age >= lifetime) { active = false; return; }
        
        float progress = (float)age / lifetime;
        
        for (int i = 0; i < particlesPerTick; i++) {
            float angle = age * speed + (float)i / particlesPerTick * (float)Math.PI * 2;
            float yOffset = progress * height;
            float r = radius * (1 - progress * 0.3f); // resserrement progressif
            
            Vec3 pos = position.add(
                Math.cos(angle) * r,
                yOffset,
                Math.sin(angle) * r
            );
            
            Vec3 vel = new Vec3(
                -Math.sin(angle) * 0.02,
                0.01,
                Math.cos(angle) * 0.02
            );
            
            float particleLife = 20 + random.nextInt(10);
            float alpha = 1 - progress; // fade out progressif
            spawnParticle(pos, vel, 0.3f, (int)particleLife, getSchoolColor());
        }
    }
}
```

### VortexEmitter

```java
public class VortexEmitter extends Emitter {
    // Particules qui tournent en se resserrant vers le centre
    // Effet : tornade, aspiration, portail
    // Paramètres : radius, height, speed, contractionSpeed, particlesPerTick
    
    @Override
    public void tick() {
        for (int i = 0; i < particlesPerTick; i++) {
            float angle = age * speed + i * 1.5f;
            float r = radius * (1 - (float)age / lifetime * contractionSpeed);
            float y = height * (float)age / lifetime;
            
            Vec3 pos = position.add(
                cos(angle) * r,
                y,
                sin(angle) * r
            );
            
            // Particule avec mouvement vers le haut et vers le centre
            spawnParticle(pos, 
                new Vec3(-cos(angle) * 0.03, 0.05, -sin(angle) * 0.03),
                0.2f + r / radius * 0.3f,
                15,
                getSchoolColor());
        }
    }
}
```

### ConeEmitter

```java
public class ConeEmitter extends Emitter {
    // Particules projetées dans un cône (cône de feu, souffle)
    // Paramètres : angle, speed, spread, count
    
    @Override
    public void tick() {
        for (int i = 0; i < count; i++) {
            float theta = random.nextFloat() * (float)Math.PI * 2;
            float phi = random.nextFloat() * angle - angle / 2;
            
            Vec3 dir = new Vec3(
                cos(theta) * sin(phi),
                cos(phi),
                sin(theta) * sin(phi)
            ).normalize();
            
            float spd = speed * (0.5f + random.nextFloat() * 0.5f);
            
            spawnParticle(
                position,
                dir.scale(spd),
                0.2f + random.nextFloat() * 0.3f,
                10 + random.nextInt(15),
                getSchoolColor()
            );
        }
    }
}
```

### RingEmitter

```java
public class RingEmitter extends Emitter {
    // Anneau de particules qui s'expand (onde de choc, burst)
    // Paramètres : startRadius, endRadius, speed, particles
    
    @Override
    public void tick() {
        float progress = (float)age / lifetime;
        float r = startRadius + (endRadius - startRadius) * progress;
        float alpha = 1 - progress; // fade au fur et à mesure
        
        for (int i = 0; i < particles; i++) {
            float theta = (float)i / particles * (float)Math.PI * 2;
            
            Vec3 pos = position.add(
                cos(theta) * r,
                0.1f,
                sin(theta) * r
            );
            
            spawnParticle(pos, Vec3.ZERO, 0.15f, 10, 
                new float[]{color[0], color[1], color[2]});
        }
    }
}
```

### HelixEmitter

```java
public class HelixEmitter extends Emitter {
    // Double hélice lumineuse (pour projectiles, auras)
    // Paramètres : radius, height, turns, speed
    
    @Override
    public void tick() {
        float angle = age * speed;
        float baseY = (float)age / lifetime * height;
        
        // Hélice 1
        Vec3 pos1 = position.add(
            cos(angle) * radius,
            baseY,
            sin(angle) * radius
        );
        spawnParticle(pos1, new Vec3(0, 0.01, 0), 0.2f, 15, getSchoolColor());
        
        // Hélice 2 (déphasée de 180°)
        Vec3 pos2 = position.add(
            cos(angle + PI) * radius,
            baseY,
            sin(angle + PI) * radius
        );
        spawnParticle(pos2, new Vec3(0, 0.01, 0), 0.2f, 15, getSchoolColor2());
    }
}
```

### BurstEmitter

```java
public class BurstEmitter extends Emitter {
    // Explosion sphérique de particules
    // Paramètres : count, speed, spread, innerParticles
    
    @Override
    public void tick() {
        if (age > 5) { active = false; return; } // burst est instantané
        
        for (int i = 0; i < count; i++) {
            float theta = random.nextFloat() * PI * 2;
            float phi = acos(2 * random.nextFloat() - 1);
            float spd = speed * (0.3f + random.nextFloat() * 0.7f);
            
            Vec3 dir = new Vec3(
                sin(phi) * cos(theta),
                sin(phi) * sin(theta),
                cos(phi)
            );
            
            spawnParticle(
                position,
                dir.scale(spd),
                0.1f + random.nextFloat() * 0.4f,
                10 + random.nextInt(20),
                lerpColor(getSchoolColor(), getSchoolColor2(), random.nextFloat())
            );
        }
    }
}
```

---

## 6. Composition d'Effets

### Principe

Un effet de sort est une **séquence de phases** qui s'exécutent dans le temps. Chaque phase peut lancer des émetteurs, des meshes, des particules, des trails, des impacts.

### EffectComposer

```java
public class EffectComposer {
    private List<EffectPhase> phases;
    private int currentPhase;
    private int globalTick;
    
    public void tick() {
        globalTick++;
        
        EffectPhase phase = getCurrentPhase();
        if (phase != null) {
            phase.tick(globalTick - phase.startTick);
            
            if (phase.isFinished(globalTick)) {
                currentPhase++;
            }
        }
    }
    
    public void render(PoseStack stack, MultiBufferSource buffer, float partialTick) {
        for (EffectPhase phase : phases) {
            if (phase.isActive(globalTick)) {
                phase.render(stack, buffer, partialTick, globalTick);
            }
        }
    }
}
```

### EffectPhase

```java
public class EffectPhase {
    private PhaseType type;
    private int startTick;       // tick de début relatif au début de l'effet
    private int duration;        // durée en ticks
    private EmitterConfig emitter;
    private MeshConfig mesh;
    private TrailConfig trail;
    private SoundConfig sound;
    private ImpactConfig impact;
    
    public enum PhaseType {
        CHARGE,          // build-up avant le lancer
        CAST,            // moment du lancer
        PROJECTILE,      // projectile en vol
        BEAM,            // faisceau continu
        IMPACT,          // à l'impact
        PERSISTENT,      // effet au sol persistant
        AURA,            // aura autour du joueur
        DEATH            // effet de disparition
    }
}
```

### Configuration JSON d'un effet composé

```json
{
  "spellId": "fire.meteor",
  "phases": [
    {
      "type": "CHARGE",
      "startTick": 0,
      "duration": 30,
      "emitter": {
        "type": "spiral",
        "radius": 1.5,
        "height": 2.0,
        "speed": 0.15,
        "particlesPerTick": 4
      },
      "mesh": {
        "type": "sphere",
        "radius": 0.3,
        "scaleStart": 1.0, "scaleEnd": 3.0,
        "color": [1.0, 0.4, 0.05]
      },
      "sound": "sihriya:fire_charge"
    },
    {
      "type": "PROJECTILE",
      "startTick": 30,
      "duration": 60,
      "emitter": {
        "type": "helix",
        "radius": 0.5,
        "height": 0.8,
        "speed": 0.2,
        "particlesPerTick": 3
      },
      "trail": {
        "particlesPerTick": 2,
        "lifetime": 15,
        "spread": 0.05,
        "fade": true
      },
      "mesh": {
        "type": "sphere",
        "radius": 0.5,
        "color": [1.0, 0.4, 0.05],
        "glowIntensity": 1.5
      }
    },
    {
      "type": "IMPACT",
      "startTick": 90,
      "duration": 20,
      "emitter": {
        "type": "burst",
        "count": 40,
        "speed": 0.8,
        "spread": 0.5
      },
      "mesh": {
        "type": "disk",
        "radius": 0.5,
        "scaleStart": 1.0, "scaleEnd": 6.0,
        "fade": true
      },
      "impact": {
        "shockwave": true,
        "shockwaveRings": 3,
        "debris": 10
      },
      "sound": "sihriya:fire_explosion"
    },
    {
      "type": "PERSISTENT",
      "startTick": 110,
      "duration": 100,
      "emitter": {
        "type": "ring",
        "startRadius": 3.0,
        "endRadius": 4.0,
        "speed": 0.2,
        "particles": 16
      },
      "mesh": {
        "type": "disk",
        "radius": 3.0,
        "alpha": 0.2
      }
    }
  ]
}
```

---

## 7. Post-Processing Bloom

### Concept

Le bloom (ou glow) est l'effet où les zones très lumineuses de l'image "débordent" sur les zones sombres autour. C'est ce qui fait que les orbes magiques, les faisceaux, et les particules brillantes semblent vraiment émettre de la lumière.

### Implémentation

```java
public class BloomPostChain {
    private static BloomPostChain INSTANCE;
    
    private PostChain postChain;
    private boolean active;
    
    // Le PostChain applique les passes suivantes :
    // 1. Extract Bright : isole les pixels > seuil de luminance
    // 2. Blur (Gaussian) : 4 passes (2 horizontales + 2 verticales)
    // 3. Combine : Additionne le blur à l'image originale
    
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
    
    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.active;
    }
    
    public static void setActive(boolean value) {
        if (INSTANCE != null) INSTANCE.active = value;
    }
    
    public static void processFrame(float partialTick) {
        if (isActive() && INSTANCE.postChain != null) {
            INSTANCE.postChain.process(partialTick);
        }
    }
    
    public static void onResize(int width, int height) {
        if (INSTANCE != null && INSTANCE.postChain != null) {
            INSTANCE.postChain.resize(width, height);
        }
    }
}
```

### Configuration JSON du PostChain (`assets/sihriya/shaders/post/bloom.json`)

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
            "auxtargets": ["brightPass", "blurVertical1", "blurVertical2"],
            "uniforms": [
                { "name": "BloomIntensity", "values": [0.8] }
            ]
        }
    ]
}
```

### Shaders GLSL pour le bloom

**bright.fsh :**
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

**blur.fsh :**
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

### Fichiers de passes JSON requis

Le PostChain bloom nécessite les fichiers suivants dans `assets/sihriya/shaders/post/` :

| Fichier | Type | Description |
|---------|------|-------------|
| `bloom.json` | Chaînage PostChain | Orchestre les passes bright→blur→combine |
| `bright.json` | Définition de passe | Isole les pixels lumineux (seuil 0.6) |
| `bright.fsh` | Fragment shader | Calcule la luminance et filtre |
| `combine.json` | Définition de passe | Fusionne l'original + bright + blur |
| `combine.fsh` | Fragment shader | Addition pondérée des 3 textures |
| `blur.fsh` | Fragment shader | Gaussian blur (horizontal/vertical) — peut réutiliser le blur vanilla existant |

**Exemple de `bright.json` :**
```json
{
    "targets": [],
    "passes": [{
        "name": "bright",
        "intarget": "minecraft:main",
        "outtarget": "minecraft:main",
        "auxtargets": [],
        "uniforms": [
            { "name": "BrightThreshold", "values": [0.6] },
            { "name": "BrightMultiplier", "values": [1.5] }
        ]
    }]
}
```

**Exemple de `combine.json` :**
```json
{
    "targets": ["brightPass", "blurVertical1", "blurVertical2"],
    "passes": [{
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
    }]
}
```

### Activation/désactivation

```java
// SihriyaClientConfig.java
public static final ConfigValue<Boolean> VFX_BLOOM = 
    CLIENT_BUILDER.comment("Enable bloom post-processing for spell effects")
        .define("vfxBloom", true);

public static final ConfigValue<Float> VFX_BLOOM_INTENSITY =
    CLIENT_BUILDER.comment("Bloom intensity (0.0 - 2.0)")
        .defineInRange("vfxBloomIntensity", 0.8f, 0.0f, 2.0f);
```

---

## 8. Système Data-Driven VFX

### Format des fichiers

Les définitions VFX sont stockées dans `assets/sihriya/sihriya_vfx/` en JSON.

### VFXDefinition (record Java)

```java
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
    SoundConfig sound
) {
    public record ProjectileConfig(
        float scale,                // taille du projectile
        String meshType,            // "sphere", "cone", "icosahedron"
        int meshDetail,             // subdivisions du mesh
        float glowIntensity,        // 0.0 - 3.0
        float[] color1,             // couleur primaire
        float[] color2,             // couleur secondaire (dégradé)
        float rotationSpeed,        // degrés/tick
        float pulseSpeed,           // fréquence de pulsation
        float pulseAmount,          // amplitude de pulsation
        EmitterConfig trail,        // trail pendant le vol
        EmitterConfig aura,         // aura autour du projectile
        boolean volumetric          // true = vrai mesh, false = cross-quads
    ) {}
    
    public record BeamConfig(
        float radius,               // épaisseur du faisceau
        int segments,               // segments autour du tube
        int rings,                  // anneaux le long du tube
        float[] color1,
        float[] color2,
        float glowIntensity,
        boolean scrolling,          // texture qui défile
        float scrollSpeed,
        boolean noise,              // perturbation du tube (effet instable)
        float noiseAmount
    ) {}
    
    public record ImpactConfig(
        EmitterConfig burst,        // explosion de particules
        boolean shockwave,          // activer l'onde de choc
        int shockwaveRings,         // nombre d'anneaux (ringCount dans JSON)
        float shockwaveSpeed,
        float shockwaveRadius,
        int debris,                 // nombre de débris projetés
        boolean groundMark,         // marque au sol persistante
        int groundMarkDuration,
        boolean screenShake,        // shake caméra (client uniquement)
        float screenShakeIntensity,
        float[] color1,
        float[] color2
    ) {}
    
    public record MeshConfig(
        String type,                // "sphere", "tube", "torus", "cone", "disk", "helix"
        float radius,
        float height,
        int segments,
        int rings,
        float[] color,
        float alpha,
        float glowIntensity,
        float scaleStart,           // échelle au début (lerp vers scaleEnd)
        float scaleEnd,             // échelle à la fin (lerp)
        boolean fade                // fondre l'alpha progressivement
    ) {}

    public record ChargeConfig(
        int duration,               // ticks de charge
        EmitterConfig emitter,
        MeshConfig mesh,
        boolean soundLoop,
        float scaleStart,
        float scaleEnd
    ) {}
    
    public record AuraConfig(
        float radius,
        MeshConfig mesh,
        EmitterConfig emitter,
        boolean followEntity,
        int lifetime
    ) {}
    
    public record PersistentConfig(
        int duration,
        MeshConfig mesh,
        EmitterConfig emitter,
        boolean groundDecal,
        float decalAlpha
    ) {}
    
    public record TrailConfig(
        int particlesPerTick,
        int particleLifetime,
        float spread,
        boolean fade,
        String emitterType,         // "helix", "cone", "ring", "simple"
        float radius,
        float speed
    ) {}
    
    public record EmitterConfig(
        String type,                // "spiral", "vortex", "cone", "ring", "helix", "burst"
        int count,
        float speed,
        float spread,
        float radius,
        float startRadius,          // pour RingEmitter : rayon initial
        float endRadius,            // pour RingEmitter : rayon final
        float height,
        float angle,
        float contractionSpeed,     // pour VortexEmitter : vitesse de resserrement
        int particlesPerTick,
        int particleLifetime
    ) {}
    
    public record SoundConfig(
        String charge,
        String cast,
        String impact,
        String loop
    ) {}
}
```

### Exemple de fichier VFX pour les sorts de feu

```json
{
  "school": "fire",
  "defaults": {
    "projectile": {
      "scale": 1.0,
      "meshType": "sphere",
      "meshDetail": 16,
      "glowIntensity": 1.5,
      "color1": [1.0, 0.4, 0.05],
      "color2": [1.0, 0.8, 0.2],
      "rotationSpeed": 3.0,
      "pulseSpeed": 0.15,
      "pulseAmount": 0.2,
      "volumetric": true,
      "trail": {
        "type": "helix",
        "particlesPerTick": 3,
        "particleLifetime": 12,
        "radius": 0.3,
        "speed": 0.15
      }
    },
    "impact": {
      "burst": {
        "type": "burst",
        "count": 25,
        "speed": 0.6,
        "spread": 0.4
      },
      "shockwaveRings": 2,
      "shockwaveSpeed": 0.8,
      "shockwaveRadius": 4.0,
      "groundMark": true,
      "groundMarkDuration": 60,
      "screenShake": true,
      "screenShakeIntensity": 0.3,
      "color1": [1.0, 0.4, 0.05],
      "color2": [1.0, 0.6, 0.1]
    }
  },
  "overrides": {
    "fire.fireball": {
      "projectile": {
        "scale": 1.5,
        "glowIntensity": 2.0,
        "trail": {
          "particlesPerTick": 5,
          "radius": 0.5
        }
      },
      "impact": {
        "screenShakeIntensity": 0.5,
        "burst": {
          "count": 40,
          "speed": 0.8
        }
      }
    },
    "fire.meteor": {
      "phases": [
        {
          "type": "CHARGE",
          "startTick": 0,
          "duration": 40,
          "emitter": { "type": "spiral", "radius": 2.0, "height": 3.0, "speed": 0.1, "particlesPerTick": 6 },
"mesh": { "type": "sphere", "radius": 0.2, "scaleStart": 1.0, "scaleEnd": 4.0, "color": [1.0, 0.4, 0.05] }
        },
        {

        },
        {
          "type": "PROJECTILE",
          "startTick": 40,
          "duration": 80,
          "emitter": { "type": "helix", "radius": 0.6, "height": 0.5, "speed": 0.2, "particlesPerTick": 5 },
          "trail": { "particlesPerTick": 4, "lifetime": 20, "spread": 0.1 },
          "mesh": { "type": "sphere", "radius": 0.8, "color": [1.0, 0.4, 0.05], "glowIntensity": 2.0 }
        },
        {
          "type": "IMPACT",
          "startTick": 120,
          "duration": 30,
          "emitter": { "type": "burst", "count": 60, "speed": 1.2, "spread": 0.7 },
          "mesh": { "type": "disk", "radius": 0.5, "scaleStart": 1.0, "scaleEnd": 6.0, "fade": true },
          "impact": { "shockwave": true, "shockwaveRings": 3, "debris": 15, "screenShake": true, "screenShakeIntensity": 0.8 }
        }
      ]
    },
    "fire.wall": {
      "phases": [
        {
          "type": "CAST",
          "startTick": 0,
          "duration": 10,
          "emitter": { "type": "ring", "startRadius": 0.5, "endRadius": 3.0, "speed": 0.5, "particles": 20 }
        },
        {
          "type": "PERSISTENT",
          "startTick": 10,
          "duration": 200,
          "emitter": { "type": "cone", "angle": 0.3, "speed": 0.2, "count": 8 },
          "mesh": { "type": "disk", "radius": 4.0, "color": [1.0, 0.4, 0.05], "alpha": 0.05 }
        }
      ]
    }
  }
}
```

### Fallback par école

Si un sort n'a pas de définition VFX spécifique, le système utilise les `defaults` de son école. Si l'école n'a pas de defaults non plus, on utilise les defaults globaux.

```java
public VFXDefinition getDefinition(String spellId, String schoolId) {
    // 1. Chercher override spécifique au sort
    VFXDefinition def = spellOverrides.get(spellId);
    if (def != null) return def;
    
    // 2. Chercher defaults de l'école
    def = schoolDefaults.get(schoolId);
    if (def != null) return def;
    
    // 3. Fallback global
    return globalDefaults;
}
```

---

## 9. Effets par École

Chaque école a une identité visuelle unique qui va au-delà de la simple couleur.

### 🔥 Feu (rouge-orangé-jaune)
| Aspect | Description |
|--------|-------------|
| **Particules** | Étincelles, braises, tourbillons de feu |
| **Projectile** | Sphère de feu rugueuse (mesh avec perturbation de normale) |
| **Trail** | Particules qui montent en spiralant, fumée légère |
| **Impact** | Explosion, onde de choc, anneau de feu au sol |
| **Beam** | Jet continu de flammes (cône de feu) |
| **Marque au sol** | Cercle de braise qui rougeoie |
| **Son** | Crépitement, souffle, explosion |
| **Shake** | Oui, fort |

### 💧 Eau (bleu-cyan-blanc)
| Aspect | Description |
|--------|-------------|
| **Particules** | Gouttelettes, bulles, éclaboussures |
| **Projectile** | Sphère liquide translucide (mesh avec surface ondulante) |
| **Trail** | Gouttes qui retombent |
| **Impact** | Éclaboussure, anneau liquide, brume au sol |
| **Beam** | Jet d'eau sous pression |
| **Marque au sol** | Flaque d'eau qui miroite |
| **Son** | Éclaboussure, ruissellement |

### 🌪️ Vent (blanc-argent-transparent)
| Aspect | Description |
|--------|-------------|
| **Particules** | Traits de vent, feuilles, poussière |
| **Projectile** | Tourbillon d'air comprimé (helix serrée) |
| **Trail** | Sillage de vent en spirale |
| **Impact** | Rafale, cercle de vent qui s'expand, poussière |
| **Beam** | Tornade, souffle |
| **Marque au sol** | Tourbillon de vent qui tourne |
| **Son** | Vent, whoosh, sifflement |

### 🪨 Terre (brun-vert-gris)
| Aspect | Description |
|--------|-------------|
| **Particules** | Cailloux, poussière, éclats de roche |
| **Projectile** | Rocher irrégulier (icosahedron avec perturbation) |
| **Trail** | Particules qui retombent |
| **Impact** | Éboulement, onde de choc, cratère |
| **Beam** | Jet de pierres (cône) |
| **Marque au sol** | Fissures lumineuses, cercle de pierre |
| **Son** | Grondement, craquement |

### ⚡ Foudre (jaune vif-blanc-bleu électrique)
| Aspect | Description |
|--------|-------------|
| **Particules** | Étincelles, arcs électriques |
| **Projectile** | Sphère électrique crépitante (mesh avec noise d'instabilité) |
| **Trail** | Mini-éclairs qui bifurquent |
| **Impact** | Explosion électrique, arcs qui cherchent les cibles proches |
| **Beam** | Éclair continu (tube noise avec branches) |
| **Marque au sol** | Cercle électrique crépitant |
| **Son** | Craquement électrique, bourdonnement |
| **Shake** | Oui, violent |

### 🧊 Glace (bleu pâle-blanc-violet glacial)
| Aspect | Description |
|--------|-------------|
| **Particules** | Cristaux, givre, poudre de glace |
| **Projectile** | Cristal de glace facetté (icosahedron avec reflets) |
| **Trail** | Poudre de glace qui scintille et retombe |
| **Impact** | Explosion de cristaux, givre au sol, stalagmites de glace |
| **Beam** | Souffle glacial (cône de givre) |
| **Marque au sol** | Givre qui s'étend en motifs géométriques |
| **Son** | Craquement de glace, vent froid |

### 🌋 Lave (rouge foncé-orange-braises)
| Aspect | Description |
|--------|-------------|
| **Particules** | Braises, fumée épaisse, étincelles |
| **Projectile** | Boule de lave visqueuse (mesh avec surface qui ondule) |
| **Trail** | Gouttes de lave qui retombent en brûlant |
| **Impact** | Explosion de lave, flaques en fusion, fumée |
| **Beam** | Jet de lave continu |
| **Marque au sol** | Flaque de lave qui rougeoie |
| **Son** | Bouillonnement, grondement profond |
| **Shake** | Oui, constant |

### 💀 Nécromancie (violet foncé-noir-vert spectral)
| Aspect | Description |
|--------|-------------|
| **Particules** | Âmes, ombres, particules violettes montantes |
| **Projectile** | Orbe spectral avec âmes tournoyantes à l'intérieur |
| **Trail** | Particules d'ombre qui se dispersent |
| **Impact** | Explosion d'âmes, cercle de runes sombres |
| **Beam** | Rayon d'ombre qui draine la vie |
| **Marque au sol** | Cercle de runes nécromantiques |
| **Son** | Murmures, cris lointains, résonance grave |

### ✨ Lumagie (blanc-arc-en-ciel-or)
| Aspect | Description |
|--------|-------------|
| **Particules** | Étoiles, poudre lumineuse, scintillements |
| **Projectile** | Sphère de lumière pure (mesh brillant, dégradé arc-en-ciel) |
| **Trail** | Particules arc-en-ciel qui montent |
| **Impact** | Explosion de lumière, halo blanc, particules arc-en-ciel |
| **Beam** | Rayon de lumière sacrée, pilier lumineux |
| **Marque au sol** | Cercle lumineux avec symboles sacrés |
| **Son** | Carillon, harmonie, souffle léger |

---

## 10. Centralisation des Couleurs

### Problème

Actuellement, les couleurs des écoles sont définies à **3 endroits différents** avec des valeurs qui diffèrent légèrement :

| Source | Feu (R, G, B) |
|--------|---------------|
| `SchoolColors.java` | `[1.0, 0.35, 0.05]` |
| `MagicCircleRenderer.SCHOOL_COLORS` | `[1.0, 0.4, 0.1]` |
| `ClientSetup` particle providers | `[1.0, 0.4, 0.1]` |

### Solution

`SchoolColors.java` devient la **source unique de vérité**. Tous les renderers (magic circle, projectile, VFX) lisent depuis cette classe.

```java
// SchoolColors.java — source unique
public class SchoolColors {
    private static final Map<String, float[]> COLORS = Map.of(
        "fire",       new float[]{1.0f, 0.35f, 0.05f},
        "water",      new float[]{0.2f, 0.5f, 1.0f},
        "wind",       new float[]{0.8f, 0.85f, 1.0f},
        "earth",      new float[]{0.35f, 0.6f, 0.2f},
        "lightning",  new float[]{1.0f, 0.85f, 0.1f},
        "ice",        new float[]{0.5f, 0.8f, 1.0f},
        "lava",       new float[]{1.0f, 0.2f, 0.0f},
        "necromancy", new float[]{0.55f, 0.0f, 0.75f},
        "lumamancy",  new float[]{1.0f, 0.85f, 0.4f}
    );
    
    public static float[] get(String schoolId) {
        return COLORS.getOrDefault(schoolId, COLORS.get("fire"));
    }
    
    // Couleur secondaire (plus claire, utilisée pour les dégradés de glow)
    private static final Map<String, float[]> COLORS2 = Map.of(
        "fire",       new float[]{1.0f, 0.8f, 0.2f},
        "water",      new float[]{0.6f, 0.85f, 1.0f},
        // ...
    );
    
    public static float[] getSecondary(String schoolId) { ... }
}
```

**MagicCircleRenderer** et **ClientSetup** sont modifiés pour utiliser `SchoolColors.get()` au lieu de leurs propres maps. Les VFX definitions peuvent override une couleur si nécessaire (par exemple, un sort spécifique peut avoir une teinte différente).

---

## 11. Protocole Réseau VFX

### Problème

Actuellement, les effets visuels sont déclenchés via `ClientboundLevelParticlesPacket` (particules vanilla) — c'est du "fire-and-forget" : pas de gestion de lifecycle, pas de suivi d'entité, pas d'annulation.

Le nouveau système VFX a besoin d'un protocole réseau pour que le serveur commande au client de démarrer/arrêter/mettre à jour des effets.

### Packets

```java
// VFXTriggerPacket — démarrer un effet
public class VFXTriggerPacket {
    private String vfxId;        // identifiant de la définition VFX
    private String spellId;      // sort qui déclenche
    private UUID targetEntity;   // entité cible (optionnel)
    private Vec3 position;       // position
    private Vec3 direction;      // direction
    private int duration;        // durée en ticks (0 = durée par défaut)
    private float intensity;     // multiplicateur d'intensité (1.0 = normal)
}
// → Client: démarre un effet via VFXEngine.startEffect(vfxId, pos, dir, ...)

// VFXStopPacket — arrêter un effet
public class VFXStopPacket {
    private UUID effectId;       // identifiant de l'effet côté client
}
// → Client: arrête et cleanup l'effet via VFXEngine.stopEffect(effectId)

// VFXUpdatePacket — mettre à jour la position d'un effet suivi
public class VFXUpdatePacket {
    private UUID effectId;
    private Vec3 newPosition;
    private Vec3 newDirection;
}
// → Client: déplace l'effet existant
```

```java
// NetworkHandler.java — enregistrement des packets
public static void register() {
    int id = 0;
    INSTANCE.registerPacket(
        VFXTriggerPacket.class,
        VFXTriggerPacket::encode,
        VFXTriggerPacket::decode,
        VFXTriggerPacket::handle,
        id++
    );
    INSTANCE.registerPacket(VFXStopPacket.class, ...);
    INSTANCE.registerPacket(VFXUpdatePacket.class, ...);
}
```

### Flux typique

```
1. Joueur caste un sort de projectile
2. Serveur : crée SpellProjectile entity, envoie VFXTriggerPacket aux clients proches
3. Client : VFXEngine démarre un effet de projectile avec trail, mesh 3D, etc.
4. À chaque tick : serveur envoie VFXUpdatePacket avec la nouvelle position (ou le projectile entity fait le travail côté client)
5. À l'impact : serveur envoie VFXStopPacket pour le trail, VFXTriggerPacket pour l'impact
```

### Optimisation

Les effets "attachés à une entité" (comme les trails de projectile) sont gérés **côté client uniquement** quand l'entité est chargée — pas besoin de packets de update, le client suit l'entité tout seul. Les packets ne sont nécessaires que pour :
1. Les effets sans entité (effets de zone, buffs)
2. La synchronisation initiale (un joueur qui rejoint en cours de partie)
3. Les effets qui doivent être parfaitement synchronisés entre joueurs

---

## 12. VFXEngine Lifecycle

### Architecture

`VFXEngine` gère le cycle de vie complet des effets VFX côté client. Ce n'est PAS une entité Minecraft — c'est une couche de rendu légère gérée par des événements Forge.

```java
public class VFXEngine {
    private static VFXEngine INSTANCE;
    
    private final List<VFXEffect> activeEffects = new ArrayList<>();  // effets en cours
    private final List<VFXEffect> pendingAdd = new ArrayList<>();      // file d'attente
    private final List<VFXEffect> pendingRemove = new ArrayList<>();   // cleanup
    private final ObjectPool<VFXEffect> effectPool;                    // pool de réutilisation
    private final PerformanceMonitor perfMonitor;
    
    private VFXEngine() {
        this.effectPool = new ObjectPool<>(VFXEffect::new, 100);
        this.perfMonitor = new PerformanceMonitor();
    }
    
    public static VFXEngine getInstance() { ... }
}
```

### Lifecycle hooks

```java
// ClientSetup.java
@SubscribeEvent
public static void onClientSetup(FMLClientSetupEvent event) {
    VFXEngine.init();
}

// Dans la classe d'event client
@SubscribeEvent
public static void onClientTick(ClientTickEvent.Pre event) {
    VFXEngine.getInstance().tick();
}

@SubscribeEvent
public static void onRenderLevelStage(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
        VFXEngine.getInstance().render(
            event.getPoseStack(),
            event.getProjectionMatrix(),
            event.getPartialTick()
        );
    }
}
```

### VFXEffect (objet léger, PAS une Entity)

```java
public class VFXEffect {
    private String vfxId;            // identifiant de définition
    private UUID effectId;           // identifiant unique d'instance
    private Vec3 position;           
    private Vec3 direction;
    private int age;                 // ticks écoulés
    private int lifetime;            // durée de vie max
    private float intensity;         // 0.0 - 1.0 (fade in/out)
    private boolean active;
    
    // Composants de l'effet
    private List<Emitter> emitters;
    private List<ProceduralMesh> meshes;
    private TrailHandler trail;
    
    public void tick() { /* update émetteurs, meshes, lifecycle */ }
    public void render(PoseStack stack, MultiBufferSource buffer, float partialTick) { /* rendu */ }
    public boolean isFinished() { return age >= lifetime; }
}
```

### Object Pool

```java
public class ObjectPool<T> {
    private final Queue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;
    
    public T acquire() {
        T obj = pool.poll();
        return obj != null ? obj : factory.get();
    }
    
    public void release(T obj) {
        if (pool.size() < maxSize) {
            pool.offer(obj);
        }
    }
}
```

### Gestion du resource reload (F3+T)

```java
// ClientSetup.java
@SubscribeEvent
public static void onResourceReload(OnResourceReloadEvent event) {
    // Les shaders core sont rechargés automatiquement par RegisterShadersEvent
    // Mais les références dans SihriyaCoreShaders deviennent invalides
    SihriyaCoreShaders.onResourceReload();
    
    // Vider les mesh caches (les textures ont peut-être changé)
    ProceduralMesh.clearCache();
    
    // Nettoyer les effets en cours qui utilisent d'anciennes ressources
    VFXEngine.getInstance().clearAllEffects();
}
```

---

## 13. Plan d'Implémentation (Milestones)

### Milestone 1 — Fondations + CoreShader + Bloom + DataLoader
- Système de CoreShader GLSL (Fresnel glow) avec `RegisterShadersEvent`
- Extension de `SihriyaRenderTypes` pour les nouveaux types de rendu (glow mesh, beam, etc.)
- PostChain bloom complet (bright pass, blur, combine) avec fichiers GLSL
- Système de configuration client (bloom ON/OFF, intensité, qualité)
- Data loader pour les définitions VFX JSON (`SihriyaVFXData`)
- `VFXRegistry`, `VFXDefinition` records (tous les composants)
- Centralisation des couleurs (refactor `SchoolColors`)

### Milestone 2 — Mesh 3D Procédural + VFXEngine
- `SphereMesh`, `TubeMesh`, `TorusMesh`, `ConeMesh`, `DiskMesh`, `HelixMesh`
- `ProceduralMesh` interface + cache de vertices
- `MeshRenderer` générique (supporte le shader glow Fresnel)
- `VFXEngine` lifecycle (init, tick, render, object pooling)
- `VFXEffect` objet léger (pas une entité Minecraft)
- Gestion du resource reload (F3+T)

### Milestone 3 — Émetteurs + Trails + Protocole Réseau
- `Emitter` interface + `EmitterManager`
- `SpiralEmitter`, `VortexEmitter`, `ConeEmitter`, `RingEmitter`, `HelixEmitter`, `BurstEmitter`
- `TrailHandler` pour les trails de particules
- Packets réseau : `VFXTriggerPacket`, `VFXStopPacket`, `VFXUpdatePacket`
- Enregistrement dans `NetworkHandler`

### Milestone 4 — Projectiles 3D + Beams + Éclairs
- Extension de `SpellProjectileRenderer` : mesh 3D (sphère UV) au lieu de quad plat
- Trail de particules pendant le vol (dans `SpellProjectile.tick()`)
- Rotation, pulsation, glow Fresnel
- Impact effects (burst, ring, shockwave)
- `BeamRenderer` avec `TubeMesh` entre lanceur et cible
- Texture scrollante pour les faisceaux
- `LightningBoltHelper` (génération d'arcs par midpoint displacement)
- Rendu d'éclairs ramifiés avec `TubeMesh`

### Milestone 5 — Auras, Boucliers, Effets au Sol + EffectComposer
- `AuraRenderer` pour les buffs autour du joueur
- `ShieldRenderer` avec `SphereMesh` ou `TorusMesh`
- Effets au sol persistants (disques, runes, marques)
- `EffectComposer` et `EffectPhase` (effets en phases chronométrées)
- Animation des phases : charge → cast → projectile → impact → persistant

### Milestone 6 — Data-Driven + Polissage + Documentation
- Fichiers JSON VFX pour les 9 écoles (`assets/sihriya/sihriya_vfx/`)
- Définitions pour les 252 sorts (overrides spécifiques)
- Désactivation du bloom auto si FPS < 30
- Niveaux de qualité (FAST / FANCY / FABULOUS)
- Tests de régression visuelle
- Documentation dans `GUIDE-DEV.md`
- Nettoyage des anciens systèmes (particules vanilla, `SchoolGlowParticle` si remplacé)

---

## 14. Ressources Textures

### Nouvelles textures à créer

| Texture | Usage | Format |
|---------|-------|--------|
| `textures/vfx/glow_ramp.png` | Ramp de glow pour le shader Fresnel | 16×256, gradient horizontal |
| `textures/vfx/beam.png` | Texture de faisceau (rayures longitudinales) | 64×64, tileable |
| `textures/vfx/noise.png` | Bruit procédural pour perturbation de mesh | 64×64 |
| `textures/vfx/lightning.png` | Texture d'éclair | 64×64, tileable |
| `textures/vfx/shield_{school}.png` | Texture de bouclier par école | 256×256 |
| `textures/vfx/rune_{school}.png` | Runes au sol par école | 128×128 |
| `textures/vfx/aura_{school}.png` | Texture d'aura par école | 64×64 |

### Textures existantes à améliorer

| Texture actuelle | Problème | Solution |
|-----------------|----------|----------|
| `textures/particle/glow_spark.png` | Trop petite, basse résolution | Remplacer par 32×32 avec halo progressif |
| `textures/magiccircle/{school}_{0-3}.png` | Résolution inconnue | Vérifier et upscaler si < 256×256 |
| `textures/gui/spell_icons.png` | Sprite sheet basique | Garder pour l'UI, pas lié au VFX |

### Shaders GLSL et passes PostChain

| Fichier | Type | Description |
|---------|------|-------------|
| `shaders/core/sihriya_glow.vsh` | Vertex shader | Fresnel glow effect (normal, view angle, pulsation) |
| `shaders/core/sihriya_glow.fsh` | Fragment shader | Glow Fresnel + dégradé couleur + blending additif |
| `shaders/post/bloom.json` | PostChain | Orchestre les passes bright→blur→combine |
| `shaders/post/bright.json` | Passe PostChain | Extraction des pixels lumineux |
| `shaders/post/bright.fsh` | Fragment shader | Calcul de luminance, seuil à 0.6 |
| `shaders/post/combine.json` | Passe PostChain | Combinaison original + bright + blur |
| `shaders/post/combine.fsh` | Fragment shader | Addition pondérée des 4 targets |
| `shaders/post/blur.fsh` | Fragment shader | Gaussian blur (peut réutiliser le blur vanilla) |

### Textures de shader intégrées

Les textures de bruit et de ramp n'ont pas besoin d'être des fichiers PNG — on peut les générer en code et les stocker dans des `NativeImage` pour éviter des assets supplémentaires.

---

## 15. Performance et Optimisation

### Budget de performance

| Cible | Limite |
|-------|--------|
| Particules simultanées max | 2000 |
| Meshes simultanés max | 50 |
| Beam actifs max | 8 |
| Émetteurs simultanés max | 30 |
| Ticks de calcul VFX max | 2ms / tick |
| Temps de rendu VFX max | 5ms / frame (sur GPU dédié) |

### Niveaux de qualité

| Setting | Bloom | Mesh Detail | Particles | Beams |
|---------|-------|-------------|-----------|-------|
| **FAST** | OFF | 8 segments | ×0.5 | OFF |
| **FANCY** | ON (low) | 16 segments | ×1.0 | ON |
| **FABULOUS** | ON (high) | 32 segments | ×1.5 | ON + glow |

Ces niveaux suivent les settings graphiques vanilla de Minecraft.

### Optimisations clés

1. **Precompute des vertices de mesh** — les meshes (sphère, tube, etc.) sont générés une fois et réutilisés
2. **Culling frustum** — les effets hors champ de la caméra ne sont pas rendus
3. **Distance LOD** — les effets lointains utilisent moins de segments
4. **Pool d'entités VFX** — réutilisation des objets pour éviter l'alloc mémoire
5. **Instancing** — les particules identiques sont rendues en un seul draw call
6. **Blazing Pack** — tout le système utilise les packs de données (data-driven) pour éviter de recompiler le mod à chaque changement de paramètre VFX

### Détection de performance

```java
// Auto-désactivation du bloom si FPS < seuil
public class PerformanceMonitor {
    private static final int CHECK_INTERVAL = 100; // ticks
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

---

> **Fin du document de design.**
> Prochaine étape : validation par l'utilisateur, puis rédaction du plan d'implémentation détaillé via le skill writing-plans.
