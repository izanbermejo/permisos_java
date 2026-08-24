package ames.comercial.advantage.internal.auxiliar;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConditionGenerator {

    public static <T> String generate (Collection<T> listElements, int numPlaceholders) {
        return listElements.stream()
                .map(a -> "(" + IntStream.range(0, numPlaceholders)
                        .mapToObj(i -> "?")
                        .collect(Collectors.joining(", ")) + ")")
                .collect(Collectors.joining(", "));
    }

}
