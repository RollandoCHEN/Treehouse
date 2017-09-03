package com.beamcalculate.model.calculate.support;

import Jama.Matrix;
import com.beamcalculate.model.entites.Geometry;
import com.beamcalculate.model.entites.Load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.beamcalculate.enums.MethodeName.TROIS_MOMENT;

public class SupportMoment3Moment extends SupportMoment {
    private Map<Integer, Double> mEffectiveSpansLengthMap = new HashMap();

    public SupportMoment3Moment(Geometry geometry, Load load) {

        // add supportId and Map to mSupportMomentMap

        for(int supportId = 1;supportId<Geometry.getNumSupport()+1;supportId++){
            Map<Integer, Double> loadCaseMomentMap = new HashMap();
            for (int loadCase = 0; loadCase<Geometry.getNumSpan()+1;loadCase++){
                loadCaseMomentMap.put(loadCase,null);
            }
            mSupportMomentMap.put(supportId, loadCaseMomentMap);
        }

        // calculate effective length for each span

        geometry.spansLengthMap().forEach((spanId, spanLength)-> mEffectiveSpansLengthMap.put(
                spanId,
                spanLength + geometry.supportWidthMap().get(spanId) /2 + geometry.supportWidthMap().get(spanId + 1) /2
        ));

        for (int loadCase = 0; loadCase < Geometry.getNumSpan()+1; loadCase++) {

//        get left hand side array for the equation

            double[][] lhsArray = getLhsArray();

//        get right hand side array for the equation

            double[] rhsArray = getRhsArray(load, loadCase);

//        Creating Matrix Objects with arrays

            Matrix lhs = new Matrix(lhsArray);
            Matrix rhs = new Matrix(rhsArray, Geometry.getNumSupport());
            //calculate Solved Matrix
            Matrix answer = lhs.solve(rhs);

            mSupportMomentMap.forEach((supportId, loadCaseMomentMap) -> {

            });
            for (int supportId = 1; supportId < Geometry.getNumSupport()+1; supportId++){
                if (supportId==1 || supportId == Geometry.getNumSupport()){
                    mSupportMomentMap.get(supportId).put(loadCase, 0.0);
                } else {
                    double moment = answer.get(supportId - 1, 0);
                    mSupportMomentMap.get(supportId).put(loadCase, moment);
                }
            }
        }
    }

    private Double getLeftSpanEffectiveLength(Integer supportId){
        return mEffectiveSpansLengthMap.getOrDefault(supportId-1, 0.0);
    }

    private Double getRightSpanEffectiveLength(Integer supportId){
        return mEffectiveSpansLengthMap.getOrDefault(supportId, 0.0);
    }

    private double[][] getLhsArray(){
        List<double[]> lhsArrayList = new ArrayList<>();
        for (int centreSupportCase = 1; centreSupportCase < Geometry.getNumSupport()+1; centreSupportCase++){
            List<Double> newCase = new ArrayList();
            if (centreSupportCase == 1||centreSupportCase == Geometry.getNumSupport()){
                for (int supportId = 1; supportId < Geometry.getNumSupport()+1; supportId++){
                    double matrixCoef;
                    if (supportId == centreSupportCase){matrixCoef = 1;}
                    else {matrixCoef = 0;}
                    newCase.add(matrixCoef);
                }
            }else {
                for (int supportId = 1; supportId < Geometry.getNumSupport()+1; supportId++){
                    double matrixCoef;
                    if (supportId == centreSupportCase - 1){
                        matrixCoef = getLeftSpanEffectiveLength(centreSupportCase);
                    }else if (supportId == centreSupportCase){
                        matrixCoef = 2 * (getLeftSpanEffectiveLength(centreSupportCase) + getRightSpanEffectiveLength(centreSupportCase));
                    }else if (supportId == centreSupportCase + 1){
                        matrixCoef = getRightSpanEffectiveLength(centreSupportCase);
                    }else {
                        matrixCoef = 0;
                    }
                    newCase.add(matrixCoef);
                }
            }
            Double[] arrayD = new Double[newCase.size()];
            newCase.toArray(arrayD);
            double[] arrayd = Stream.of(arrayD).mapToDouble(Double::doubleValue).toArray();
            lhsArrayList.add(arrayd);
        }
        double[][] lhsArray = new double[lhsArrayList.size()][];
        lhsArrayList.toArray(lhsArray);
        return lhsArray;
    }

    private double[] getRhsArray(Load load, int loadCase){
        List<Double> rhsArrayList = new ArrayList();

        for (int centreSupportCase = 1; centreSupportCase < Geometry.getNumSupport()+1; centreSupportCase++){
            if (centreSupportCase == 1||centreSupportCase == Geometry.getNumSupport()){
                rhsArrayList.add(0.0);
            }else {
                double matrixCoef;
                if (loadCase == 0){
                    double loadToApply = load.getGMNm();
                    matrixCoef =
                            - 1.0/4 * (
                                    loadToApply * Math.pow(getLeftSpanEffectiveLength(centreSupportCase), 3)
                                            + loadToApply * Math.pow(getRightSpanEffectiveLength(centreSupportCase), 3)
                            );
                } else if (loadCase == centreSupportCase - 1){
                    double loadToApply = load.getQMNm();
                    matrixCoef =
                            - 1.0/4 * (
                                    loadToApply * Math.pow(getLeftSpanEffectiveLength(centreSupportCase), 3)
                            );
                } else if (loadCase == centreSupportCase){
                    double loadToApply = load.getQMNm();
                    matrixCoef =
                            - 1.0/4 * (
                                    loadToApply * Math.pow(getRightSpanEffectiveLength(centreSupportCase), 3)
                            );
                } else {
                    matrixCoef = 0;
                }
                rhsArrayList.add(matrixCoef);
            }
        }
        Double[] rhsArrayD = new Double[rhsArrayList.size()];
        rhsArrayList.toArray(rhsArrayD);
        double[] rhsArray = Stream.of(rhsArrayD).mapToDouble(Double::doubleValue).toArray();
        return rhsArray;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getSupportMomentMap() {
        return mSupportMomentMap;
    }

    @Override
    public double getMomentValueOfSupport(Integer supportId, Integer loadCase){
        Map<Integer, Double> loadCaseMomentMap;
        loadCaseMomentMap = this.getSupportMomentMap().get(supportId);
        return loadCaseMomentMap.get(loadCase);
    }

    @Override
    public String getMethod() {
        return TROIS_MOMENT.getMethodeName();
    }

    @Override
    public Map<Integer, Double> getCalculateSpanLengthMap() {
        return mEffectiveSpansLengthMap;
    }
}
