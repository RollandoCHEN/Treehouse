package com.beamcalculate.enums;

public enum Pivots {
    PIVOTA("Pivot A (Mu < 0.056)"),
    PIVOTB("Pivot B (0.056 < Mu < 0.48)"),
    PIVOTC("Pivot C");

    private String mContent;

    Pivots(String content) {
        setContent(content);
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }
}
