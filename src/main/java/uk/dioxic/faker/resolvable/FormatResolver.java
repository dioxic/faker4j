package uk.dioxic.faker.resolvable;

import java.util.concurrent.ThreadLocalRandom;

public class FormatResolver implements Resolvable<String> {

    private final String pattern;

    public FormatResolver(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String resolve() {
        StringBuilder sb = new StringBuilder(pattern.length());

        for (char c : pattern.toCharArray()) {
            if (c == '#') {
                sb.append(ThreadLocalRandom.current().nextInt(10));
            }
            else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "FormatResolver{" +
                "pattern='" + pattern + '\'' +
                '}';
    }

    public static boolean canHandle(String expression) {
        return expression.contains("#");
    }
}
