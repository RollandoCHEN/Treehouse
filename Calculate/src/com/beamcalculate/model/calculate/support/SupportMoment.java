package com.beamcalculate.model.calculate.support;

import java.util.HashMap;
import java.util.Map;

public abstract class SupportMoment {
    protected Map<Integer, Map<Integer, Double>> mSupportMomentMap = new HashMap<>();

    public abstract Map<Integer, Map<Integer, Double>> getSupportMomentMap();

    public abstract double getMomentValueOfSupport(Integer supportId, Integer loadCase);

    public abstract String getMethod();

    public abstract Map<Integer, Double> getCalculateSpanLengthMap();
}
