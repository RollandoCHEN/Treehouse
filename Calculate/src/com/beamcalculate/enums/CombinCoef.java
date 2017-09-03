package com.beamcalculate.enums;

public enum CombinCoef {

        G_UNFAVORABLE_COEF (1.35),
        G_FAVORABLE_COEF (1.00),
        Q_UNFAVORABLE_COEF (1.50),
        Q_FAVORABLE_COEF (0.00);

        private double mValue;

        CombinCoef(double v) {
            mValue = v;
        }

        public double getValue() {
            return mValue;
        }

}
