package uk.dioxic.faker.resolvable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ListResolver implements Resolvable {
    private final List from;

    public ListResolver(List from) {
        this.from = from;
    }

    @Override
    public Object resolve() {
        Object result = from.get(ThreadLocalRandom.current().nextInt(from.size()));

        return result instanceof Resolvable ? ((Resolvable)result).resolve() : result;
    }

    @Override
    public String toString() {
        return "ListResolver{" +
                "from=" + from +
                '}';
    }
}
