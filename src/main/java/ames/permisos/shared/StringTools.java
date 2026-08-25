package ames.permisos.shared;

public class StringTools {

    public static String replaceLast(String original, String replacement, int numChars) {
        if (original == null || replacement == null || numChars < 0 || numChars > original.length()) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        // Obtener la parte inicial de la cadena, excluyendo los últimos `numChars` caracteres
        String start = original.substring(0, original.length() - numChars);

        // Si el tamaño del reemplazo es mayor que `numChars`, recortar el reemplazo
        if (replacement.length() > numChars) {
            replacement = replacement.substring(0, numChars);
        }

        // Si el tamaño del reemplazo es menor que `numChars`, ajustar añadiendo espacios o truncando
        if (replacement.length() < numChars) {
            replacement = String.format("%" + numChars + "s", replacement);
        }

        // Concatenar la parte inicial con la cadena de reemplazo ajustada
        return start + replacement;
    }
}
