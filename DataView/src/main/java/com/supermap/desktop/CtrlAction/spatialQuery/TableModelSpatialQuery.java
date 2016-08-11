package com.supermap.desktop.CtrlAction.spatialQuery;

import com.supermap.data.DatasetType;
import com.supermap.data.Datasource;
import com.supermap.data.SpatialQueryMode;
import com.supermap.desktop.dataview.DataViewProperties;
import com.supermap.desktop.utilities.StringUtilities;
import com.supermap.mapping.Layer;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author XiaJT
 */
public class TableModelSpatialQuery extends DefaultTableModel {
	private ArrayList<TableRowData> rowDatas;
	private final String defaultDatasetName = "SpatialQuery";

	private String[] columns = new String[]{
			"",
			com.supermap.desktop.dataview.DataViewProperties.getString("String_Type"),
			DataViewProperties.getString("String__SearchedLayerName"),
			DataViewProperties.getString("String_SpatialQueryMode"),
			DataViewProperties.getString("String_TabularQueryCondition"),
	};
	private ArrayList<DatasetType> supportDatasetTypes;

	public TableModelSpatialQuery() {
		super();
		rowDatas = new ArrayList<>();
		supportDatasetTypes = new ArrayList<>();
		supportDatasetTypes.add(DatasetType.POINT);
		supportDatasetTypes.add(DatasetType.LINE);
		supportDatasetTypes.add(DatasetType.REGION);
		supportDatasetTypes.add(DatasetType.NETWORK);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else if (columnIndex == 1) {
			return DatasetType.class;
		} else if (columnIndex == 2) {
			return String.class;
		} else if (columnIndex == 3) {
			return SpatialQueryMode.class;
		} else if (columnIndex == 4) {
			return String.class;
		}
		return String.class;
	}

	@Override
	public int getRowCount() {
		if (rowDatas == null) {
			return 0;
		}
		return rowDatas.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column != 1 && column != 2;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return rowDatas.get(row).isSelected();
		} else if (column == 1) {
			return rowDatas.get(row).getDatasetType();
		} else if (column == 2) {
			return rowDatas.get(row).getLayerName();
		} else if (column == 3) {
			return rowDatas.get(row).getSpatialQueryMode();
		} else if (column == 4) {
			return rowDatas.get(row).getSql();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (column == 0) {
			rowDatas.get(row).setSelected(((Boolean) aValue));
		} else if (column == 3) {
			rowDatas.get(row).setSpatialQueryMode(((SpatialQueryMode) aValue));
		} else if (column == 4) {
			rowDatas.get(row).setSql((String) aValue);
		}
	}

	public void setLayers(ArrayList<Layer> layers) {
		if (rowDatas != null) {
			rowDatas.clear();
		}
		if (layers != null && layers.size() > 0) {
			for (Layer layer : layers) {
				if (layer.getDataset() != null && supportDatasetTypes.contains(layer.getDataset().getType())) {
					TableRowData rowData = new TableRowData(layer);
					rowData.setResultDataset(rowDatas.size() == 0 ? defaultDatasetName : defaultDatasetName + "_" + rowDatas.size());
					rowDatas.add(rowData);
				}
			}
		}
	}

	public void setIsSave(int[] selectedRows, boolean isSave) {
		for (int selectedRow : selectedRows) {
			rowDatas.get(selectedRow).setSave(isSave);
		}
	}

	public void setDatasource(int[] selectedRows, Datasource datasource) {
		for (int selectedRow : selectedRows) {
			if (datasource != rowDatas.get(selectedRow).getResultDatasource()) {
				rowDatas.get(selectedRow).setResultDatasource(datasource);
				rowDatas.get(selectedRow).setResultDataset(datasource.getDatasets().getAvailableDatasetName(rowDatas.get(selectedRow).getResultDataset()));
			}
		}
	}

	public boolean isSupportDatasetName(int row, String datasetName) {
		if (StringUtilities.isNullOrEmpty(datasetName)) {
			// 空
			return false;
		}
		TableRowData currentRowData = rowDatas.get(row);
		Datasource datasource = currentRowData.getResultDatasource();
		if (!datasource.getDatasets().isAvailableDatasetName(datasetName)) {
			// 数据源内存在
			return false;
		}

//		for (TableRowData rowData : rowDatas) {
//			if (rowData != currentRowData && rowData.isSave() && rowData.getResultDatasource() == datasource && datasetName.equalsIgnoreCase(rowData.getResultDataset())) {
//				// 当前确认保存的数据集
//				return false;
//			}
//		}
		return true;
	}

	public void setDatasetName(int row, String datasetName) {
		rowDatas.get(row).setResultDataset(datasetName);
	}

	public void setIsOnlySaveSpatialInfo(int[] rows, boolean isOnlySaveSpatialInfo) {
		for (int row : rows) {
			rowDatas.get(row).setOnlySaveSpatialInfo(isOnlySaveSpatialInfo);
		}
	}

	public void setShowInTabular(int[] rows, boolean isShowInTabular) {
		for (int row : rows) {
			rowDatas.get(row).setShowInTabular(isShowInTabular);
		}
	}

	public Boolean isShowInTabular(int[] rows) {
		Boolean result = null;
		for (int row : rows) {
			if (result == null) {
				result = rowDatas.get(row).isShowInTabular();
			}else {
				if (result != rowDatas.get(row).isShowInTabular()) {
					result = null;
				}
			}
		}
		return result;
	}

	public void setShowInMap(int[] rows, boolean isShowInMap) {
		for (int row : rows) {
			rowDatas.get(row).setShowInMap(isShowInMap);
		}
	}

	public Boolean isShowInMap(int[] rows) {
		Boolean result = null;
		for (int row : rows) {
			if (result == null) {
				result = rowDatas.get(row).isShowInMap();
			} else {
				if (result != rowDatas.get(row).isShowInMap()) {
					result = null;
				}
			}
		}
		return result;
	}

	public void setShowInScene(int[] rows, boolean isShowInScene) {
		for (int row : rows) {
			rowDatas.get(row).setShowInScene(isShowInScene);
		}
	}

	public Boolean isShowInScene(int[] rows) {
		Boolean result = null;

		for (int row : rows) {
			if (result == null) {
				result = rowDatas.get(row).isShowInScene();
			} else {
				if (result != rowDatas.get(row).isShowInScene()) {
					result = null;
				}
			}
		}
		return result;
	}


//	private String getSuitDatasetName(Datasource datasource, int row) {
//		ArrayList<String> existDatasetNames = new ArrayList<>();
//		TableRowData currentRowData = rowDatas.get(row);
//		String datasetName = currentRowData.getResultDataset();
//		for (TableRowData rowData : rowDatas) {
//			if (rowData != currentRowData && rowData.isSave() && rowData.getResultDatasource() == datasource && !StringUtilities.isNullOrEmpty(rowData.getResultDataset())) {
//				existDatasetNames.add(rowData.getResultDataset());
//			}
//		}
//		return DatasetUtilities.getAvailableDatasetName(datasource, datasetName, existDatasetNames.toArray(new String[existDatasetNames.size()]));
//	}
}