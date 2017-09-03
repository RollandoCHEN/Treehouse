package com.beamcalculate.model.calculate;

import com.beamcalculate.enums.UltimateCase;
import com.beamcalculate.model.calculate.support.SupportMoment;
import com.beamcalculate.model.entites.Geometry;
import com.beamcalculate.model.entites.Load;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.beamcalculate.enums.CombinCoef.*;
import static com.beamcalculate.enums.UltimateCase.MAX;

public class SpanMomentFunction {
    private SupportMoment mSupportMoment;
    private Map<Integer, Map<Integer, Function<Double, Double>>> mSpanMomentFunctionMap = new HashMap<>();

    private Map<Integer, Map<Integer, Function<Double, Double>>> mSpecialLoadCaseSpanFunction = new HashMap<>();

    public SpanMomentFunction(SupportMoment supportMoment) {
        mSupportMoment = supportMoment;

        // add spanId and Map to spanMomentMap

        for (int i = 0; i < Geometry.getNumSpan(); i++) {
            Map<Integer, Function<Double, Double>> loadCaseMomentFunctionMap = new HashMap();
            for (int j = 0; j < Geometry.getNumSpan() + 1; j++) {
                loadCaseMomentFunctionMap.put(j, null);
            }
            mSpanMomentFunctionMap.put(i + 1, loadCaseMomentFunctionMap);
        }

        // add span moment function to the mSpanMomentFunctionMap

        mSpanMomentFunctionMap.forEach((spanId, loadCaseMomentFunctionMap) -> {
            for (int loadCase = 0; loadCase < loadCaseMomentFunctionMap.size(); loadCase++) {
                double leftSupportMoment = mSupportMoment.getMomentValueOfSupport(spanId, loadCase);
                double rightSupportMoment = mSupportMoment.getMomentValueOfSupport(spanId + 1, loadCase);
                Function<Double, Double> momentFunction;
                double thisSpanLength;
                double thisSpanLoad;

                thisSpanLength = mSupportMoment.getCalculateSpanLengthMap().get(spanId);

                if (loadCase == 0) {
                    thisSpanLoad = Load.getGMNm();
                } else {
                    if (loadCase == spanId) {
                        thisSpanLoad = Load.getQMNm();
                    } else {
                        thisSpanLoad = 0;
                    }
                }

                momentFunction = (x -> thisSpanLoad * x * (thisSpanLength - x) / 2 +
                        leftSupportMoment * (1 - x / thisSpanLength) +
                        rightSupportMoment * x / thisSpanLength);

                loadCaseMomentFunctionMap.put(loadCase, momentFunction);
            }
        });
    }

    public SpanMomentFunction(Map<Integer, Map<Integer, Double>> specialLoadCaseSupportMomentMap) {

        // add spanId and Map to spanMomentMap

        for (int spanId = 1; spanId < Geometry.getNumSpan() + 1; spanId++) {
            Map<Integer, Function<Double, Double>> loadCaseMomentFunctionMap = new HashMap();
            loadCaseMomentFunctionMap.put(1, null);
            loadCaseMomentFunctionMap.put(2, null);
            for (int loadCase = 2; loadCase < Geometry.getNumSupport(); loadCase++) {
                loadCaseMomentFunctionMap.put(loadCase + 10, null);
            }
            mSpecialLoadCaseSpanFunction.put(spanId, loadCaseMomentFunctionMap);
        }

        mSpecialLoadCaseSpanFunction.forEach((spanId, loadCaseMomentFunctionMap) ->
                loadCaseMomentFunctionMap.forEach((loadCase, momentFunction) -> {

                    double leftSupportMoment = specialLoadCaseSupportMomentMap.get(spanId).get(loadCase);
                    double rightSupportMoment = specialLoadCaseSupportMomentMap.get(spanId + 1).get(loadCase);
                    double thisSpanLength = Geometry.getEffectiveSpansLengthMap().get(spanId);
                    double thisSpanLoad;

                    if (loadCase < 10) {                 // loadCase == 1, loadCase == 2
                        if (spanId % 2 == loadCase % 2) {
                            thisSpanLoad = G_UNFAVORABLE_COEF.getValue() * Load.getGMNm() + Q_UNFAVORABLE_COEF.getValue() * Load.getQMNm();
                        } else {
                            thisSpanLoad = G_UNFAVORABLE_COEF.getValue() * Load.getGMNm();
                        }
                    } else {
                        if (spanId == loadCase - 10 - 1 || spanId == loadCase - 10) {
                            thisSpanLoad = G_UNFAVORABLE_COEF.getValue() * Load.getGMNm() + Q_UNFAVORABLE_COEF.getValue() * Load.getQMNm();
                        } else {
                            thisSpanLoad = G_UNFAVORABLE_COEF.getValue() * Load.getGMNm();
                        }
                    }

                    momentFunction = (x -> thisSpanLoad * x * (thisSpanLength - x) / 2 +
                            leftSupportMoment * (1 - x / thisSpanLength) +
                            rightSupportMoment * x / thisSpanLength);

                    loadCaseMomentFunctionMap.put(loadCase, momentFunction);
                }));

    }

    public Map<Integer, Map<Integer, Function<Double, Double>>> getSpanMomentFunctionMap() {
        return mSpanMomentFunctionMap;
    }

    public String getMethod() {
        return mSupportMoment.getMethod();
    }

    public Map<Integer, Double> getCalculateSpanLengthMap() {
        return mSupportMoment.getCalculateSpanLengthMap();
    }

    public Map<Integer, Map<Integer, Function<Double, Double>>> getSpecialLoadCaseSpanFunction() {
        return mSpecialLoadCaseSpanFunction;
    }

    public double getUltimateMomentForSpecialLoadCaseAtXOfSpan(
            double x, int spanId, UltimateCase ultimateCase
    ){
        Map<Integer, Function<Double, Double>> loadCaseMomentFunctionMap;
        double finalMoment = 0;
        loadCaseMomentFunctionMap = mSpecialLoadCaseSpanFunction.get(spanId);

        for(Map.Entry<Integer, Function<Double, Double>> entry : loadCaseMomentFunctionMap.entrySet()){
            if (ultimateCase == MAX) {
                finalMoment = Math.max(finalMoment, entry.getValue().apply(x));
            } else {
                finalMoment = Math.min(finalMoment, entry.getValue().apply(x));
            }
        }

        return finalMoment;
    }
}
