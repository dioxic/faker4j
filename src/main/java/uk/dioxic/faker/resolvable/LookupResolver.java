package uk.dioxic.faker.resolvable;

import uk.dioxic.faker.Faker;

public class LookupResolver implements Resolvable<String> {

    private final String lookupKey;
    private final Faker faker;

    public LookupResolver(String lookupKey, Faker faker) {
        this.lookupKey = lookupKey;
        this.faker = faker;
    }

    @Override
    public String resolve() {
        return faker.get(lookupKey);
    }
}
