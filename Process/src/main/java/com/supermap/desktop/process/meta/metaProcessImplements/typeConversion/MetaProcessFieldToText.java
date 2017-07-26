package com.supermap.desktop.process.meta.metaProcessImplements.typeConversion;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.geometry.Abstract.IGeometry;
import com.supermap.desktop.geometry.Abstract.ILineFeature;
import com.supermap.desktop.geometry.Abstract.IRegionFeature;
import com.supermap.desktop.geometry.Implements.DGeometryFactory;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.implement.EqualDatasetConstraint;
import com.supermap.desktop.process.constraint.implement.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.meta.MetaKeys;
import com.supermap.desktop.process.parameter.implement.*;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;
import com.supermap.desktop.utilities.RecordsetUtilities;

import java.util.Map;

/**
 * Created By Chens on 2017/7/22 0022
 */
public class MetaProcessFieldToText extends MetaProcessTypeConversion {
    private static final String INPUT_DATA = "InputData";
    private static final String OUTPUT_DATA = "FieldToTextResult";

    private ParameterDatasourceConstrained inputDatasource;
    private ParameterSingleDataset inputDataset;
    private ParameterSaveDataset outputData;
    private ParameterFieldComboBox fieldComboBox;

    public MetaProcessFieldToText() {
        initParameters();
        initParameterConstraint();
    }

    private void initParameters() {
        inputDatasource = new ParameterDatasourceConstrained();
        inputDataset = new ParameterSingleDataset(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION, DatasetType.TEXT, DatasetType.POINT3D, DatasetType.LINE3D, DatasetType.REGION3D, DatasetType.MODEL);
        outputData = new ParameterSaveDataset();
        fieldComboBox = new ParameterFieldComboBox(ProcessProperties.getString("String_ExportField"));

        Dataset dataset = DatasetUtilities.getDefaultDataset(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION, DatasetType.TEXT, DatasetType.POINT3D, DatasetType.LINE3D, DatasetType.REGION3D, DatasetType.MODEL);
        if (dataset != null) {
            inputDatasource.setSelectedItem(dataset.getDatasource());
            inputDataset.setSelectedItem(dataset);
        }
        outputData.setSelectedItem("result_fieldToText");

        ParameterCombine inputCombine = new ParameterCombine();
        inputCombine.setDescribe(CommonProperties.getString("String_GroupBox_SourceData"));
        inputCombine.addParameters(inputDatasource, inputDataset);
        ParameterCombine outputCombine = new ParameterCombine();
        outputCombine.setDescribe(CommonProperties.getString("String_GroupBox_ResultData"));
        outputCombine.addParameters(outputData);
        ParameterCombine settingCombine = new ParameterCombine();
        settingCombine.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));
        settingCombine.addParameters(fieldComboBox);

        parameters.setParameters(inputCombine, settingCombine, outputCombine);
        parameters.addInputParameters(INPUT_DATA, DatasetTypes.VECTOR,inputCombine);
        parameters.addOutputParameters(OUTPUT_DATA, DatasetTypes.TEXT,outputCombine);
    }

    private void initParameterConstraint() {
        EqualDatasourceConstraint equalDatasourceConstraint = new EqualDatasourceConstraint();
        equalDatasourceConstraint.constrained(inputDatasource,ParameterDatasourceConstrained.DATASOURCE_FIELD_NAME);
        equalDatasourceConstraint.constrained(inputDataset,ParameterSingleDataset.DATASOURCE_FIELD_NAME);

        EqualDatasetConstraint equalDatasetConstraint = new EqualDatasetConstraint();
        equalDatasetConstraint.constrained(inputDataset,ParameterSingleDataset.DATASET_FIELD_NAME);
        equalDatasetConstraint.constrained(fieldComboBox,ParameterFieldComboBox.DATASET_FIELD_NAME);
    }

    @Override
    public IParameters getParameters() {
        return parameters;
    }

    @Override
    public boolean execute() {
        boolean isSuccessful = false;
        Recordset recordsetResult = null;

        try {
            fireRunning(new RunningEvent(this,0,"start"));

            DatasetVector src = null;
            if (parameters.getInputs().getData(INPUT_DATA).getValue() != null) {
                src = (DatasetVector) parameters.getInputs().getData(INPUT_DATA).getValue();
            } else {
                src = (DatasetVector) inputDataset.getSelectedDataset();
            }
            DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
            datasetVectorInfo.setName(outputData.getResultDatasource().getDatasets().getAvailableDatasetName(outputData.getDatasetName()));
            datasetVectorInfo.setType(DatasetType.TEXT);
            DatasetVector resultDataset = outputData.getResultDatasource().getDatasets().create(datasetVectorInfo);
            resultDataset.setPrjCoordSys(src.getPrjCoordSys());
            for (int i = 0; i < src.getFieldInfos().getCount(); i++) {
                FieldInfo fieldInfo = src.getFieldInfos().get(i);
                if (!fieldInfo.isSystemField() && !fieldInfo.getName().toLowerCase().equals("smuserid")) {
                    resultDataset.getFieldInfos().add(fieldInfo);
                }
            }
            recordsetResult = resultDataset.getRecordset(false, CursorType.DYNAMIC);
            recordsetResult.addSteppedListener(steppedListener);
            recordsetResult.getBatch().setMaxRecordCount(2000);
            recordsetResult.getBatch().begin();
            String fieldName = fieldComboBox.getFieldName();

            Recordset recordsetInput = src.getRecordset(false, CursorType.DYNAMIC);
            while (!recordsetInput.isEOF()) {
                IGeometry geometry = null;
                try {
                    geometry = DGeometryFactory.create(recordsetInput.getGeometry());
                    Map<String, Object> value = mergePropertyData(resultDataset, recordsetInput.getFieldInfos(), RecordsetUtilities.getFieldValuesIgnoreCase(recordsetInput));
                    isSuccessful = convert(recordsetResult, geometry, value, recordsetInput.getFieldValue(fieldName).toString());
                }finally {
                    if (geometry != null) {
                        geometry.dispose();
                    }
                }
                recordsetInput.moveNext();
            }
            recordsetResult.getBatch().update();
            recordsetInput.close();
            recordsetInput.dispose();
            this.getParameters().getOutputs().getData(OUTPUT_DATA).setValue(resultDataset);
            fireRunning(new RunningEvent(this,100,"finish"));
        } catch (Exception e) {
            Application.getActiveApplication().getOutput().output(e);
        } finally {
            if (recordsetResult != null) {
                recordsetResult.removeSteppedListener(steppedListener);
                recordsetResult.close();
                recordsetResult.dispose();
            }
        }

        return isSuccessful;
    }

    @Override
    public String getKey() {
        return MetaKeys.CONVERSION_FIELD_TO_TEXT;
    }

    @Override
    public String getTitle() {
        return ProcessProperties.getString("String_Title_FieldToText");
    }

    private boolean convert(Recordset recordset, IGeometry geometry, Map<String, Object> value,String fieldName) {
        boolean isConvert = true;
        if (geometry instanceof ILineFeature) {
            GeoLine geoLine = ((ILineFeature) geometry).convertToLine(120);
            for (int i = 0; i < geoLine.getPartCount(); i++) {
                Point2Ds points = geoLine.getPart(i);
                for (int j = 0; j < points.getCount(); j++) {
                    TextPart textPart = new TextPart(fieldName, points.getItem(j));
                    GeoText geoText = new GeoText(textPart);
                    recordset.addNew(geoText, value);
                    textPart.dispose();
                    geoText.dispose();
                }
            }
            geoLine.dispose();
        } else if (geometry instanceof IRegionFeature) {
            GeoRegion geoRegion = ((IRegionFeature) geometry).convertToRegion(120);
            for (int i = 0; i < geoRegion.getPartCount(); i++) {
                Point2Ds points = geoRegion.getPart(i);
                for (int j = 0; j < points.getCount(); j++) {
                    TextPart textPart = new TextPart(fieldName, points.getItem(j));
                    GeoText geoText = new GeoText(textPart);
                    recordset.addNew(geoText, value);
                    textPart.dispose();
                    geoText.dispose();
                }
            }
            geoRegion.dispose();
        } else if (geometry.getGeometry().getType().equals(GeometryType.GEOPOINT)) {
            GeoPoint geoPoint = (GeoPoint) geometry.getGeometry();
            TextPart textPart = new TextPart(fieldName, new Point2D(geoPoint.getX(), geoPoint.getY()));
            GeoText geoText = new GeoText(textPart);
            recordset.addNew(geoText, value);
            textPart.dispose();
            geoText.dispose();
        } else {
            isConvert = false;
        }

        return isConvert;
    }
}
