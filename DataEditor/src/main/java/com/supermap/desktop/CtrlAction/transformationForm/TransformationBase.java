package com.supermap.desktop.CtrlAction.transformationForm;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetVector;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.dataeditor.DataEditorProperties;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.event.ActiveLayersChangedEvent;
import com.supermap.desktop.event.ActiveLayersChangedListener;
import com.supermap.desktop.ui.LayersComponentManager;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.NodeDataType;
import com.supermap.desktop.ui.controls.TreeNodeData;
import com.supermap.desktop.utilities.MapUtilities;
import com.supermap.mapping.Layer;
import com.supermap.mapping.LayerGroup;
import com.supermap.mapping.Map;
import com.supermap.ui.MapControl;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author XiaJT
 */
public abstract class TransformationBase implements IFormMap {

	private boolean isAddedListener = false;
	// 我觉得图层选中改变应该和图层管理器相关，而和地图窗口无关
	private transient LayersTreeSelectionListener layersTreeSelectionListener = new LayersTreeSelectionListener();
	private transient EventListenerList eventListenerList = new EventListenerList();
	private ArrayList<Layer> activeLayersList = new ArrayList<Layer>();
	private int isShowPopupMenu = 0;
	private List<String> lastSelectedGeometry = new ArrayList<>();

	@Override
	public String getText() {
		return getMapControl().getMap().getName();
	}


	@Override
	public WindowType getWindowType() {
		return WindowType.TRANSFORMATION;
	}

	@Override
	public void actived() {
		addListeners();
		LayersComponentManager layersComponentManager = UICommonToolkit.getLayersManager();
		layersComponentManager.setMap(getMapControl().getMap());
	}

	@Override
	public void deactived() {
		removeListeners();
		LayersComponentManager layersComponentManager = UICommonToolkit.getLayersManager();
		layersComponentManager.setMap(null);
	}


	@Override
	public void windowShown() {

	}

	@Override
	public void windowHidden() {

	}

	@Override
	public void clean() {

	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public MapControl getMapControl() {
		return null;
	}

	@Override
	public Layer[] getActiveLayers() {
		Layer[] layers = new Layer[this.activeLayersList.size()];
		this.activeLayersList.toArray(layers);
		return layers;
	}

	@Override
	public void setActiveLayers(Layer... activeLayers) {

	}

	@Override
	public void addActiveLayersChangedListener(ActiveLayersChangedListener listener) {
		eventListenerList.add(ActiveLayersChangedListener.class, listener);
	}

	@Override
	public void removeActiveLayersChangedListener(ActiveLayersChangedListener listener) {
		eventListenerList.remove(ActiveLayersChangedListener.class, listener);
	}

	@Override
	public void removeActiveLayersByDatasets(Dataset... datasets) {

	}

	@Override
	public void dontShowPopupMenu() {
		isShowPopupMenu++;
	}

	@Override
	public void showPopupMenu() {
		isShowPopupMenu--;
	}

	@Override
	public int getIsShowPopupMenu() {
		return isShowPopupMenu;
	}

	@Override
	public void updataSelectNumber() {

	}

	@Override
	public void setSelectedGeometryProperty() {

	}

	@Override
	public void openMap(String mapName) {

	}

	@Override
	public int getSelectedCount() {
		return 0;
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public void setText(String text) {

	}

	@Override
	public boolean save(boolean notify, boolean isNewWindow) {
		return false;
	}

	@Override
	public boolean saveFormInfos() {
		return false;
	}

	@Override
	public boolean saveAs(boolean isNewWindow) {
		return false;
	}

	@Override
	public boolean isNeedSave() {
		return false;
	}

	@Override
	public void setNeedSave(boolean needSave) {

	}

	@Override
	public boolean isActivated() {
		return false;
	}

	public void removeLayers(Layer[] layers) {
		try {
			if (layers != null && layers.length > 0) {
				ArrayList<String> removingLayers = new ArrayList<String>();
				String message = "";
				if (layers.length == 1) {
					message = String.format(DataEditorProperties.getString("String_validateRemoveLayerMessage"), layers[0].getCaption());
				} else {
					message = MessageFormat.format(DataEditorProperties.getString("String_validateRemoveRangeMessage"), layers.length);
				}

				int result = UICommonToolkit.showConfirmDialog(message);
				if (result == JOptionPane.OK_OPTION) {
					for (Layer layer : layers) {
						if (layer instanceof LayerGroup) {
							ArrayList<Layer> childLayers = MapUtilities.getLayers((LayerGroup) layer);
							for (Layer childLayer : childLayers) {
								Dataset dataset = childLayer.getDataset();
								if (dataset == null) {
									if (childLayer.getBounds().getWidth() > 0 || childLayer.getBounds().getHeight() > 0) {
										break;
									}
								} else {
									// 有可能存在一个点的数据集，所以还是用记录集来判断吧

									if (dataset instanceof DatasetVector && ((DatasetVector) dataset).getRecordCount() > 0) {
										break;
									}
								}
							}
						} else {
							Dataset dataset = layer.getDataset();
							if (dataset == null) {
								if (layer.getBounds().getWidth() > 0 || layer.getBounds().getHeight() > 0) {
									break;
								}
							} else {
								// 有可能存在一个点的数据集，所以还是用记录集来判断吧

								if (dataset instanceof DatasetVector && ((DatasetVector) dataset).getRecordCount() > 0) {
									// do nothing

								}
							}
						}

						removingLayers.add(layer.getName());
					}

					for (int i = 0; i < removingLayers.size(); i++) {
						MapUtilities.removeLayer(this.getMapControl().getMap(), removingLayers.get(i));
					}

					this.getMapControl().getMap().refresh();
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	private void addListeners() {
		if (!isAddedListener) {
			UICommonToolkit.getLayersManager().getLayersTree().addTreeSelectionListener(this.layersTreeSelectionListener);
			isAddedListener = true;
			initDrag();
		}
	}

	private void initDrag() {
		new DropTarget(getMapControl(), DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {

			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					if (data instanceof DefaultMutableTreeNode[]) {
						DefaultMutableTreeNode[] defaultMutableTreeNodes = (DefaultMutableTreeNode[]) data;
						ArrayList<Object> objects = new ArrayList<>();
						for (DefaultMutableTreeNode defaultMutableTreeNode : defaultMutableTreeNodes) {
							if (defaultMutableTreeNode.getUserObject() instanceof TreeNodeData && (((TreeNodeData) defaultMutableTreeNode.getUserObject()).getData() instanceof Dataset || ((TreeNodeData) defaultMutableTreeNode.getUserObject()).getData() instanceof Map)) {
								objects.add(((TreeNodeData) defaultMutableTreeNode.getUserObject()).getData());
							}
						}
						addDatas(objects);
					}
				} catch (Exception e) {
					Application.getActiveApplication().getOutput().output(e);
				}
			}
		});
	}

	protected abstract void addDatas(List<Object> dataset);

	private void removeListeners() {
		if (isAddedListener) {
			UICommonToolkit.getLayersManager().getLayersTree().removeTreeSelectionListener(this.layersTreeSelectionListener);
			isAddedListener = false;
		}
	}

	private class LayersTreeSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			LayersTreeSelectionChanged();
		}

	}

	private void LayersTreeSelectionChanged() {
		TreePath[] selectedPaths = UICommonToolkit.getLayersManager().getLayersTree().getSelectionPaths();
		Layer[] oldActiveLayers = getActiveLayers();

		this.activeLayersList.clear();
		if (selectedPaths != null) {
			for (TreePath path : selectedPaths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

				if (node != null) {
					TreeNodeData nodeData = (TreeNodeData) node.getUserObject();

					if (isNodeLayer(nodeData.getType()) && nodeData.getData() instanceof Layer) {
						this.activeLayersList.add((Layer) nodeData.getData());
					}
				}
			}
		}

		if (!this.activeLayersList.isEmpty()) {
			fireActiveLayersChanged(new ActiveLayersChangedEvent(this, oldActiveLayers, getActiveLayers()));
		} else if (oldActiveLayers != null) {
			fireActiveLayersChanged(new ActiveLayersChangedEvent(this, oldActiveLayers, null));
		}
	}

	protected void fireActiveLayersChanged(ActiveLayersChangedEvent e) {
		Object[] listeners = eventListenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActiveLayersChangedListener.class) {
				((ActiveLayersChangedListener) listeners[i + 1]).acitiveLayersChanged(e);
			}
		}
	}

	private boolean isNodeLayer(NodeDataType nodeDataType) {
		return nodeDataType == NodeDataType.LAYER || nodeDataType == NodeDataType.LAYER_IMAGE || nodeDataType == NodeDataType.LAYER_THEME
				|| nodeDataType == NodeDataType.LAYER_GRID || nodeDataType == NodeDataType.THEME_UNIQUE || nodeDataType == NodeDataType.THEME_RANGE
				|| nodeDataType == NodeDataType.THEME_LABEL_ITEM || nodeDataType == NodeDataType.THEME_UNIQUE_ITEM
				|| nodeDataType == NodeDataType.THEME_RANGE_ITEM || nodeDataType == NodeDataType.LAYER_GROUP
				|| nodeDataType == NodeDataType.DATASET_IMAGE_COLLECTION || nodeDataType == NodeDataType.DATASET_GRID_COLLECTION
				|| nodeDataType == NodeDataType.THEME_CUSTOM;
	}

	public void setSelectedGeoCompoundTags(String... selectedGeoCompoundTags) {
		lastSelectedGeometry.clear();
		if (selectedGeoCompoundTags != null) {
			Collections.addAll(lastSelectedGeometry, selectedGeoCompoundTags);
		}
	}

	public List<String> getLastSelectedGeometry() {
		return lastSelectedGeometry;
	}

	@Override
	public void setVisibleScales(double[] scales) {
		getMapControl().getMap().setVisibleScales(scales);
	}

	@Override
	public void setVisibleScalesEnabled(boolean isVisibleScalesEnabled) {
		getMapControl().getMap().setVisibleScalesEnabled(isVisibleScalesEnabled);
	}
}
