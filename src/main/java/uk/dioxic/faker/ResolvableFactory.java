package uk.dioxic.faker;

import uk.dioxic.faker.resolvable.*;

import java.util.List;
import java.util.stream.Collectors;

public class ResolvableFactory {

    @SuppressWarnings("unchecked")
    public Resolvable create(Object object, String key, Faker faker) {
        Object o = createOptional(object, key, faker);

        return (o instanceof Resolvable) ? (Resolvable) o : new GenericResolver(o);
    }

    private Object createOptional(Object object, String key, Faker faker) {
        if (object instanceof List) {
            List<?> list = (List) object;
            list = list.stream().map(o -> createOptional(o, key, faker)).collect(Collectors.toList());
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
