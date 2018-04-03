package uk.dioxic.faker.resolvable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.dioxic.faker.Faker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakerStringResolver implements Resolvable<String> {
    private final static Pattern LOOKUP_PATTERN = Pattern.compile("#\\{([a-z0-9A-Z_.]+)\\s?(?:'([^']+)')*\\}");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<String> parts;
    private final List<Resolvable<String>> lookups;
    private final String expression;

    public FakerStringResolver(String expression, Faker faker) {
        this(expression, null, faker);
    }

    public FakerStringResolver(String expression, String fakerKey, Faker faker) {
        this.expression = expression;
        Matcher matcher = LOOKUP_PATTERN.matcher(expression);
        lookups = new ArrayList<>();

        while (matcher.find()) {
            String directive = matcher.group(1);

            if (fakerKey != null && Character.isLowerCase(directive.charAt(0))) {
                directive = getPath(fakerKey) + "." + directive;
            }

            lookups.add(new LookupResolver(directive, faker));
        }

        parts = Arrays.asList(LOOKUP_PATTERN.split(expression));

        if (lookups.isEmpty()) {
            throw new IllegalStateException("no lookups found in expression [" + expression + "]");
        }
    }

    @Override
    public String resolve() {
        if (parts.isEmpty()) {
            return lookups.get(0).resolve();
        }

        StringBuilder sb = new StringBuilder();

        Iterator<Resolvable<String>> iter = lookups.iterator();
        for (String part : parts) {
            sb.append(part);
            if (iter.hasNext()) {
                sb.append(iter.next().resolve());
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "FakerStringResolver{" +
                "expression='" + expression + '\'' +
                '}';
    }

    private static String getPath(String fakerKey) {
        if (fakerKey.contains(".")) {
            return fakerKey.substring(0, fakerKey.lastIndexOf('.'));
        }
        return fakerKey;
    }

    public static boolean canHandle(String expression) {
        return LOOKUP_PATTERN.matcher(expression).find();
    }
}
