package com.beamcalculate.model.calculate;

import com.beamcalculate.model.entites.Geometry;
import com.beamcalculate.model.entites.Material;

import java.util.HashMap;
import java.util.Map;

public class MomentRedistribution {
    private Map<Integer, Double> mSupportMuMap_BR = new HashMap<>();
    private Map<Integer, Double> mSupportMuMap_AR = new HashMap<>();
    private Map<Integer, Double> mRedistributionCoefMap = new HashMap<>();
    private Map<Integer, Double> mFinalRedistributionCoefMap = new HashMap<>();

    private Map<Integer, Double> mSupportMomentMap_BR = new HashMap<>();
    private Map<Integer, Double> mSupportMomentMap_AR = new HashMap<>();

    private double mMinRedistributionCoef;
    private double mMuLowerBound;
    private final double mMuUpperBound = 0.294;


    private double resolveQuadraticEquation(double a, double b, double c){

        double temp1 = Math.sqrt(b * b - 4 * a * c);

        double root1 = (- b - temp1) / (2*a) ;

        return root1;
    }

    public MomentRedistribution(
            ELUCombination combination
    ) {

        for (int supportId = 1; supportId < Geometry.getNumSupport()+1; supportId++) {

//        get support moment values before redistribution
            mSupportMomentMap_BR.put(supportId, combination.getMinMomentValueOfSupport(supportId));

//        get support Mu values before redistribution

            double maxMoment = - combination.getMinMomentValueOfSupport(supportId);
            double supportMuValue = maxMoment / (Geometry.getSectionWidth() * Math.pow(Geometry.getEffectiveHeight(), 2.0) * Material.getFcd());
            mSupportMuMap_BR.put(supportId, supportMuValue);
        }

//        set lower bound and redistribution coefficient for different ductibility class case
        if (Material.getDuctibilityClass().equals("A")){
            mMuLowerBound = 0.255;
            mMinRedistributionCoef = 0.8;
        } else {
            mMuLowerBound = 0.218;
            mMinRedistributionCoef = 0.7;
        }

//        calculate redistribution coefficient according to the support Mu value before redistribution
        mSupportMuMap_BR.forEach((supportId, supportMuValue)->{
            if (supportMuValue == 0 || supportMuValue > mMuUpperBound){
                mRedistributionCoefMap.put(supportId, 1.0);
            } else if (supportMuValue > 0 && supportMuValue < mMuLowerBound){
                mRedistributionCoefMap.put(supportId, mMinRedistributionCoef);
            } else {
                double a = 1;
                double b = 4.8828 * supportMuValue - 4.005;
                double c = 1.5686;
                mRedistributionCoefMap.put(supportId, resolveQuadraticEquation(a, b, c)) ;
            }
        });

        mRedistributionCoefMap.forEach((supportId, redistributionCoef)->{

//        calculate support moment after redistribution
            mSupportMomentMap_AR.put(supportId, redistributionCoef * mSupportMomentMap_BR.get(supportId));

//        calculate support Mu after redistribution
            mSupportMuMap_AR.put(supportId, redistributionCoef * mSupportMuMap_BR.get(supportId));
        });

        mRedistributionCoefMap.forEach((supportId, redistributionCoef)->{
            double supportMomentWhenSpanMomentMax = Math.min(
                    combination.getSupportMomentWhenSpanMomentMax(supportId, 1),
                    combination.getSupportMomentWhenSpanMomentMax(supportId, 2)
            );
            double ration = 0;
            if (supportId > 1 && supportId < Geometry.getNumSupport()) {
                ration = supportMomentWhenSpanMomentMax / combination.getMinMomentValueOfSupport(supportId);
            }
            mFinalRedistributionCoefMap.put(supportId, Math.max(ration, redistributionCoef));

        });
    }

    public Map<Integer, Double> getRedistributionCoefMap() {
        return mRedistributionCoefMap;
    }

    public Map<Integer, Double> getFinalRedistributionCoefMap() {
        return mFinalRedistributionCoefMap;
    }

    public Map<Integer, Double> getSupportMuMap_BR() {
        return mSupportMuMap_BR;
    }
}
