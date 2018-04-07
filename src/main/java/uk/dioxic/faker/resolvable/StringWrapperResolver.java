package uk.dioxic.faker.resolvable;

class StringWrapperResolver implements Resolvable<String> {

    private final String value;

    public StringWrapperResolver(String value) {
        this.value = value;
    }

    @Override
    public String resolve() {
        return value;
    }
}
