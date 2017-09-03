package com.beamcalculate.enums;

public enum ReinforcementParam {
    a_M("Moment max", "M_max", "(MN*m)"),
    b_MU("Moment réduit", "Mu", ""),
    c_ALPHA("Position relative de l'axe neutre", "Alpha", ""),
    d_X("Position de l'axe neutre", "x", "(m)"),
    e_BETA("Bras de levier relative", "Beta", ""),
    f_Z("Bras de levier", "z", "(m)"),
    g_EPSILON_S("Allongement des aciers", "Epsilon_s", "%"),
    h_EPSILON_UK("Allongement à la rupture", "Epsilon_uk", ""),
    i_SIGMA_S("Contrainte des aciers", "Sigma_s", "(MPa)"),
    j_A_S("Section d'armature", "As", "(cm2)"),
    k_PIVOT("Pivot", "Pivot", "");


    private String mParamName;
    private String mSign;
    private String mUnit;

    ReinforcementParam(String name, String sign, String unit) {
        setParamName(name);
        setSign(sign);
        setUnit(unit);
    }

    public void setParamName(String paramName) {
        this.mParamName = paramName;
    }

    public String getParamName() {
        return mParamName;
    }

    public String getSign() {
        return mSign;
    }

    public void setSign(String sign) {
        this.mSign = sign;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }
}
