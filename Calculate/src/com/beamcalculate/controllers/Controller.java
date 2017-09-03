package com.beamcalculate.controllers;

import com.beamcalculate.Main;
import com.beamcalculate.model.calculate.ELUCombination;
import com.beamcalculate.model.calculate.MomentRedistribution;
import com.beamcalculate.model.calculate.Reinforcement;
import com.beamcalculate.model.calculate.SpanMomentFunction;
import com.beamcalculate.model.calculate.support.SupportMomentCaquot;
import com.beamcalculate.model.calculate.support.SupportMoment3Moment;
import com.beamcalculate.model.entites.Geometry;
import com.beamcalculate.model.entites.Load;
import com.beamcalculate.model.entites.Material;
import com.beamcalculate.model.result.GetLineChart;
import com.beamcalculate.model.calculate.support.SupportMomentForfaitaire;
import com.beamcalculate.model.result.GetReinforcementResult;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;


public class Controller implements Initializable {
    @FXML private ChoiceBox numSpans;
    @FXML private CheckBox equalSupportCheck;
    @FXML private CheckBox equalSpanCheck;
    @FXML private GridPane spansLengthGrid;
    @FXML private ImageView image;
    @FXML private GridPane supportsWidthGrid;
    @FXML private TextField equalSupportWidth;
    @FXML private TextField equalSpanLength;
    @FXML private TextField sectionWidth;
    @FXML private TextField sectionHeight;
    @FXML public TextField perpendicularSpacing;
    @FXML public TextField slabThickness;
    @FXML private TextField permanentLoad;
    @FXML private TextField variableLoad;
    @FXML private TextField fck;
    @FXML private TextField fyk;
    @FXML private ChoiceBox ductibilityClass;
    @FXML private Button GraphGenerate;
    @FXML private Button RebarCalculate;


    private Geometry newGeometry = new Geometry();
    private Load newLoad = new Load();
    private Material newMaterial = new Material();

    private SupportMomentCaquot mSupportMomentCaquot;
    private SupportMoment3Moment mSupportMoment3Moment;
    private SupportMomentForfaitaire mSupportMomentForfaitaire;

    private SpanMomentFunction mSpanMomentFunctionCaquot;
    private SpanMomentFunction mSpanMomentFunction3Moment;
    private SpanMomentFunction mSpanMomentFunctionForfaitaire;

    private Reinforcement mReinforcement;

    private Set<String> inputsWarning = new HashSet<>();
    private BooleanProperty notEqualSpan = new SimpleBooleanProperty(true);
    private BooleanProperty notEqualSupport = new SimpleBooleanProperty(true);

    private List<TextField> allTextField = new ArrayList<>();

    private static BooleanProperty isDisabledRebarCalculate = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private BooleanBinding turnTextFieldsIsEmptyGridToBooleanBinding(GridPane gridPane){
//    由于foreach的lambda中只能出现final的参数，orConjunction = orConjunction.or(...)不能出现，所以用了for (Node node : gridPane.getChildren())
        BooleanBinding orConjunction = Bindings.isEmpty(permanentLoad.textProperty()).or(Bindings.isEmpty(variableLoad.textProperty()));
        for (Node node : gridPane.getChildren()){
            TextInputControl textInputNode = (TextInputControl)node;
            orConjunction = orConjunction.or(Bindings.isEmpty(textInputNode.textProperty()));
        }
        return orConjunction;
    }

    private void checkIfDisableTextFields(CheckBox checkBox, TextField textField, GridPane gridPane){
        if(checkBox.isSelected()){
            bindTextFieldToTextFieldsGrid(textField, gridPane);
        }else {
            unbindTextFieldToTextFieldsGrid(gridPane);
        }
    }

    private void addRealNumberValidation(TextField textField){
        textField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { //when focus lost
                if(!textField.getText().matches("\\d+\\.\\d+|\\d+")){
                    //when it not matches the pattern
                    //set the textField empty
                    textField.setText("");
                }
            }
        });
    }

    private void addRealNumberValidation(List<TextField> list){
        list.forEach(this::addRealNumberValidation);
    }

    private void addTextFieldToGrid(
            int numField, double hgapValue,
            CheckBox checkBox, TextField toBindTextField,
            GridPane goalGridPane
    ){
        for (int i=0;i<numField;i++){
            TextField textField = new TextField();
            String textFieldId = Integer.toString(i+1);
            textField.setId(textFieldId);

            textField.disableProperty().bind(checkBox.selectedProperty());
            if (checkBox.isSelected()){
                textField.textProperty().bind(toBindTextField.textProperty());
            }

            addRealNumberValidation(textField);

            goalGridPane.add(textField,i,0);
            goalGridPane.setHgap(hgapValue);
        }
    }


    private void bindTextFieldToTextFieldsGrid(TextField textField, GridPane gridPane){
        gridPane.getChildren().forEach(node -> {
            TextInputControl textInputNode = (TextInputControl)node;
            textInputNode.textProperty().bind(textField.textProperty());
        });
    }

    private void unbindTextFieldToTextFieldsGrid(GridPane gridPane) {
        gridPane.getChildren().forEach(node -> {
            TextInputControl textInputNode = (TextInputControl)node;
            textInputNode.textProperty().unbind();
        });
    }

    private Image getImage(int numSpan){
        StringBuilder url = new StringBuilder("");
        switch (numSpan){
            case 1: url.append("image/1span.png");
                break;
            case 2: url.append("image/2spans.png");
                break;
            case 3: url.append("image/3spans.png");
                break;
            case 4: url.append("image/4spans.png");
                break;
            case 5: url.append("image/5spans.png");
                break;
            case 6: url.append("image/6spans.png");
                break;
        }
        return new Image(url.toString());
    }

    private void getInputValue(GridPane sourceGridPane, Map goalMap){
        sourceGridPane.getChildren().forEach(node -> {
            TextInputControl textField = (TextInputControl)node;
            try {
                goalMap.put(Integer.parseInt(textField.getId()),Double.parseDouble(textField.getText()));
            } catch (NumberFormatException e) {
            }
        });
    }

    private void getInputValue(TextField sourceTextField, DoubleProperty goalProperty){
        try {
            goalProperty.set(Double.parseDouble(sourceTextField.getText()));
        } catch (NumberFormatException e) {
            inputsWarning.add(sourceTextField.getId());
        }
    }

    private void getInputValue(ChoiceBox sourceChoiceBox, StringProperty goalProperty){
        try {
            goalProperty.set((String)sourceChoiceBox.getValue());
            if(sourceChoiceBox.getValue()==null){
                inputsWarning.add(sourceChoiceBox.getId());
            }
        } catch (Exception e) {
            inputsWarning.add(sourceChoiceBox.getId());
        }
    }

    private void getInputValue(ChoiceBox sourceChoiceBox, IntegerProperty goalProperty){
        try {
            goalProperty.set((Integer)sourceChoiceBox.getValue());
        } catch (Exception e) {
            inputsWarning.add(sourceChoiceBox.getId());
        }
    }

    private void showInputWarning(){
        if(!inputsWarning.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(Main.getBundleText("window.title.warning"));
            alert.setHeaderText(null);
            StringBuffer inputsId = new StringBuffer();
            inputsWarning.forEach (input-> inputsId.append("\n- "+input));
            String infoMessage = Main.getBundleText("message.inputWarning") + inputsId;
            alert.setContentText(infoMessage);
            alert.showAndWait();
            inputsWarning.clear();
        }
    }

    private void showForfaitaireCondWarning(List<String> invalidedConditions){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(Main.getBundleText("window.title.warning"));
        alert.setHeaderText(null);
        String infoMessage = Main.getBundleText("message.conditionWarning");
        for (String condition : invalidedConditions){
            infoMessage += "- " + condition + "\n";
        }
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    private void getInputs(){
        getInputValue(spansLengthGrid, newGeometry.spansLengthMap());
        getInputValue(spansLengthGrid, newGeometry.spansLengthMap());
        getInputValue(supportsWidthGrid, newGeometry.supportWidthMap());
        getInputValue(sectionHeight, newGeometry.sectionHeightProperty());
        getInputValue(sectionWidth, newGeometry.sectionWidthProperty());
        getInputValue(slabThickness, newGeometry.slabThicknessProperty());
        getInputValue(perpendicularSpacing, newGeometry.perpendicularSpcingProperty());
        getInputValue(permanentLoad, newLoad.gTmProperty());
        getInputValue(variableLoad, newLoad.qTmProperty());
        getInputValue(fck, newMaterial.fckProperty());
        getInputValue(fyk, newMaterial.fykProperty());
        getInputValue(ductibilityClass, newMaterial.ductibilityClassProperty());
    }

    private void calculateMoments(){
        mSupportMomentCaquot = new SupportMomentCaquot(newGeometry, newLoad);
        mSpanMomentFunctionCaquot = new SpanMomentFunction(mSupportMomentCaquot);
        mSupportMoment3Moment = new SupportMoment3Moment(newGeometry, newLoad);
        mSpanMomentFunction3Moment = new SpanMomentFunction(mSupportMoment3Moment);
        mSupportMomentForfaitaire = new SupportMomentForfaitaire(newGeometry, newLoad);
        mSpanMomentFunctionForfaitaire = new SpanMomentFunction(mSupportMomentForfaitaire);
    }

    @FXML
    private void disableSpanLength(ActionEvent actionEvent) {
        checkIfDisableTextFields(equalSpanCheck, equalSpanLength, spansLengthGrid);
    }

    @FXML
    private void disableSupportWidth(ActionEvent actionEvent) {
        checkIfDisableTextFields(equalSupportCheck, equalSupportWidth, supportsWidthGrid);
    }

    @FXML
    private void generateGeometryDiagram(ActionEvent actionEvent) {

        getInputValue(numSpans, newGeometry.numSpanProperty());
        double hgapValue = (880-newGeometry.getNumSpan()*69)/newGeometry.getNumSpan();
        notEqualSpan.bind(Bindings.not(equalSpanCheck.selectedProperty()));
        notEqualSupport.bind(Bindings.not(equalSupportCheck.selectedProperty()));

        allTextField.addAll(Arrays.asList(
                equalSupportWidth, equalSpanLength, sectionWidth,
                sectionHeight, permanentLoad, variableLoad, fck, fyk
        ));
        addRealNumberValidation(allTextField);

        spansLengthGrid.getChildren().clear();
        supportsWidthGrid.getChildren().clear();
        newGeometry.spansLengthMap().clear();
        newGeometry.supportWidthMap().clear();

        addTextFieldToGrid(
                newGeometry.getNumSpan(), hgapValue,
                equalSpanCheck, equalSpanLength,
                spansLengthGrid
        );

        image.setImage(getImage(newGeometry.getNumSpan()));

        addTextFieldToGrid(
                newGeometry.getNumSupport(), hgapValue,
                equalSupportCheck, equalSupportWidth,
                supportsWidthGrid
        );

//        bind graph generating button to the text fields
        GraphGenerate.disableProperty().bind(
                turnTextFieldsIsEmptyGridToBooleanBinding(spansLengthGrid).
                        or(turnTextFieldsIsEmptyGridToBooleanBinding(supportsWidthGrid))
        );
//        bind rabar calculate button to the text fields
        RebarCalculate.disableProperty().bind(
                Bindings.isEmpty(sectionWidth.textProperty())
                        .or(Bindings.isEmpty(sectionHeight.textProperty()))
                        .or(Bindings.isEmpty(fck.textProperty()))
                        .or(Bindings.isEmpty(fyk.textProperty()))
                        .or(Bindings.isNull(ductibilityClass.valueProperty()))
        );
        isDisabledRebarCalculate.bind(RebarCalculate.disableProperty());
    }

    @FXML
    private void GenerateGraph(ActionEvent actionEvent) {
        getInputs();
        showInputWarning();
        calculateMoments();
        GetLineChart lineChart;
        if(mSupportMomentForfaitaire.isConditionsVerified()){
            lineChart = new GetLineChart(
                    mSpanMomentFunctionCaquot,
                    mSpanMomentFunctionForfaitaire,
                    mSpanMomentFunction3Moment
            );
        } else {
            lineChart = new GetLineChart(
                    mSpanMomentFunctionCaquot,
                    mSpanMomentFunction3Moment
            );
            showForfaitaireCondWarning(mSupportMomentForfaitaire.getInvalidatedConditions());
        }
    }

    @FXML
    private void CalculateRebar(ActionEvent actionEvent) {
        getInputs();
        calculateMoments();
        mReinforcement = new Reinforcement(mSpanMomentFunction3Moment);
        GetReinforcementResult reinforcementResult = new GetReinforcementResult(mReinforcement);
    }

    @FXML
    private void DEBUG(ActionEvent actionEvent) throws Exception {

        ELUCombination combination = new ELUCombination(mSpanMomentFunction3Moment);
        MomentRedistribution momentRedistribution = new MomentRedistribution(combination);

        momentRedistribution.getRedistributionCoefMap().forEach((supportId, redistributionCoef) -> {
            System.out.printf("for the support %d, the redistribution coef is %.4f\n", supportId, redistributionCoef);
        });

        momentRedistribution.getSupportMuMap_BR().forEach((supportId, supportMoment) -> {
            System.out.printf("for the support %d, the support moment before redistribution is %.4f\n", supportId, supportMoment);
        });

        momentRedistribution.getFinalRedistributionCoefMap().forEach((supportId, finalRedistributionCoef) -> {
            System.out.printf("for the support %d, the final redistribution coef is %.4f\n", supportId, finalRedistributionCoef);
        });

        System.out.printf("When span 2 Max, the support 2 moment is %.4f\n", combination.getSupportMomentWhenSpanMomentMax(2, 2));
        System.out.printf("When support 3 Max, the support 2 moment is %.4f\n", combination.getSupportMomentWhenSupportMomentMin(2, 3));
        System.out.printf("When span 2 Max, the span 2 max moment is %.4f\n", combination.getSpanMaxMomentWhenSpanMomentMax(2, 2));
        System.out.printf("When support 3 Max, the span 2 max moment is %.4f\n", combination.getSpanMaxMomentWhenSupportMomentMin(2, 3));

        SpanMomentFunction spacialLoadCaseFunction = new SpanMomentFunction(combination.getSpecialLoadCaseSupportMomentMap());
        spacialLoadCaseFunction.getSpecialLoadCaseSpanFunction().forEach((spanId, functionMap) ->
                functionMap.forEach((loadCase, function)->{
                    System.out.printf("On the span %d : \n", spanId);
                    System.out.printf("Under the load case of %d, the function is %s\n", loadCase, function);
        }));
    }


    public boolean isNotEqualSpan() {
        return notEqualSpan.get();
    }

    public BooleanProperty notEqualSpanProperty() {
        return notEqualSpan;
    }

    public boolean isNotEqualSupport() {
        return notEqualSupport.get();
    }

    public BooleanProperty notEqualSupportProperty() {
        return notEqualSupport;
    }

    public static boolean isDisabledRebarCalculate() {
        return isDisabledRebarCalculate.get();
    }

    public BooleanProperty isDisabledRebarCalculateProperty() {
        return isDisabledRebarCalculate;
    }
}
