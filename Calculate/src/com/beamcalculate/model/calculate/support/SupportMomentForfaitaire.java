package com.beamcalculate.model.calculate.support;

import com.beamcalculate.Main;
import com.beamcalculate.enums.CombinCoef;
import com.beamcalculate.model.entites.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.beamcalculate.enums.CombinCoef.G_UNFAVORABLE_COEF;
import static com.beamcalculate.enums.CombinCoef.Q_UNFAVORABLE_COEF;
import static com.beamcalculate.enums.MethodeName.FORFAITAIRE;

public class SupportMomentForfaitaire extends SupportMoment{
    private Map<Integer, Map<CombinCoef, Double>> mSpanRefMomentMap = new HashMap<>();
    private Map<Integer, Double> mSpanELURefMomentMap = new HashMap<>();
    private Map<Integer, Double> mSupportELUMomentMap = new HashMap<>();
    private Map<Integer, Double> mCalculateSpanLengthMap = new HashMap<>();
    private boolean mConditionsVerified;
    private List<String> mInvalidateConditions = new ArrayList<>();

    public SupportMomentForfaitaire(Geometry geometry, Load load) {

        mCalculateSpanLengthMap = geometry.spansLengthMap();

        // add spanId and Map to mSpanRefMomentMap

        for(int i = 0;i<geometry.getNumSpan();i++){
            Map<CombinCoef, Double> loadCaseMomentMap = new HashMap();
            loadCaseMomentMap.put(G_UNFAVORABLE_COEF, 0.0);
            loadCaseMomentMap.put(Q_UNFAVORABLE_COEF, 0.0);
            mSpanRefMomentMap.put(i+1, loadCaseMomentMap);
        }

        // add supportId and Map to mSupportMomentMap

        for(int i = 0;i<geometry.getNumSupport();i++){
            Map<Integer, Double> loadCaseMomentMap = new HashMap();
            for (int j = 0; j<geometry.getNumSpan()+1;j++){
                loadCaseMomentMap.put(j,0.0);
            }
            mSupportMomentMap.put(i+1, loadCaseMomentMap);
        }

        // add spanId and value to mSpanELURefMomentMap

        for(int i = 0;i<geometry.getNumSpan();i++){
            mSpanELURefMomentMap.put(i+1, 0.0);
        }

        // add supportId and value to mSupportELUMomentMap

        for(int i = 0;i<geometry.getNumSupport();i++){
            mSupportELUMomentMap.put(i+1, 0.0);
        }

        verifyConditions(geometry, load);
        if (!mConditionsVerified){
            System.out.printf("");
        } else {
            double p = G_UNFAVORABLE_COEF.getValue() * load.getGMNm() + Q_UNFAVORABLE_COEF.getValue() * load.getQMNm();
            mSpanELURefMomentMap.forEach((spanId, refMoment)->{
                refMoment = p * Math.pow(mCalculateSpanLengthMap.get(spanId),2) / 8.0;
                mSpanELURefMomentMap.put(spanId, refMoment);
            });

            if (geometry.getNumSpan()==2){
                mSupportELUMomentMap.put(2, - 0.6 * mSpanELURefMomentMap.get(1));
            } else {
                mSupportELUMomentMap.forEach((supportId, moment)->{
                    if (supportId == 1||supportId == geometry.getNumSupport()){
                        moment = 0.0;
                        mSupportELUMomentMap.put(supportId, moment);
                    } else if (supportId == 2||supportId == geometry.getNumSupport()-1){
                        moment = - 0.5 * Math.max(mSpanELURefMomentMap.get(supportId-1), mSpanELURefMomentMap.get(supportId));
                        mSupportELUMomentMap.put(supportId, moment);
                    } else {
                        moment = - 0.4 * Math.max(mSpanELURefMomentMap.get(supportId-1), mSpanELURefMomentMap.get(supportId));
                        mSupportELUMomentMap.put(supportId, moment);
                    }
                });
            }

            mSpanRefMomentMap.forEach((spanId, loadCaseMomentMap)->
                    loadCaseMomentMap.forEach((loadCase, moment)->{
                        moment = loadCase.getValue() * load.getGMNm() * Math.pow(mCalculateSpanLengthMap.get(spanId),2) / 8;
                        loadCaseMomentMap.put(loadCase, moment);
                    })
            );

            mSupportMomentMap.forEach((supportId, loadCaseMomentMap)->{
                if (supportId!=1 && supportId!=geometry.getNumSupport()){
                    double g1 = mSpanRefMomentMap.get(supportId-1).get(G_UNFAVORABLE_COEF);
                    double q1 = mSpanRefMomentMap.get(supportId-1).get(Q_UNFAVORABLE_COEF);
                    double g2 = mSpanRefMomentMap.get(supportId).get(G_UNFAVORABLE_COEF);
                    double q2 = mSpanRefMomentMap.get(supportId).get(Q_UNFAVORABLE_COEF);
                    double coef = mSupportELUMomentMap.get(supportId) / ( g1 + q1 + g2 + q2 );
                    loadCaseMomentMap.forEach((loaCase, moment)-> {
                        if (loaCase == 0){
                            moment = (g1 + g2) * coef;
                            loadCaseMomentMap.put(loaCase, moment);
                        } else if (loaCase == supportId-1){
                            moment = q1 * coef;
                            loadCaseMomentMap.put(loaCase, moment);
                        } else if (loaCase == supportId){
                            moment = q2 * coef;
                            loadCaseMomentMap.put(loaCase, moment);
                        } else {
                            loadCaseMomentMap.put(loaCase, 0.0);
                        }
                    });
                }
            });
        }

    }

    private void verifyConditions(Geometry geometry, Load load){
        boolean liveLoadCond = load.getQMNm() / 6 < 0.005;
        if (!liveLoadCond){
            mInvalidateConditions.add(Main.getBundleText("text.conditionA"));
        }
        boolean live_deadLoadCond = load.getQMNm() < 2*load.getGMNm();
        if(!live_deadLoadCond){
            mInvalidateConditions.add(Main.getBundleText("text.conditionB"));
        }
        boolean spanLengthCond = getSpanLengthCondition(geometry);
        if (!spanLengthCond){
            mInvalidateConditions.add(Main.getBundleText("text.conditionC"));
        }
        mConditionsVerified = liveLoadCond && live_deadLoadCond && spanLengthCond;
    }

    private boolean getSpanLengthCondition(Geometry geometry){
        boolean b = false;
        for(int spanId = 1; spanId < geometry.getNumSpan(); spanId++){
            boolean b1 = mCalculateSpanLengthMap.get(spanId) / mCalculateSpanLengthMap.get(spanId + 1) < 1.25;
            boolean b2 = mCalculateSpanLengthMap.get(spanId) / mCalculateSpanLengthMap.get(spanId + 1) > 0.8;
            b = b1 && b2;
        }
        return b;
    }

    public boolean isConditionsVerified() {
        return mConditionsVerified;
    }

    public List<String> getInvalidatedConditions() {
        return mInvalidateConditions;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getSupportMomentMap() {
        return mSupportMomentMap;
    }

    @Override
    public double getMomentValueOfSupport(Integer supportId, Integer loadCase) {
        Map<Integer, Double> loadCaseMomentMap;
        loadCaseMomentMap = this.getSupportMomentMap().get(supportId);
        return loadCaseMomentMap.get(loadCase);
    }

    @Override
    public String getMethod() {
        return FORFAITAIRE.getMethodeName();
    }

    @Override
    public Map<Integer, Double> getCalculateSpanLengthMap() {
        return mCalculateSpanLengthMap;
    }
}
