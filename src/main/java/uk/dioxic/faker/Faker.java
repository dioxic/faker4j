package uk.dioxic.faker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import uk.dioxic.faker.exception.LocaleDoesNotExistException;
import uk.dioxic.faker.resolvable.Resolvable;
import uk.dioxic.faker.resolvable.ResolvableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class Faker {

    private final static Logger logger = LoggerFactory.getLogger(Faker.class);
    private final Map<String, Resolvable<String>> globalMap = new HashMap<>();
    private final ResolvableFactory factory = new ResolvableFactory();

    protected Faker() {
        put("IDNumber.ssn_valid", "###-##-####");
        put("IDNumber.valid_sv_se_ssn", "###-##-####");
        put("IDNumber.invalid_sv_se_ssn", "###-##-####");
    }

    public static Faker instance(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale cannot be null");
        }

        Faker faker = new Faker();

        faker.loadFromResource(locale);

        return faker;
    }

    interface LocaleResource {
        String resourceName(Locale locale);
    }

    private LocaleResource languageResource = (locale) -> "locale/" + locale.getLanguage() + ".yml";
    private LocaleResource countryResource = (locale) -> "locale/" + locale.getLanguage() + "-" + locale.getCountry().toUpperCase() + ".yml";

    private void loadFromResource(Locale locale) {
        try (InputStream languageStream = Faker.class.getClassLoader().getResourceAsStream(languageResource.resourceName(locale));
             InputStream countryStream = Faker.class.getClassLoader().getResourceAsStream(countryResource.resourceName(locale))) {
            if (languageStream != null) {
                logger.info("loading faker definitions for {}", locale.getDisplayLanguage());
                load(new Yaml().loadAll(languageStream));
            }
            if (countryStream != null) {
                logger.info("loading faker definitions for {}", locale.getDisplayCountry());
                load(new Yaml().loadAll(countryStream));
            }
            else if (!locale.getDisplayCountry().isEmpty()) {
                logger.warn("Locale country [{}] not supported, using base language [{}]", locale.getDisplayCountry(), locale.getDisplayLanguage());
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static Faker instance(String... fakerFiles) throws IOException {
        Yaml yaml = new Yaml();
        Faker faker = new Faker();
        for (String fakerFile : fakerFiles) {
            logger.info("loading faker definitions from {}", fakerFile);
            Files.newBufferedReader(Paths.get(fakerFile));
            try (Reader reader = Files.newBufferedReader(Paths.get(fakerFile))) {
                faker.load(yaml.loadAll(reader));
            }
        }
        return faker;
    }

    protected void load(Iterable<Object> yamlDefinition) {
        put(null, findFakerPath(yamlDefinition));
    }

    public boolean isEmpty() {
        return globalMap.isEmpty();
    }

    public String get(String key) {
        Resolvable<String> obj = globalMap.get(key.toLowerCase());

        if (obj != null) {
            return obj.resolve();
        }
        else {
            logger.warn("could not resolve pattern [{}]", key);
            return key;
        }
    }

    public boolean contains(String key) {
        return globalMap.containsKey(key);
    }

    public Map<String, Object> getGlobalMap() {
        return Collections.unmodifiableMap(globalMap);
    }

    @SuppressWarnings("unchecked")
    protected void put(String key, Object o) {
        if (o instanceof Map) {
            Map<String, Object> localMap = ((Map) o);

            localMap.entrySet().forEach(m -> put(key, m));
        }
        else if (o instanceof Map.Entry) {
            Map.Entry entry = ((Map.Entry) o);
            put(getNewKey(key, entry), entry.getValue());
        }
        else if (o instanceof List) {
            globalMap.put(key.toLowerCase(), factory.create(o, key, this));
        }
        else if (o instanceof Resolvable) {
            globalMap.put(key.toLowerCase(), (Resolvable) o);
        }
        else {
            if (key != null) {
                globalMap.put(key.toLowerCase(), factory.create(o, key, this));
            }
        }
    }

    private static String getNewKey(String key, Map.Entry<String, Object> entry) {
        String newKey = key == null ? entry.getKey() : key + "." + entry.getKey();

        return newKey.replace('-', '_');
    }

    @SuppressWarnings("unchecked")
    private static Object findFakerPath(Object o) {
        Object path = null;

        if (o instanceof Iterable) {
            path = findFakerPath((Iterable) o);
        }
        else if (o instanceof Map) {
            path = findFakerPath(((Map) o).entrySet());
        }
        else if (o instanceof Map.Entry) {
            Map.Entry entry = ((Map.Entry) o);
            if ("faker".equals(entry.getKey())) {
                return entry.getValue();
            }
            else {
                path = findFakerPath(entry.getValue());
            }
        }

        return path;
    }

    private static Object findFakerPath(Iterable<Object> iter) {
        for (Object o : iter) {
            Object path = findFakerPath(o);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

}
