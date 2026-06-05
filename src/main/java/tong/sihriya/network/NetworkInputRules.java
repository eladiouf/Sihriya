package tong.sihriya.network;

import java.util.function.Predicate;

public final class NetworkInputRules {
    public static final int MAX_SCHOOL_ID_LENGTH = 32;
    public static final int MAX_SPELL_ID_LENGTH = 64;
    public static final int MAX_REASON_KEY_LENGTH = 96;
    public static final int MAX_SYNC_ENTRIES = 512;
    private static final String SCHOOL_ID_PATTERN = "[a-z_]+";
    private static final String SPELL_ID_PATTERN = "[a-z_]+\\.[a-z0-9_]+";

    private NetworkInputRules() {
    }

    public static boolean isValidSchoolId(String schoolId, Predicate<String> knownSchool) {
        return schoolId != null
            && schoolId.length() > 0
            && schoolId.length() <= MAX_SCHOOL_ID_LENGTH
            && schoolId.matches(SCHOOL_ID_PATTERN)
            && knownSchool.test(schoolId);
    }

    public static boolean isValidSchoolIdSyntax(String schoolId) {
        return schoolId != null
            && schoolId.length() > 0
            && schoolId.length() <= MAX_SCHOOL_ID_LENGTH
            && schoolId.matches(SCHOOL_ID_PATTERN);
    }

    public static String requireSchoolId(String schoolId) {
        if (!isValidSchoolIdSyntax(schoolId)) {
            throw new IllegalArgumentException("Invalid Sihriya school id: " + schoolId);
        }
        return schoolId;
    }

    public static String requireOptionalSchoolId(String schoolId) {
        if (schoolId == null || schoolId.isEmpty()) {
            return "";
        }
        return requireSchoolId(schoolId);
    }

    public static boolean isValidSpellId(String spellId) {
        return spellId != null
            && spellId.length() > 0
            && spellId.length() <= MAX_SPELL_ID_LENGTH
            && spellId.matches(SPELL_ID_PATTERN);
    }

    public static String requireSpellId(String spellId) {
        if (!isValidSpellId(spellId)) {
            throw new IllegalArgumentException("Invalid Sihriya spell id: " + spellId);
        }
        return spellId;
    }

    public static int requireSyncEntryCount(int count) {
        if (count < 0 || count > MAX_SYNC_ENTRIES) {
            throw new IllegalArgumentException("Invalid Sihriya sync entry count: " + count);
        }
        return count;
    }
}
