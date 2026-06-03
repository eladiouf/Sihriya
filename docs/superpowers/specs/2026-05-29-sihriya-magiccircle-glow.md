# Sihriya — Magic Circle Glow System

## Objectif
Chaque sort lancé affiche un cercle magique lumineux (glow ADDITION, comme end_rod) au sol, avec une couleur unique par école, et des particules tournoyantes.

---

## 1. Les 9 couleurs glow

| École | R | G | B | Rendu |
|-------|---|---|---|-------|
| Feu | 1.0 | 0.4 | 0.1 | Orange vif |
| Eau | 0.2 | 0.6 | 1.0 | Bleu cyan |
| Vent | 0.9 | 0.9 | 1.0 | Blanc argenté |
| Terre | 0.3 | 0.7 | 0.2 | Vert émeraude |
| Foudre | 1.0 | 0.9 | 0.1 | Jaune électrique |
| Glace | 0.5 | 0.8 | 1.0 | Bleu glacé pâle |
| Lave | 1.0 | 0.2 | 0.0 | Rouge feu profond |
| Nécromancie | 0.5 | 0.0 | 0.8 | Violet sombre |
| Lumagie | 1.0 | 0.85 | 0.4 | Doré blanc |

---

## 2. Principe du glow ADDITION

Au lieu du blend mode normal (MULTIPLY) qui obscurcit :
```
couleur finale = texture × color × destination
```

Le mode ADDITION ajoute les couleurs :
```
couleur finale = texture × color + destination
```

Un pixel blanc (1.0) sur fond noir (0.0) = blanc. Un pixel coloré sur n'importe quel fond s'ajoute → **brille par-dessus tout**, même dans le noir. C'est exactement le rendu d'end_rod.

---

## 3. Architecture complète

```
src/main/java/tong/sihriya/
├── registry/
│   └── SihriyaParticles.java
│
├── client/particle/
│   ├── SchoolGlowParticle.java
│   ├── SchoolGlowParticleProvider.java
│   └── magiccircle/
│       ├── MagicCircleEntity.java
│       ├── MagicCircleRenderer.java
│       ├── MagicCircleAnimation.java
│       ├── SihriyaRenderTypes.java
│       └── CircleShape.java
│
└── SpellParticleHelper.java
```

---

## 4. SihriyaParticles.java — 9 ParticleType

```java
@ObjectHolder(SihriyaMod.MOD_ID)
public class SihriyaParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SihriyaMod.MOD_ID);

    public static final RegistryObject<ParticleType<SimpleParticleType>> FIRE_GLOW =
        register("fire_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> WATER_GLOW =
        register("water_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> WIND_GLOW =
        register("wind_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> EARTH_GLOW =
        register("earth_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> LIGHTNING_GLOW =
        register("lightning_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> ICE_GLOW =
        register("ice_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> LAVA_GLOW =
        register("lava_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> NECRO_GLOW =
        register("necro_glow");
    public static final RegistryObject<ParticleType<SimpleParticleType>> LUMI_GLOW =
        register("lumi_glow");

    private static RegistryObject<ParticleType<SimpleParticleType>> register(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }
}
```

---

## 5. SchoolGlowParticle.java — 1 particule, 9 couleurs

Comportement identique à end_rod :
- `gravity = 0` — flotte
- `friction = 0.99` — ralentit doucement
- `lifetime = 20-30 ticks` — court
- `quadSize = 0.3-0.7` — taille variable
- `RenderType = PARTICLE_SHEET_LIT` — glow

```java
public class SchoolGlowParticle extends TextureSheetParticle {
    private final float r, g, b;

    public SchoolGlowParticle(ClientLevel level, double x, double y, double z,
                              float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0);
        this.r = r;
        this.g = g;
        this.b = b;
        this.gravity = 0;
        this.lifetime = 20 + random.nextInt(10);
        this.quadSize = 0.3f + random.nextFloat() * 0.4f;
        this.hasPhysics = false;
        this.friction = 0.99f;
        this.xd = (random.nextDouble() - 0.5) * 0.1;
        this.zd = (random.nextDouble() - 0.5) * 0.1;
    }

    @Override
    public void tick() {
        super.tick();
        // Léger mouvement sinusoïdal vertical
        this.yd += Math.sin(age * 0.2) * 0.002;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float delta) {
        float alpha = 1.0f - (float) age / lifetime;
        this.setColor(r, g, b);
        this.alpha = alpha;
        super.render(buffer, camera, delta);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
```

---

## 6. SchoolGlowParticleProvider.java — 9 providers, 1 classe

```java
public class SchoolGlowParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;
    private final float r, g, b;

    public SchoolGlowParticleProvider(SpriteSet sprite, float r, float g, float b) {
        this.sprite = sprite;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double dx, double dy, double dz) {
        SchoolGlowParticle p = new SchoolGlowParticle(level, x, y, z, r, g, b);
        p.pickSprite(sprite);
        return p;
    }
}
```

Enregistrement dans l'event `TextureStitchEvent.Pre` ou via `RegisterParticleProvidersEvent` :

```java
@SubscribeEvent
public static void onParticleRegister(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(SihriyaParticles.FIRE_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 1.0f, 0.4f, 0.1f));
    event.registerSpriteSet(SihriyaParticles.WATER_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 0.2f, 0.6f, 1.0f));
    event.registerSpriteSet(SihriyaParticles.WIND_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 0.9f, 0.9f, 1.0f));
    event.registerSpriteSet(SihriyaParticles.EARTH_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 0.3f, 0.7f, 0.2f));
    event.registerSpriteSet(SihriyaParticles.LIGHTNING_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 1.0f, 0.9f, 0.1f));
    event.registerSpriteSet(SihriyaParticles.ICE_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 0.5f, 0.8f, 1.0f));
    event.registerSpriteSet(SihriyaParticles.LAVA_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 1.0f, 0.2f, 0.0f));
    event.registerSpriteSet(SihriyaParticles.NECRO_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 0.5f, 0.0f, 0.8f));
    event.registerSpriteSet(SihriyaParticles.LUMI_GLOW.get(),
        s -> new SchoolGlowParticleProvider(s, 1.0f, 0.85f, 0.4f));
}
```

---

## 7. CircleShape.java — disposition des particules

```java
public class CircleShape {
    // Cercle simple : points réguliers
    public static Vec3[] circlePoints(double radius, int count, double startAngle) {
        Vec3[] points = new Vec3[count];
        for (int i = 0; i < count; i++) {
            double angle = startAngle + (2 * Math.PI * i / count);
            points[i] = new Vec3(Math.cos(angle) * radius, 0,
                                 Math.sin(angle) * radius);
        }
        return points;
    }

    // Spirale : rayon croît avec le tick
    public static Vec3[] spiralPoints(double maxRadius, int count,
                                       double startAngle, double turns) {
        Vec3[] points = new Vec3[count];
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double radius = progress * maxRadius;
            double angle = startAngle + turns * 2 * Math.PI * progress;
            points[i] = new Vec3(Math.cos(angle) * radius, 0,
                                 Math.sin(angle) * radius);
        }
        return points;
    }

    // Double anneau (inner + outer)
    public static Vec3[][] doubleRing(double innerR, double outerR, int count) {
        return new Vec3[][] {
            circlePoints(innerR, count / 2, 0),
            circlePoints(outerR, count / 2, Math.PI / count)
        };
    }
}
```

---

## 8. MagicCircleAnimation.java — timeline

```java
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
            // Phase EXPAND (0-25%)
            float t = progress / 0.25f;
            radius    = MathHelper.lerp(t, 0.5f, 3.0f);
            alpha     = MathHelper.lerp(t, 0.0f, 1.0f);
            rotation += 2.0f;
        } else if (progress < 0.80f) {
            // Phase ROTATE (25-80%)
            radius    = 3.0f;
            alpha     = 1.0f;
            rotation += 4.0f;
        } else {
            // Phase FADE (80-100%)
            float t = (progress - 0.80f) / 0.20f;
            radius    = MathHelper.lerp(t, 3.0f, 5.0f);
            alpha     = MathHelper.lerp(t, 1.0f, 0.0f);
            rotation += 6.0f;
        }
    }

    public float getRadius()    { return radius; }
    public float getRotation()  { return rotation; }
    public float getAlpha()     { return alpha; }
}
```

---

## 9. SihriyaRenderTypes.java — RenderType ADDITION custom

```java
public class SihriyaRenderTypes {
    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
        new TransparencyStateShard("sihriya_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE,
                                       GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
        );

    public static RenderType magicCircleGlow(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
            .setTextureState(new TextureStateShard(texture, false, false))
            .setTransparencyState(ADDITIVE_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .setOutputState(TRANSLUCENT_TARGET)
            .createCompositeState(true);

        return RenderType.create("sihriya_magic_circle_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256, false, true, state);
    }
}
```

---

## 10. MagicCircleEntity.java — entité volante

```java
public class MagicCircleEntity extends Entity {
    private MagicSchool school;
    private int lifetime;
    private int age;
    private MagicCircleAnimation animation;

    public MagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.lifetime = 40;  // ~2 secondes
        this.animation = new MagicCircleAnimation(lifetime);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        animation.tick(age);

        // Émet des particules glow en cercle
        if (level.isClientSide) {
            emitGlowParticles();
        }

        if (age >= lifetime) {
            remove(RemovalReason.DISCARDED);
        }
    }

    private void emitGlowParticles() {
        ParticleType<?> particleType = school.getGlowParticleType();
        Vec3[] points = CircleShape.circlePoints(
            animation.getRadius(), 12, animation.getRotation()
        );
        for (Vec3 p : points) {
            level.addParticle(particleType, false,
                getX() + p.x, getY() + 0.1, getZ() + p.z,
                0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {}
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
```

---

## 11. MagicCircleRenderer.java — texture glow au sol

```java
public class MagicCircleRenderer extends EntityRenderer<MagicCircleEntity> {
    private static final Map<MagicSchool, ResourceLocation> TEXTURES = ImmutableMap.of(
        MagicSchool.FIRE,        new ResourceLocation("sihriya", "textures/magiccircle/fire.png"),
        MagicSchool.WATER,       new ResourceLocation("sihriya", "textures/magiccircle/water.png"),
        MagicSchool.WIND,        new ResourceLocation("sihriya", "textures/magiccircle/wind.png"),
        MagicSchool.EARTH,       new ResourceLocation("sihriya", "textures/magiccircle/earth.png"),
        MagicSchool.LIGHTNING,   new ResourceLocation("sihriya", "textures/magiccircle/lightning.png"),
        MagicSchool.ICE,         new ResourceLocation("sihriya", "textures/magiccircle/ice.png"),
        MagicSchool.LAVA,        new ResourceLocation("sihriya", "textures/magiccircle/lava.png"),
        MagicSchool.NECROMANCY,  new ResourceLocation("sihriya", "textures/magiccircle/necromancy.png"),
        MagicSchool.LUMAGIE,     new ResourceLocation("sihriya", "textures/magiccircle/lumagie.png")
    );

    public MagicCircleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MagicCircleEntity entity, float entityYaw, float partialTick,
                       PoseStack stack, MultiBufferSource buffer, int packedLight) {
        ResourceLocation tex = TEXTURES.get(entity.getSchool());
        if (tex == null) return;

        stack.pushPose();
        stack.translate(0, 0.02, 0);

        float rot = entity.getAnimation().getRotation();
        stack.mulPose(Axis.YP.rotationDegrees(rot));

        float s = entity.getAnimation().getRadius() / 3.0f;
        stack.scale(s, 1.0f, s);

        float alpha = entity.getAnimation().getAlpha();
        float[] rgb = entity.getSchool().getGlowColor(); // [r,g,b]

        RenderType renderType = SihriyaRenderTypes.magicCircleGlow(tex);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = stack.last().pose();

        // Quad plat au sol (plan XZ)
        consumer.vertex(matrix, -1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).endVertex();
        consumer.vertex(matrix, -1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).endVertex();
        consumer.vertex(matrix,  1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).endVertex();
        consumer.vertex(matrix,  1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).endVertex();

        stack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MagicCircleEntity entity) {
        return TEXTURES.get(entity.getSchool());
    }
}
```

---

## 12. SpellParticleHelper.java — API publique

```java
public class SpellParticleHelper {

    // Cercle autour du lanceur (sorts auto-ciblés)
    public static void spawnCircleAround(LivingEntity caster, MagicSchool school, int duration) {
        if (!caster.level.isClientSide) return;
        MagicCircleEntity circle = new MagicCircleEntity(
            SihriyaEntities.MAGIC_CIRCLE.get(), caster.level
        );
        circle.setPos(caster.getX(), caster.getY(), caster.getZ());
        circle.setSchool(school);
        circle.setLifetime(duration);
        caster.level.addFreshEntity(circle);
    }

    // Cercle à une position cible (sorts projectile, zone)
    public static void spawnCircleAt(Level level, Vec3 pos, MagicSchool school, int duration) {
        if (!level.isClientSide) return;
        MagicCircleEntity circle = new MagicCircleEntity(
            SihriyaEntities.MAGIC_CIRCLE.get(), level
        );
        circle.setPos(pos.x, pos.y, pos.z);
        circle.setSchool(school);
        circle.setLifetime(duration);
        level.addFreshEntity(circle);
    }

    // Pluie de particules glow (effet secondaire)
    public static void spawnGlowBurst(Level level, Vec3 pos, MagicSchool school, int count) {
        ParticleType<?> type = school.getGlowParticleType();
        for (int i = 0; i < count; i++) {
            double dx = (random.nextDouble() - 0.5) * 2;
            double dy = random.nextDouble() * 1.5;
            double dz = (random.nextDouble() - 0.5) * 2;
            level.addParticle(type, true,
                pos.x + dx, pos.y + dy, pos.z + dz,
                0, 0.02, 0);
        }
    }
}
```

---

## 13. Intégration dans le cast handler (Milestone 4)

```java
// Quand un sort est lancé (SpellCastHandler) :
MagicSchool school = spell.getSchool();
int duration = spell.getCircleDuration(); // data-driven, défaut 40

// Cercle au sol
SpellParticleHelper.spawnCircleAround(caster, school, duration);

// Rafale de particules glow
SpellParticleHelper.spawnGlowBurst(
    caster.level,
    caster.position().add(0, 1, 0),
    school,
    10
);
```

---

## 14. Fichiers de ressources

```
assets/sihriya/textures/particle/
├── glow_spark.png              # Sprite unique (8×8, blanc sur fond noir)
                                # La couleur vient du provider

assets/sihriya/textures/magiccircle/
├── fire.png                    # 256×256, runes blanc sur fond noir
├── water.png                   # Idem, motif différent par école
├── wind.png
├── earth.png
├── lightning.png
├── ice.png
├── lava.png
├── necromancy.png
└── lumagie.png

assets/sihriya/particles/
├── fire_glow.json              # Définition particle (sprite sheet)
├── water_glow.json
├── wind_glow.json
├── earth_glow.json
├── lightning_glow.json
├── ice_glow.json
├── lava_glow.json
├── necro_glow.json
└── lumi_glow.json
```

Exemple de `fire_glow.json` :
```json
{
  "textures": [
    "sihriya:glow_spark"
  ]
}
```

---

## 15. Résumé visuel attendu

```
Sort lancé (Fire Bolt) →
  ┌─────────────────────────────────────────────┐
  │  0-10 ticks  : cercle apparaît, rayon 0.5→3 │
  │                particules glow orange        │
  │                texture runique au sol fade in│
  │                                              │
  │  10-32 ticks : cercle tourne, rayon 3       │
  │                glow max, rotation continue   │
  │                particules tournent            │
  │                                              │
  │  32-40 ticks : cercle s'élargit 3→5         │
  │                alpha → 0, disparition        │
  │                particules s'éloignent        │
  └─────────────────────────────────────────────┘
  Durée totale : ~2 secondes
  Particules : 12-20 glow particles par tick
  Rendu : ADDITION blend → brille dans le noir
```

---

## 16. Dépendances et compatibilité

- **Forge 47.4.20 / Minecraft 1.20.1** — OK
- Utilise uniquement des API vanilla (`ParticleType`, `Entity`, `PoseStack`, `RenderSystem`)
- Aucune librairie tierce requise
- Compatible avec OptiFine, Sodium, Oculus (le blend ADDITION est standard OpenGL)
- Marche en client-only (les particules sont client-side)
