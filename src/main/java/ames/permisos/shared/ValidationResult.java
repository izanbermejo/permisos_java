package ames.permisos.shared;

import com.google.common.util.concurrent.CycleDetectingLockFactory.Policies;

public final class ValidationResult {

    private boolean valid;
    private String description = "";
    private Policies policy;

    public ValidationResult() {
        this.valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public String getDescription() {
        return description;
    }

    public void invalidate(String custoReason) {
        this.valid = false;
        this.description = custoReason;
    }

}
