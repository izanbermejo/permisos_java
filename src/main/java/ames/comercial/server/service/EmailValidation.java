package ames.comercial.server.service;

import ames.comercial.shared.SharedExceptions.InvalidEmail;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *	Classe que valida el format d'una o múltiples llistes d'emails separats per punt i coma.
 *	Una llista pot estar formada per un sol email. Cada email de les llistes ha de complir les següents regles.
 *	La part local:
 *		- Permet chars numèrics i alfanumèric de la 'a' a la 'z' en majúscules i minúscules
 *		- Permet els chars _!#$%&'*+/=?`|~^-
 *		- No permet "." consecutius, ni al principi, ni al final
 *		- Permet un màxim de 64 chars
 *	El domini:
 *		- Permet chars numèrics i alfanumèric de la 'a' a la 'z' en majúscules i minúscules
 *		- Permet els chars "-" i "." (excepte al principi i al final)
 *		- No permet "." consecutius
 */

@Service
public class EmailValidation {

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$");

    /** Separa una llista d'emails separats per coma i es comprova la seva validesa. Es llança un error si algun dels emails no té el format correcte. */
    public List<String> splitAndCheck (String emailList) {
        var result = Stream.of(emailList.split(","))
                .map(String::trim)
                .filter(str -> !str.isBlank())
                .collect(Collectors.toList());

        var errorList = result.stream()
                .filter(email -> !pattern.matcher(email).matches())
                .collect(Collectors.joining(" "));

        if (!errorList.isEmpty())
            throw new InvalidEmail(errorList);

        return result;
    }

}
