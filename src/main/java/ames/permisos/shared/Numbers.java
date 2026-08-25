package ames.permisos.shared;

import java.math.BigDecimal;

public final class Numbers {

    private Numbers () {}

    public static BigDecimal descompteAplicar(BigDecimal descompte) {
        return new BigDecimal(100).subtract(descompte);
    }

    private static interface Comparation {
        boolean compare (BigDecimal value1, BigDecimal value2);
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str); // O usa Integer.parseInt(str) para enteros
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean compareGreater (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) > 0;
    }

    private static boolean compareGreaterEq (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) >= 0;
    }

    private static boolean compareLesser (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) < 0;
    }

    private static boolean compareLesserEq (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) <= 0;
    }

    private static boolean compareEq (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) == 0;
    }

    private static boolean compareDiff (BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) != 0;
    }

    public static class Than {
        private BigDecimal value1;
        private Comparation comp;
        public Than (BigDecimal value1, Comparation comp) {
            this.value1 = value1;
            this.comp = comp;
        }
        public boolean than (BigDecimal value2) {
            return comp.compare(value1, value2);
        }
        public boolean than (long value2) {
            return comp.compare(value1, decimal(value2));
        }
        public boolean than (String value2) {
            return comp.compare(value1, decimal(value2));
        }
    }

    public static class To {
        private BigDecimal value1;
        private Comparation comp;
        public To (BigDecimal value1, Comparation comp) {
            this.value1 = value1;
            this.comp = comp;
        }
        public boolean to (BigDecimal value2) {
            return comp.compare(value1, value2);
        }
        public boolean to (long value2) {
            return comp.compare(value1, decimal(value2));
        }
        public boolean to (String value2) {
            return comp.compare(value1, decimal(value2));
        }
    }

    public static Than isHigh (BigDecimal value) {
        return new Than(value, Numbers::compareGreater);
    }

    public static Than isHigh (long value) {
        return new Than(decimal(value), Numbers::compareGreater);
    }

    public static Than isHigh (String value) {
        return new Than(decimal(value), Numbers::compareGreater);
    }

    public static Than isHighEq (BigDecimal value) {
        return new Than(value, Numbers::compareGreaterEq);
    }

    public static Than isHighEq (long value) {
        return new Than(decimal(value), Numbers::compareGreaterEq);
    }

    public static Than isHighEq (String value) {
        return new Than(decimal(value), Numbers::compareGreaterEq);
    }

    public static Than isSmall (BigDecimal value) {
        return new Than(value, Numbers::compareLesser);
    }

    public static Than isSmall (long value) {
        return new Than(decimal(value), Numbers::compareLesser);
    }

    public static Than isSmall (String value) {
        return new Than(decimal(value), Numbers::compareLesser);
    }

    public static Than isSmallEq (BigDecimal value) {
        return new Than(value, Numbers::compareLesserEq);
    }

    public static Than isSmallEq (long value) {
        return new Than(decimal(value), Numbers::compareLesserEq);
    }

    public static Than isSmallEq (String value) {
        return new Than(decimal(value), Numbers::compareLesserEq);
    }

    public static To isEqual (BigDecimal value) {
        return new To(value, Numbers::compareEq);
    }

    public static To isEqual (long value) {
        return new To(decimal(value), Numbers::compareEq);
    }

    public static To isEqual (String value) {
        return new To(decimal(value), Numbers::compareEq);
    }

    public static To isDiff (BigDecimal value) {
        return new To(value, Numbers::compareDiff);
    }

    public static To isDiff (long value) {
        return new To(decimal(value), Numbers::compareDiff);
    }

    public static To isDiff (String value) {
        return new To(decimal(value), Numbers::compareDiff);
    }

    public static boolean isZero (BigDecimal value) {
        return isEqual(value).to(BigDecimal.ZERO);
    }

    public static boolean isZero (long value) {
        return value == 0;
    }

    public static boolean isZero (String value) {
        return "0".equals(value);
    }

    public static BigDecimal decimal (int value) {
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal decimal (long value) {
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal decimal (double value) {
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal decimal (String value) {
        return new BigDecimal(value);
    }

}
