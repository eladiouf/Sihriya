package tong.sihriya.data;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistryViewsTest {
    @Test
    void spellRegistryViewCannotBeMutatedExternally() {
        int initialSize = SpellRegistry.size();
        String spellId = "test." + UUID.randomUUID();

        SpellRegistry.register(new SpellRegistry.SpellData(
            spellId,
            "fire",
            1,
            1,
            1,
            0,
            SpellRegistry.SpellType.PROJECTILE,
            List.of(new SpellRegistry.SpellEffect("damage", 1.0f, 0.0f, 0))
        ));

        var allSpells = SpellRegistry.getAll();

        assertEquals(initialSize + 1, allSpells.size());
        assertThrows(UnsupportedOperationException.class, allSpells::clear);
    }

    @Test
    void schoolRegistryViewCannotBeMutatedExternally() {
        int initialSize = SchoolRegistry.size();
        String schoolId = "test_" + UUID.randomUUID().toString().replace('-', '_');

        SchoolRegistry.register(new SchoolRegistry.SchoolData(
            schoolId,
            schoolId,
            false,
            "FFFFFF",
            null
        ));

        var allSchools = SchoolRegistry.getAll();

        assertEquals(initialSize + 1, allSchools.size());
        assertThrows(UnsupportedOperationException.class, allSchools::clear);
    }
}
