package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;

public class SpellHotbar {
    public static final int SLOT_COUNT = 6;
    private static final String TAG = "sihriya_hotbar";

    public static String getSpell(ServerPlayer player, int slot) {
        var data = player.getPersistentData().getList(TAG, 8);
        if (slot < 0 || slot >= data.size()) return "";
        return data.getString(slot);
    }

    public static void setSpell(ServerPlayer player, int slot, String spellId) {
        var data = player.getPersistentData().getList(TAG, 8);
        while (data.size() <= slot) data.add(StringTag.valueOf(""));
        data.set(slot, StringTag.valueOf(spellId != null ? spellId : ""));
        player.getPersistentData().put(TAG, data);
    }

    public static void clear(ServerPlayer player) {
        player.getPersistentData().remove(TAG);
    }
}
