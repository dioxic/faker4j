package uk.dioxic.faker;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code borrowed from https://github.com/mifmif/Generex and modified to use {@code ThreadLocalRandom}
 * @author Mifrah Youssef
 */
public class Generex {

    private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;
    private final Automaton automaton;

    static {
        Map<String, String> characterClasses = new HashMap<>();
        characterClasses.put("#", "[0-9]");
        characterClasses.put("\\\\d", "[0-9]");
        characterClasses.put("\\\\D", "[^0-9]");
        characterClasses.put("\\\\s", "[ \t\n\f\r]");
        characterClasses.put("\\\\S", "[^ \t\n\f\r]");
        characterClasses.put("\\\\w", "[a-zA-Z_0-9]");
        characterClasses.put("\\\\W", "[^a-zA-Z_0-9]");
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasses);
    }

    public Generex(String regex) {
        automaton = createRegExp(requote(regex)).toAutomaton();
    }

    public String generate() {
        return prepareRandom("", automaton.getInitialState(), 1, Integer.MAX_VALUE);
    }

    private String prepareRandom(String strMatch, State state, int minLength, int maxLength) {
        List<Transition> transitions = state.getSortedTransitions(false);
        Set<Integer> selectedTransitions = new HashSet<>();
        String result = strMatch;
        Random random = ThreadLocalRandom.current();
        while (transitions.size() > selectedTransitions.size()) {
            if (state.isAccept()) {
                if (strMatch.length() == maxLength) {
                    return strMatch;
                }
                if (random.nextInt() > 0.3 * Integer.MAX_VALUE && strMatch.length() >= minLength) {
                    return strMatch;
                }
            }
            if (transitions.size() == 0) {
                return strMatch;
            }
            int nextInt = random.nextInt(transitions.size());
            if (selectedTransitions.contains(nextInt))
                continue;
            selectedTransitions.add(nextInt);
            Transition randomTransition = transitions.get(nextInt);
            int diff = randomTransition.getMax() - randomTransition.getMin() + 1;
            int randomOffset = diff;
            if (diff > 0) {
                randomOffset = random.nextInt(diff);
            }
            char randomChar = (char) (randomOffset + randomTransition.getMin());
            result = prepareRandom(strMatch + randomChar, randomTransition.getDest(), minLength, maxLength);
            int resultLength = result.length();
            if (minLength <= resultLength && resultLength <= maxLength) {
                break;
            }
        }
        return result;
    }

    /**
     * Creates a {@code RegExp} instance from the given regular expression.
     * <p>
     * Predefined character classes are replaced with equivalent regular expression syntax prior creating the instance.
     *
     * @param regex
     *            the regular expression used to build the {@code RegExp} instance
     * @return a {@code RegExp} instance for the given regular expression
     * @throws NullPointerException
     *             if the given regular expression is {@code null}
     * @throws IllegalArgumentException
     *             if an error occurred while parsing the given regular expression
     * @throws StackOverflowError
     *             if the regular expression has to many transitions
     * @see #PREDEFINED_CHARACTER_CLASSES
     */
    private static RegExp createRegExp(String regex) {
        String finalRegex = regex;
        for (Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
            finalRegex = finalRegex.replaceAll(charClass.getKey(), charClass.getValue());
        }
        return new RegExp(finalRegex);
    }

    /**
     * Requote a regular expression by escaping some parts of it from generation without need to escape each special character one by one.
     * <br>
     * this is done by setting the part to be interpreted as normal characters (thus, quote all meta-characters) between \Q and \E , ex :
     * <br>
     * <code> minion_\d{3}\Q@gru.evil\E </code>
     * <br>
     * will be transformed to :
     * <br>
     * <code> minion_\d{3}\@gru\.evil </code>
     * @param regex
     * @return
     */
    private static String requote(String regex) {
        final Pattern patternRequoted = Pattern.compile("\\\\Q(.*?)\\\\E");
        // http://stackoverflow.com/questions/399078/what-special-characters-must-be-escaped-in-regular-expressions
        // adding "@" prevents StackOverflowError inside generex: https://github.com/mifmif/Generex/issues/21
        final Pattern patternSpecial = Pattern.compile("[.^$*+?(){|\\[\\\\@]");
        StringBuilder sb = new StringBuilder(regex);
        Matcher matcher = patternRequoted.matcher(sb);
        while (matcher.find()) {
            sb.replace(matcher.start(), matcher.end(), patternSpecial.matcher(matcher.group(1)).replaceAll("\\\\$0"));
        }
        return sb.toString();
    }
}
