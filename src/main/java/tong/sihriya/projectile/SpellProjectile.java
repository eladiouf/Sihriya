package tong.sihriya.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SpellProjectile extends ThrowableProjectile {
    private float damage = 10.0f;
    private String spellId = "";

    public SpellProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectile(Level level, LivingEntity shooter, float damage, String spellId) {
        super(EntityType.SNOWBALL, shooter, level);
        this.damage = damage;
        this.spellId = spellId;
    }

    public void setDamage(float damage) { this.damage = damage; }
    public String getSpellId() { return spellId; }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            if (result.getType() == HitResult.Type.ENTITY) {
                var entityHit = (EntityHitResult) result;
                if (entityHit.getEntity() instanceof LivingEntity target) {
                    if (this.getOwner() instanceof LivingEntity owner) {
                        target.hurt(owner.damageSources().indirectMagic(this, owner), damage);
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {}
}
