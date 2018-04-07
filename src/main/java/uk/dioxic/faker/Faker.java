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
        InputStream langStream = getDefinitionStream(locale.getLanguage());
        InputStream countryStream = getDefinitionStream(locale.getLanguage() + "-" + locale.getCountry().toUpperCase());

        if (countryStream == null && langStream == null) {
            throw new LocaleDoesNotExistException("locale [" + locale.toString() + "] not supported");
        }

        Yaml yaml = new Yaml();
        Faker faker = new Faker();

        logger.info("loading faker definitions for {}", locale);

        if (langStream != null) {
            faker.load(yaml.loadAll(langStream));
        }
        if (countryStream != null) {
            faker.load(yaml.loadAll(countryStream));
        }
        else if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            logger.warn("Locale country [{}] not supported, using base language [{}]", locale.getCountry(), locale.getLanguage());
        }

        return faker;
    }

    public static Faker instance(InputStream... streams) {
        Yaml yaml = new Yaml();
        Faker faker = new Faker();
        for (InputStream stream : streams) {
            if (stream == null) {
                throw new IllegalStateException("stream cannot be null!");
            }
            logger.info("loading faker definitions from stream");
            faker.load(yaml.loadAll(stream));
        }
        return faker;
    }

    public static Faker instance(String... fakerFiles) throws IOException {
        Yaml yaml = new Yaml();
        Faker faker = new Faker();
        for (String fakerFile : fakerFiles) {
            logger.info("loading faker definitions from {}", fakerFile);
            Files.newBufferedReader(Paths.get(fakerFile));
            Reader reader = Files.newBufferedReader(Paths.get(fakerFile));
            faker.load(yaml.loadAll(reader));
        }
        return faker;
    }

    private static InputStream getDefinitionStream(String filename) {
        String filenameWithExtension = "locale/" + filename + ".yml";
        InputStream streamOnClass = Faker.class.getResourceAsStream(filenameWithExtension);
        if (streamOnClass == null) {
            streamOnClass = Faker.class.getClassLoader().getResourceAsStream(filenameWithExtension);
        }

        return streamOnClass;
    }

    protected void load(Iterable<Object> yamlDefinition) {
        put(null, findFakerPath(yamlDefinition));
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

    public Resolvable<String> getResolvable(String key) {
        return globalMap.get(key.toLowerCase());
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
