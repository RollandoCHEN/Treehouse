package com.beamcalculate.model.entites;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Load {
    private static final DoubleProperty gMNm = new SimpleDoubleProperty();
    private static final DoubleProperty qMNm = new SimpleDoubleProperty();
    private static final DoubleProperty gTm = new SimpleDoubleProperty();
    private static final DoubleProperty qTm = new SimpleDoubleProperty();

    public Load() {
        gMNm.bind(Bindings.divide(gTm, 10));
        qMNm.bind(Bindings.divide(qTm, 10));
    }

    public static double getGMNm() {
        return gMNm.get();
    }

    public DoubleProperty gMNmProperty() {
        return gMNm;
    }

    public static double getQMNm() {
        return qMNm.get();
    }

    public DoubleProperty qMNmProperty() {
        return qMNm;
    }

    public static double getGTm() {
        return gTm.get();
    }

    public DoubleProperty gTmProperty() {
        return gTm;
    }

    public static double getQTm() {
        return qTm.get();
    }

    public DoubleProperty qTmProperty() {
        return qTm;
    }
}
