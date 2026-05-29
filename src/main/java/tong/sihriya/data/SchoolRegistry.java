package tong.sihriya.data;

import tong.sihriya.Sihriya;

import java.util.*;

public class SchoolRegistry {
    private static final Map<String, SchoolData> SCHOOLS = new LinkedHashMap<>();

    public static void register(SchoolData school) {
        SCHOOLS.put(school.id, school);
        Sihriya.LOGGER.debug("Registered school: {}", school.id);
    }

    public static SchoolData get(String id) { return SCHOOLS.get(id); }
    public static Collection<SchoolData> getAll() { return SCHOOLS.values(); }
    public static List<SchoolData> getStartingSchools() {
        return SCHOOLS.values().stream().filter(s -> s.starting).toList();
    }
    public static int size() { return SCHOOLS.size(); }

    public static class SchoolData {
        public final String id;
        public final String name;
        public final boolean starting;
        public final String color;
        public final UnlockCondition unlock;

        public SchoolData(String id, String name, boolean starting, String color, UnlockCondition unlock) {
            this.id = id; this.name = name; this.starting = starting;
            this.color = color; this.unlock = unlock;
        }
    }

    public static class UnlockCondition {
        public final String type; // "level" or "or"
        public final String[] schoolIds;
        public final int[] levels;

        public UnlockCondition(String type, String[] schoolIds, int[] levels) {
            this.type = type; this.schoolIds = schoolIds; this.levels = levels;
        }
    }
}
