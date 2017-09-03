package com.beamcalculate.model.result;

import com.beamcalculate.enums.ReinforcementParam;
import com.beamcalculate.model.calculate.Reinforcement;
import com.beamcalculate.model.entites.Geometry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import static com.beamcalculate.enums.ReinforcementParam.b_MU;


public class GetReinforcementResult {

    private Locale mCurrentLocale = new Locale("en", "US");
    private DecimalFormat mFourDecimal = new DecimalFormat("##0.0000", new DecimalFormatSymbols(mCurrentLocale));  // show only three decimal digits


    public GetReinforcementResult(Reinforcement reinforcement) {

        HBox spanParamHBox = new HBox();
        spanParamHBox.setSpacing(20);
        spanParamHBox.setAlignment(Pos.CENTER);

        spanParamHBox.getChildren().add(getParamNameVBox(reinforcement, "span"));
        spanParamHBox.getChildren().add(getParamValuesHBox(reinforcement, "span"));

        HBox supportParamHBox = new HBox();
        supportParamHBox.setSpacing(20);
        supportParamHBox.setAlignment(Pos.CENTER);

        supportParamHBox.getChildren().add(getParamNameVBox(reinforcement, "support"));
        supportParamHBox.getChildren().add(getParamValuesHBox(reinforcement, "support"));

        VBox container = new VBox();
        container.setPadding(new Insets(20,20,20,20));
        container.setSpacing(20);
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(spanParamHBox, supportParamHBox);

        Stage resultStage = new Stage();
        resultStage.setTitle("Armatures des sections en travée et en appuis");
        resultStage.getIcons().add(new Image("image/reinforcement.png"));

        Scene scene = new Scene(container, 1000, 800);
        resultStage.setScene(scene);
        resultStage.show();
    }

    private VBox getParamNameVBox(Reinforcement reinforcement, String string){
        VBox paramNameVBox = new VBox();
        paramNameVBox.setSpacing(15);
        Label blank = new Label("");
        blank.setStyle("-fx-font-size:16px; -fx-font-weight: bold;");
        paramNameVBox.getChildren().add(blank);

        Map<Integer, Map<ReinforcementParam, Double>> reinforceParamMap;
        if (string.equals("span")){
            reinforceParamMap = reinforcement.getSpanReinforceParam();
        } else {
            reinforceParamMap = reinforcement.getSupportReinforceParam();
        }

        reinforceParamMap.get(1).forEach((param, value)->{
            if (param == b_MU){
                Label paramName = new Label(
                        param.getParamName() + " " + param.getUnit() + " : "
                );
                Label pivotName = new Label("Pivot : ");
                paramNameVBox.getChildren().addAll(paramName, pivotName);
            } else {
                Label paramName = new Label(
                        param.getParamName() + " " + param.getUnit() + " : "
                );
                paramNameVBox.getChildren().add(paramName);
            }

        });
        return paramNameVBox;
    }

    private HBox getParamValuesHBox(Reinforcement reinforcement, String string){
        HBox paramValuesHBox = new HBox();
        paramValuesHBox.setSpacing(20);
        String sectionLabelString;

        Map<Integer, Map<ReinforcementParam, Double>> reinforceParamMap;
        if (string.equals("span")){
            reinforceParamMap = reinforcement.getSpanReinforceParam();
            sectionLabelString = "Travée";
        } else {
            reinforceParamMap = reinforcement.getSupportReinforceParam();
            sectionLabelString = "Appuis";
        }

        reinforceParamMap.forEach((sectionId, paramValueMap)->{
            VBox paramValueVBox = new VBox();
            if (string.equals("support") && sectionId != 1 && sectionId != Geometry.getNumSupport()
                    || string.equals("span")){
                paramValueVBox.setSpacing(15);
                Label sectionLabel = new Label(sectionLabelString + " " + sectionId.toString());
                sectionLabel.setStyle("-fx-font-size:16px; -fx-font-weight: bold;");
                paramValueVBox.getChildren().add(sectionLabel);
                paramValueMap.forEach((param, value)->{
                    if (param == b_MU){
                        Label paramValue = new Label(
                                param.getSign() + " = " + mFourDecimal.format(value)
                        );
                        Label pivotValue = new Label(
                                reinforcement.getPivotMap().get(sectionId).getContent()
                        );
                        paramValueVBox.getChildren().addAll(paramValue, pivotValue);
                    } else {
                        Label paramValue = new Label(
                                param.getSign() + " = " + mFourDecimal.format(value)
                        );
                        paramValueVBox.getChildren().add(paramValue);
                    }
                });
            }

            paramValuesHBox.getChildren().add(paramValueVBox);
        });
        return paramValuesHBox;
    }
}
