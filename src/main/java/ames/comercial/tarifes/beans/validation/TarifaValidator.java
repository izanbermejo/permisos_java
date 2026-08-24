package ames.comercial.tarifes.beans.validation;

import ames.comercial.tarifes.beans.Tarifa;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class TarifaValidator {

    private final Validator validator;

    public TarifaValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public Set<ConstraintViolation<Tarifa>> validate(Tarifa tarifa) {
        return validator.validate(tarifa);
    }
}
