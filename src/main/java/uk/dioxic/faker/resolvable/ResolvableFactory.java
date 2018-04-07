package uk.dioxic.faker.resolvable;

import uk.dioxic.faker.Faker;

import java.util.List;
import java.util.stream.Collectors;

public class ResolvableFactory {

    @SuppressWarnings("unchecked")
    public Resolvable<String> create(Object object, String key, Faker faker) {
        Object o = createOptional(object, key, faker);

        return (o instanceof Resolvable) ? (Resolvable<String>) o : new StringWrapperResolver((String)o);
    }

    private Object createOptional(Object object, String key, Faker faker) {
        if (object instanceof List) {
            List<?> list = (List) object;
            list = list.stream().map(o -> createOptional(o, key, faker)).filter(o -> o != null).collect(Collectors.toList());
            return new ListResolver(list);
        }

        if (object instanceof String) {
            String s = (String) object;

            // regular expression
            if (RegexResolver.canHandle(s)) {
                return new RegexResolver(s);
            }

            // faker string expression
            if (FakerStringResolver.canHandle(s)) {
                return new FakerStringResolver(s, key, faker);
            }

            // format expression
            if (FormatResolver.canHandle(s)) {
                return new FormatResolver(s);
            }
        }

        return object;
    }

}
