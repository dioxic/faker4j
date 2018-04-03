import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.dioxic.faker.Faker;
import uk.dioxic.faker.exception.LocaleDoesNotExistException;
import uk.dioxic.faker.resolvable.FormatResolver;
import uk.dioxic.faker.resolvable.RegexResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class FakerTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void fakerTest() throws IOException {
        Faker faker = Faker.instance("src/test/resources/test.yml");

        List<String> namePrefixes = Lists.newArrayList("Dato", "Datin");
        List<String> names = Lists.newArrayList("Malik bin", "Osman bin");
        Pattern postcodePattern = Pattern.compile("[A-PR-UWYZ]([A-HK-Y][0-9][ABEHMNPRVWXY0-9]?|[0-9][ABCDEFGHJKPSTUW0-9]?) [0-9][ABD-HJLNP-UW-Z]{2}");

        faker.getGlobalMap().entrySet()
                .stream()
                .map(e -> e.toString())
                .forEach(logger::debug);

        assertThat(faker.get("name.prefix")).as("name.prefix").isIn(namePrefixes);
        assertThat(faker.get("name.first_name")).as("name.first_name").isIn(names);
        assertThat(faker.get("address.postcode")).as("address.postcode").matches(postcodePattern);
        assertThat(faker.get("credit_card.visa")).as("credit_card.visa").matches("[\\d-]+");
        assertThat(faker.get("phone_number.formats")).as("phone_number.formats").matches("^(03|\\+601)[\\d-]+");
        assertThat(faker.get("IDNumber.ssn_valid")).as("IDNumber.ssn_valid").matches("^\\d{3}-\\d{2}-\\d{4}$");

        logger.debug(faker.get("name.prefix"));
        logger.debug(faker.get("name.name"));
        logger.debug(faker.get("name.first_name"));
        logger.debug(faker.get("address.postcode"));
        logger.debug(faker.get("phone_number.formats"));
        logger.debug(faker.get("compass.direction"));
        logger.debug(faker.get("credit_card.visa"));
    }

    @Test
    public void fakerLocaleTest() {
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Faker faker = Faker.instance(locale);

                Stream.generate(() -> faker.getGlobalMap().entrySet())
                        .limit(5)
                        .flatMap(es -> es.stream())
                        .forEach(f -> logger.debug("{} = {}", f.getKey(), faker.get(f.getKey())));
            }
            catch (LocaleDoesNotExistException e){
                logger.debug("no locale for {}", locale);
            }
        }

    }
}
