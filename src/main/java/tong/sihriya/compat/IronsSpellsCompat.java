package tong.sihriya.compat;

import tong.sihriya.Sihriya;

public class IronsSpellsCompat {

    public static void init() {
        Sihriya.LOGGER.info("Sihriya: Iron's Spells integration active — schools: {}, spells queued: {}",
            SihriyaIronsSchools.SCHOOL_REGISTER.getEntries().size(),
            SihriyaIronsSpellRegister.SPELL_REGISTER.getEntries().size());
    }
}
