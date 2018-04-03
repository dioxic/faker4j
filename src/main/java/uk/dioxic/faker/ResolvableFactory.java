package uk.dioxic.faker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.dioxic.faker.resolvable.FakerStringResolver;
import uk.dioxic.faker.resolvable.FormatResolver;
import uk.dioxic.faker.resolvable.ListResolver;
import uk.dioxic.faker.resolvable.RegexResolver;

import java.util.List;
import java.util.stream.Collectors;

public class ResolvableFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Object create(Object object, String key, Faker faker) {
        if (object instanceof List) {
            List<?> list = (List) object;
            list = list.stream().map(o -> create(o, key, faker)).collect(Collectors.toList());
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
