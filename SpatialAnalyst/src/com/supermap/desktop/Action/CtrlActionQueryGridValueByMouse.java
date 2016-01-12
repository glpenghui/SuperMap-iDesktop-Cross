package com.supermap.desktop.Action;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import com.supermap.data.Colors;
import com.supermap.data.Dataset;
import com.supermap.data.DatasetGrid;
import com.supermap.data.DatasetImage;
import com.supermap.data.Datasource;
import com.supermap.data.PixelFormat;
import com.supermap.data.Point2D;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.spatialanalyst.SpatialAnalystProperties;
import com.supermap.mapping.Layer;
import com.supermap.mapping.Layers;
import com.supermap.mapping.Map;
import com.supermap.ui.Action;
import com.supermap.ui.MapControl;

public class CtrlActionQueryGridValueByMouse extends CtrlAction {
	TransparentBackground transparentBackground;
	MapControl mapControl;
	private final int DEFUALT_WIGHT = 240;
	private final int DEFUALT_HEIGHT = 150;
	private IFormMap formMap;

	private void hideTransparentBackground() {
		transparentBackground.setVisible(false);
		mapControl.removeKeyListener(keyAdapter);
		mapControl.removeMouseListener(mouseAdapter);
		// 允许弹出右键菜单
		formMap.showPopupMenu();
	}

	KeyAdapter keyAdapter = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
				hideTransparentBackground();
			}
		}
	};

	MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
				hideTransparentBackground();
			}
		}
	};

	public CtrlActionQueryGridValueByMouse(IBaseItem caller, IForm formClass) {
		super(caller, formClass);

	}

	@Override
	public void run() {
		try {
			formMap = (IFormMap) Application.getActiveApplication().getActiveForm();
			if (null != formMap) {
				mapControl = formMap.getMapControl();
				mapControl.setAction(Action.SELECT);
				final DecimalFormat format = new DecimalFormat("######0.000000");
				mapControl.addMouseMotionListener(new MouseAdapter() {
					@Override
					public void mouseMoved(MouseEvent arg0) {
						abstractMapcontrolMouseMoved(format, arg0);
					}
				});
				if (null == transparentBackground) {
					transparentBackground = new TransparentBackground();
					transparentBackground.setSize(DEFUALT_WIGHT, DEFUALT_HEIGHT);
				} else if (false == transparentBackground.isVisible()) {
					transparentBackground.setVisible(true);
				}
				// 不弹出菜单
				formMap.dontShowPopupMenu();

				// 添加监听事件
				mapControl.addMouseListener(mouseAdapter);
				mapControl.addKeyListener(keyAdapter);

				mapControl.setLayout(null);
				mapControl.add(transparentBackground);

			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
	}

	private void abstractMapcontrolMouseMoved(final DecimalFormat format, MouseEvent arg0) {
		Point point = arg0.getPoint();
		Point2D point2D = mapControl.getMap().pixelToMap(point);
		transparentBackground.setLocation(point.x + 15, point.y);
		Map map = mapControl.getMap();
		Layers layers = map.getLayers();
		String currentDatasource = transparentBackground.getjLabelDatasource().getText();
		String currentDataset = transparentBackground.getjLabelDataset().getText();
		String currentPointX = transparentBackground.getjLabelPointX().getText();
		String currentPointY = transparentBackground.getjLabelPointY().getText();
		String currentRow = "";
		String currentColumn = "";
		String currentValue = "";
		Dataset dataset = null;
		// 添加不同的栅格数据集到同一副地图时，通过栅格图层的边界来判断鼠标指向的是哪一个栅格数据集
		for (int i = 0; i < layers.getCount(); i++) {
			Layer layer = layers.get(i);
			Dataset tempDataset = layer.getDataset();
			if ((tempDataset instanceof DatasetGrid || tempDataset instanceof DatasetImage) && layer.getBounds().contains(point2D)) {
				dataset = tempDataset;
			}

		}
		String pointX = format.format(point2D.getX());
		String pointY = format.format(point2D.getY());
		if (null != dataset) {
			Datasource datasource = dataset.getDatasource();
			currentDatasource = getTargetString(currentDatasource, datasource.getAlias());
			currentDataset = getTargetString(currentDataset, dataset.getName());
			currentPointX = getTargetString(currentPointX, pointX);
			currentPointY = getTargetString(currentPointY, pointY);
			if (dataset instanceof DatasetGrid) {
				Point grid = ((DatasetGrid) dataset).xyToGrid(point2D);
				int row = (int) grid.getX();
				int column = (int) grid.getY();
				currentRow = SpatialAnalystProperties.getString("String_QueryGridValue_Row").replace("{0}", String.valueOf(row));
				currentColumn = SpatialAnalystProperties.getString("String_QueryGridValue_Collunm").replace("{0}", String.valueOf(column));
				currentValue = SpatialAnalystProperties.getString("String_QueryGridValue_GridValue").replace("{0}",
						String.valueOf((int) ((DatasetGrid) dataset).getValue(row, column)));
			} else {
				Point grid = ((DatasetImage) dataset).xyToImage(point2D);
				int row = (int) grid.getY();
				int column = (int) grid.getX();
				currentRow = SpatialAnalystProperties.getString("String_QueryImageColor_Row").replace("{0}", String.valueOf(row));
				currentColumn = SpatialAnalystProperties.getString("String_QueryImageColor_Column").replace("{0}", String.valueOf(column));
				DatasetImage datasetImage = ((DatasetImage) dataset);
				PixelFormat firstBandPixel = datasetImage.getPixelFormat(0);
				if (firstBandPixel == PixelFormat.RGB || firstBandPixel == PixelFormat.RGBA) {
					for (int i = 0; i < datasetImage.getBandCount(); i++)
					{
						int argb = (int) datasetImage.getValue(column, row, 0);
						int[] argbs = new int[4];
						argbs[0] = argb >> 24 & 0xFF;
						argbs[1] = argb >> 16 & 0xFF;
						argbs[2] = argb >> 8 & 0xFF;
						argbs[3] = argb & 0xFF;
						currentValue = SpatialAnalystProperties.getString("String_QueryImageBandColor").replace("{0}",
								"[A=" + argbs[0] + ", R=" + argbs[1] + ", G=" + argbs[2] + ", B=" + argbs[3] + "]");
					}
				} else {
					for (int i = 0; i < datasetImage.getBandCount(); i++)
					{
						currentValue = SpatialAnalystProperties.getString("String_QueryImageBandColor").replace("{0}",
								String.valueOf((int) datasetImage.getValue(row, column, i)));
					}
				}
			}
			transparentBackground.getjLabelDatasource().setText(currentDatasource);
			transparentBackground.getjLabelDataset().setText(currentDataset);
			transparentBackground.getjLabelPointX().setText(currentPointX);
			transparentBackground.getjLabelPointY().setText(currentPointY);
			transparentBackground.getjLabelRowOfGrid().setText(currentRow);
			transparentBackground.getjLabelColumnOfGrid().setText(currentColumn);
			transparentBackground.getjLabelGridValue().setText(currentValue);
			Dimension datasetDimension = transparentBackground.getjLabelDataset().getPreferredSize();
			Dimension datasourceDimension = transparentBackground.getjLabelDatasource().getPreferredSize();
			Dimension maxDimension = new Dimension();
			if (datasetDimension.getWidth() > datasourceDimension.getWidth()) {
				maxDimension = datasetDimension;
			} else {
				maxDimension = datasourceDimension;
			}
			// 重新设定半透明界面宽高
			if (DEFUALT_WIGHT > (int) maxDimension.getWidth() + 15) {
				transparentBackground.setSize(DEFUALT_WIGHT, DEFUALT_HEIGHT);
			} else {
				transparentBackground.setSize((int) maxDimension.getWidth() + 15, DEFUALT_HEIGHT);
			}
			transparentBackground.repaint();
		} else {
			currentDatasource = getTargetString(currentDatasource, "-");
			currentDataset = getTargetString(currentDataset, "-");
			currentPointX = getTargetString(currentPointX, pointX);
			currentPointY = getTargetString(currentPointY, pointY);
			currentRow = SpatialAnalystProperties.getString("String_QueryGridValue_Row").replace("{0}", "-");
			currentColumn = SpatialAnalystProperties.getString("String_QueryGridValue_Collunm").replace("{0}", "-");
			currentValue = SpatialAnalystProperties.getString("String_QueryGridValue_GridValue").replace("{0}", "-");
			transparentBackground.getjLabelDatasource().setText(currentDatasource);
			transparentBackground.getjLabelDataset().setText(currentDataset);
			transparentBackground.getjLabelPointX().setText(currentPointX);
			transparentBackground.getjLabelPointY().setText(currentPointY);
			transparentBackground.getjLabelRowOfGrid().setText(currentRow);
			transparentBackground.getjLabelColumnOfGrid().setText(currentColumn);
			transparentBackground.getjLabelGridValue().setText(currentValue);
			transparentBackground.setSize(DEFUALT_WIGHT, DEFUALT_HEIGHT);
			transparentBackground.repaint();
		}
	}

	private String getTargetString(String targetString, String replaceInfo) {
		return targetString.replace(targetString.substring(targetString.indexOf(":") + 1, targetString.length()), replaceInfo);
	}

	@SuppressWarnings("unused")
	@Override
	public boolean enable() {
		boolean enable = false;
		IFormMap formMap = null;
		if (null != Application.getActiveApplication().getActiveForm() && (Application.getActiveApplication().getActiveForm() instanceof IFormMap)) {
			formMap = (IFormMap) Application.getActiveApplication().getActiveForm();
			Map map = formMap.getMapControl().getMap();
			if (null != map.getLayers()) {
				for (int i = 0; i < map.getLayers().getCount(); i++) {
					Layer layer = map.getLayers().get(i);
					Dataset dataset = layer.getDataset();
					if (dataset instanceof DatasetGrid || dataset instanceof DatasetImage) {
						enable = true;
					}
				}
			}
		}

		return enable;
	}
}