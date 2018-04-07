package uk.dioxic.faker.resolvable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class ListResolver implements Resolvable {
    private final List from;

    public ListResolver(List from) {
        this.from = from;
    }

    @Override
    public String resolve() {
        Object result = from.get(ThreadLocalRandom.current().nextInt(from.size()));

        return (result instanceof Resolvable) ? ((Resolvable)result).resolve().toString() : result.toString();
    }

    @Override
    public String toString() {
        return "ListResolver{" +
                "from=" + from +
                '}';
    }
}
