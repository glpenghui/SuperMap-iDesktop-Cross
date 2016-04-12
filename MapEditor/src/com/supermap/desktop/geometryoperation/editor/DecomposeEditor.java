package com.supermap.desktop.geometryoperation.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.supermap.data.DatasetVector;
import com.supermap.data.EditType;
import com.supermap.data.FieldInfos;
import com.supermap.data.GeoCompound;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoLine3D;
import com.supermap.data.GeoLineM;
import com.supermap.data.GeoRegion;
import com.supermap.data.GeoRegion3D;
import com.supermap.data.GeoText;
import com.supermap.data.Geometry;
import com.supermap.data.GeometryType;
import com.supermap.data.Recordset;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.utilties.ArrayUtilties;
import com.supermap.desktop.utilties.TabularUtilties;
import com.supermap.mapping.Layer;

public class DecomposeEditor extends AbstractEditor {

	@Override
	public void activate(EditEnvironment environment) {
		Recordset recordset = null;
		Geometry geometry = null;
		try {
			Layer layer = environment.getActiveEditableLayer();
			List<Integer> resultIDs = new ArrayList<Integer>();
			recordset = layer.getSelection().toRecordset();
			environment.getMapControl().getEditHistory().batchBegin();
			recordset.getBatch().setMaxRecordCount(200);
			recordset.getBatch().begin();
			recordset.moveLast();
			while (!recordset.isBOF()) {
				geometry = recordset.getGeometry();
				Geometry[] geometrys = null;

				if (geometry.getType() == GeometryType.GEOCOMPOUND) {
					geometrys = ((GeoCompound) geometry).divide(false);
					// CAD上的组合对象都是复杂对象，通过Divide能够分解到最简单对象
					// 只有一种情况，CAD上的岛洞数据是面对象需要再次分解，所以对CAD复杂对象深度分解的结果存在岛洞数据的情况需要单独再处理
					// 如果每一个对象不一次性分解完成，操作历史记录的时候会出现回退的时候多了一个中间对象
					List<Geometry> tempgeometrys = new ArrayList<Geometry>();
					Geometry[] tempgeometry = null;
					for (Geometry currentgeomentry : geometrys) {
						if (currentgeomentry.getType() == GeometryType.GEOREGION && ((GeoRegion) currentgeomentry).getPartCount() > 1) {
							tempgeometry = new Geometry[((GeoRegion) currentgeomentry).getPartCount()];
							for (int i = 0; i < ((GeoRegion) currentgeomentry).getPartCount(); i++) {
								tempgeometry[i] = new GeoRegion(((GeoRegion) currentgeomentry).getPart(i));
								tempgeometrys.add(tempgeometry[i]);
							}
						} else {
							tempgeometrys.add(currentgeomentry);
						}
					}
					geometrys = tempgeometrys.toArray(new Geometry[tempgeometrys.size()]);
				} else if (geometry.getType() == GeometryType.GEOLINE && ((GeoLine) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoLine) geometry).getPartCount()];
					for (int i = 0; i < ((GeoLine) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoLine(((GeoLine) geometry).getPart(i));
					}
				} else if (geometry.getType() == GeometryType.GEOLINE3D && ((GeoLine3D) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoLine3D) geometry).getPartCount()];
					for (int i = 0; i < ((GeoLine3D) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoLine3D(((GeoLine3D) geometry).getPart(i));
					}
				} else if (geometry.getType() == GeometryType.GEOREGION && ((GeoRegion) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoRegion) geometry).getPartCount()];
					for (int i = 0; i < ((GeoRegion) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoRegion(((GeoRegion) geometry).getPart(i));
					}
				} else if (geometry.getType() == GeometryType.GEOREGION3D && ((GeoRegion3D) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoRegion3D) geometry).getPartCount()];
					for (int i = 0; i < ((GeoRegion3D) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoRegion3D(((GeoRegion3D) geometry).getPart(i));
					}
				} else if (geometry.getType() == GeometryType.GEOLINEM && ((GeoLineM) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoLineM) geometry).getPartCount()];
					for (int i = 0; i < ((GeoLineM) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoLineM(((GeoLineM) geometry).getPart(i));
					}
				} else if (geometry.getType() == GeometryType.GEOTEXT && ((GeoText) geometry).getPartCount() > 1) {
					geometrys = new Geometry[((GeoText) geometry).getPartCount()];
					for (int i = 0; i < ((GeoText) geometry).getPartCount(); i++) {
						geometrys[i] = new GeoText(((GeoText) geometry).getPart(i), ((GeoText) geometry).getTextStyle());
						((GeoText) geometrys[i]).getPart(0).setRotation(((GeoText) geometry).getPart(i).getRotation());
					}
				}

				if (geometrys != null && geometrys.length > 1) {
					HashMap<String, Object> values = new HashMap<String, Object>();
					FieldInfos fieldInfos = recordset.getFieldInfos();
					Object[] fieldValues = recordset.getValues();

					environment.getMapControl().getEditHistory().add(EditType.DELETE, recordset, true);
					recordset.delete();
					for (int i = 0; i < fieldValues.length; i++) {
						if (!fieldInfos.get(i).isSystemField()) {
							values.put(fieldInfos.get(i).getName(), fieldValues[i]);
						}
					}
					for (int j = 0; j < geometrys.length; j++) {
						recordset.addNew(geometrys[j], values);
						resultIDs.add(recordset.getID());
						environment.getMapControl().getEditHistory().add(EditType.ADDNEW, recordset, true);
					}
				}
				recordset.movePrev();
			}
			recordset.getBatch().update();
			environment.getMapControl().getEditHistory().batchEnd();
			layer.getSelection().clear();
			if (resultIDs.size() > 0) {
				int[] ids = ArrayUtilties.convertToInt(resultIDs.toArray(new Integer[resultIDs.size()]));
				layer.getSelection().addRange(ids);
				TabularUtilties.refreshTabularForm((DatasetVector) layer.getDataset());
				// _Toolkit.InvokeGeometrySelectedEvent(formMap.MapControl, new GeometrySelectedEventArgs(resultIDs.Count));
				Application.getActiveApplication().getOutput()
						.output(MessageFormat.format(MapEditorProperties.getString("String_GeometryEdit_DecomposeSuccess"), resultIDs.size()));
			}
			environment.getMapControl().getMap().refresh();
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			recordset.getBatch().update();
			if (geometry != null) {
				geometry.dispose();
			}

			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}
		}
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		boolean bEnable = false;
		Recordset recordset = null;
		Geometry geometry = null;
		IFormMap formMap = environment.getFormMap();
		try {
			if (formMap != null && formMap.getMapControl().getActiveEditableLayer() != null) {

				Layer layer = formMap.getMapControl().getActiveEditableLayer();
				if (layer.getSelection().getCount() >= 1) {
					recordset = layer.getSelection().toRecordset();
					recordset.moveFirst();

					while (!recordset.isEOF()) {
						geometry = recordset.getGeometry();

						try {
							if (geometry == null) {
								break;
							} else if (geometry.getType() == GeometryType.GEOCOMPOUND) {
								bEnable = true;
								break;
							} else if (geometry.getType() == GeometryType.GEOLINE) {
								bEnable = ((GeoLine) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							} else if (geometry.getType() == GeometryType.GEOLINE3D) {
								bEnable = ((GeoLine3D) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							} else if (geometry.getType() == GeometryType.GEOREGION) {
								bEnable = ((GeoRegion) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							} else if (geometry.getType() == GeometryType.GEOREGION3D) {
								bEnable = ((GeoRegion3D) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							} else if (geometry.getType() == GeometryType.GEOLINEM) {
								bEnable = ((GeoLineM) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							} else if (geometry.getType() == GeometryType.GEOTEXT) {
								bEnable = ((GeoText) geometry).getPartCount() > 1;
								if (bEnable) {
									break;
								}
							}

							recordset.moveNext();
						} finally {
							if (geometry != null) {
								geometry.dispose();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (geometry != null) {
				geometry.dispose();
			}

			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}
		}

		return bEnable;
	}
}