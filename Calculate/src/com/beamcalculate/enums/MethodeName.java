package com.beamcalculate.enums;

import com.beamcalculate.Main;

public enum MethodeName {
    CAQUOT(Main.getBundleText("enum.method.caquot")),
    CAQUOT_MINOREE(Main.getBundleText("enum.method.caquot.reduced")),
    FORFAITAIRE(Main.getBundleText("enum.method.forfaitaire")),
    TROIS_MOMENT(Main.getBundleText("enum.method.threeMoment")),
    TROIS_MOMENT_R(Main.getBundleText("enum.method.threeMoment.redistribution"));

    private String mMethodeName;

    MethodeName(String methodeName) {
        mMethodeName = methodeName;
    }

    public String getMethodeName() {
        return mMethodeName;
    }
}
