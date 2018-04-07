package uk.dioxic.faker.resolvable;


import uk.dioxic.faker.Generex;

class RegexResolver implements Resolvable<String> {

    private final String regex;
    private final Generex generex;

    public RegexResolver(String expression) {
        if (!canHandle(expression)) {
            throw new IllegalStateException("cannot handle expression [" + expression + "]");
        }

        // some regex in faker yml is suffixed by an erroneous L
        int truncate = (expression.endsWith("L/")) ? 1 : 0;
        regex = expression.substring(1, expression.length() - 1 - truncate);
        generex = new Generex(regex);
    }

    @Override
    public String resolve() {
        return generex.generate();
    }

    @Override
    public String toString() {
        return "RegexResolver{" +
                "regex='" + regex + '\'' +
                '}';
    }

    public static boolean canHandle(String expression) {
        return expression.startsWith("/") && expression.endsWith("/");
    }
}
