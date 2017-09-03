package com.beamcalculate.model.entites;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

public class Geometry {
    private final static IntegerProperty numSpan = new SimpleIntegerProperty();
    private final static IntegerProperty numSupport = new SimpleIntegerProperty();
    private final static DoubleProperty sectionWidth = new SimpleDoubleProperty();
    private final static DoubleProperty sectionHeight = new SimpleDoubleProperty();
    private final static DoubleProperty slabThickness = new SimpleDoubleProperty();
    private final static DoubleProperty perpendicularSpacing = new SimpleDoubleProperty();
    private final static DoubleProperty effectiveHeight = new SimpleDoubleProperty();
    private static double totalLength = 0.0;

    private static Map<Integer, Double> spansLengthMap = new HashMap<>();        // Not be able to use MapProperty, cause not be able to set (k,v) to it
    private static Map<Integer, Double> mEffectiveSpansLengthMap = new HashMap<>();
    private static Map<Integer, Double> supportWidthMap = new HashMap<>();


    public Geometry() {
        numSupport.bind(Bindings.add(numSpan,1));
        effectiveHeight.bind(Bindings.multiply(sectionHeight, 0.9));
    }

    public DoubleProperty effectiveHeightProperty() {
        return effectiveHeight;
    }

    public static double getEffectiveHeight() {
        return effectiveHeight.get();
    }

    public static int getNumSpan() {
        return numSpan.get();
    }

    public IntegerProperty numSpanProperty() {
        return numSpan;
    }

    public static int getNumSupport() {
        return numSupport.get();
    }

    public IntegerProperty numSupportProperty() {
        return numSupport;
    }

    public static Map<Integer,Double> spansLengthMap() {
        return spansLengthMap;
    }

    public static Map<Integer,Double> supportWidthMap() {
        return supportWidthMap;
    }

    public static double getSectionWidth() {
        return sectionWidth.get();
    }

    public DoubleProperty sectionWidthProperty() {
        return sectionWidth;
    }

    public static double getSectionHeight() {
        return sectionHeight.get();
    }

    public DoubleProperty sectionHeightProperty() {
        return sectionHeight;
    }

    public static double getSlabThickness() {
        return slabThickness.get();
    }

    public static DoubleProperty slabThicknessProperty() {
        return slabThickness;
    }

    public static double getPerpendicularSpacing() {
        return perpendicularSpacing.get();
    }

    public static DoubleProperty perpendicularSpcingProperty() {
        return perpendicularSpacing;
    }

    public static double getTotalLength() {
        totalLength = 0;
        if (spansLengthMap.size()==0){
            return 0.0;
        }else {
            spansLengthMap.forEach((k, v)-> totalLength += v);
            supportWidthMap.forEach((k, v)-> totalLength += v);
            return totalLength;
        }
    }

    public static Map<Integer, Double> getEffectiveSpansLengthMap() {
        spansLengthMap().forEach((spanId, spanLength)-> mEffectiveSpansLengthMap.put(
                spanId,
                spanLength + supportWidthMap().get(spanId) /2 + supportWidthMap().get(spanId + 1) /2
        ));
        return mEffectiveSpansLengthMap;
    }
}
