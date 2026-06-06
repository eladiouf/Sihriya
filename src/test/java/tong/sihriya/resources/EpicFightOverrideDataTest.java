package tong.sihriya.resources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicFightOverrideDataTest {

    @Test
    void epicFightAliasSkillRegistryProvidesEfnExampleAlias() throws IOException {
        String content = Files.readString(Path.of(
            "src/main/java/tong/sihriya/epicfight/EpicFightAliasSkillRegistry.java"
        ));

        assertTrue(content.contains("createRegistryWorker(\"efn\")"));
        assertTrue(content.contains("worker.build(\"example\""));
        assertTrue(content.contains("ResourceLocation.fromNamespaceAndPath(\"efn\", \"example\")"));
        assertTrue(content.contains("@SubscribeEvent(priority = EventPriority.HIGHEST)"));
    }

    @Test
    void builtInEpicFightDataPackOverridesInvalidInvincibleParticleReference() throws IOException {
        String content = Files.readString(Path.of(
            "src/main/resources/packs/sihriya_epicfight_data_fixes/data/invincible/capabilities/weapons/types/custom_combo_demo.json"
        ));
        String packMeta = Files.readString(Path.of(
            "src/main/resources/packs/sihriya_epicfight_data_fixes/pack.mcmeta"
        ));

        assertTrue(content.contains("\"hit_particle\": \"epicfight:hit_blunt\""));
        assertTrue(content.contains("\"hit_sound\": \"epicfight:entity.hit.blunt\""));
        assertFalse(content.contains("\"hit_particle\": \"blunt\""));
        assertTrue(packMeta.contains("\"pack_format\": 15"));
        assertTrue(packMeta.contains("Sihriya Epic Fight data fixes"));
        assertTrue(packMeta.contains("\"namespace\": \"efn\""));
        assertTrue(packMeta.contains("capabilities/weapons/scythe\\\\.json"));
        assertTrue(packMeta.contains("\"namespace\": \"epicfight\""));
        assertTrue(packMeta.contains("capabilities/weapons/_diamond_greatsword\\\\.json"));
    }

    @Test
    void epicFightIntegrationRegistersServerDataPackFinder() throws IOException {
        String content = Files.readString(Path.of(
            "src/main/java/tong/sihriya/integration/EpicFightIntegration.java"
        ));

        assertTrue(content.contains("AddPackFindersEvent"));
        assertTrue(content.contains("PackType.SERVER_DATA"));
        assertTrue(content.contains("Pack.Position.TOP"));
        assertTrue(content.contains("sihriya_epicfight_data_fixes"));
    }
}
