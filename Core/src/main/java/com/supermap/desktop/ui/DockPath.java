package com.supermap.desktop.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by highsad on 2016/11/17.
 * 用来做浮动窗口自动构建布局和导入导出用的
 * 由于当前浮动窗口的使用方式导致实现过于复杂，难点很多，暂时使用
 * 保留临时解决方案进行浮动窗口的配置文件读取构建，以后有灵感再做
 */
public class DockPath {

	private Direction direction = Direction.LEFT;
	private double ratio = 0.5;
	private DockPath next;

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public DockPath getNext() {
		return next;
	}

	public void setNext(DockPath next) {
		this.next = next;
	}

	public boolean isLeaf() {
		return this.next == null;
	}

	//	/**
//	 * Dock 方位
//	 */
//	private List<Direction> directions = new ArrayList<>();
//
//
//	/**
//	 * 最后 Dock 位置与相邻同边界 Dockbar 所占的比例
//	 */
//	private double ratio = 0.5;
//
//	/**
//	 * 获取 DockPat 路径
//	 *
//	 * @return
//	 */
//	public Direction[] getDirections() {
//		return this.directions.toArray(new Direction[this.directions.size()]);
//	}
//
//	public double getRatio() {
//		return ratio;
//	}
//
//	public void setRatio(double ratio) {
//		this.ratio = ratio;
//	}
//
//	/**
//	 * 获取路径深度（相对于中间主视图）
//	 *
//	 * @return
//	 */
//	public int getDepth() {
//		return this.directions.size();
//	}
//
//	/**
//	 * 在最后位置添加 DockPath
//	 *
//	 * @param direction
//	 */
//	public void addDirection(Direction direction) {
//		this.directions.add(direction);
//	}
//
//	/**
//	 * 移除从 index 位置往后的所有 DockPath
//	 *
//	 * @param index
//	 */
//	public void removeDirection(int index) {
//		if (index < this.directions.size()) {
//			for (int i = this.directions.size() - 1; i >= index; i--) {
//				this.directions.remove(i);
//			}
//		}
//	}
//
//	/**
//	 * 清空路径
//	 */
//	public void clear() {
//		this.directions.clear();
//		this.ratio = 0.5;
//	}
//
//	/**
//	 * 使用指定的 dockPath 初始化
//	 *
//	 * @param dockPath
//	 */
//	public void init(DockPath dockPath) {
//		if (dockPath != null) {
//			this.ratio = dockPath.getRatio();
//			this.directions.clear();
//			Direction[] directions = dockPath.getDirections();
//			for (int i = 0; i < directions.length; i++) {
//				this.directions.add(directions[i]);
//			}
//		}
//	}
}
