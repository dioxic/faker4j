package uk.dioxic.faker.resolvable;

public class GenericResolver<T> implements Resolvable<T> {

    private final T value;

    public GenericResolver(T value) {
        this.value = value;
    }

    @Override
    public T resolve() {
        return value;
    }
}
