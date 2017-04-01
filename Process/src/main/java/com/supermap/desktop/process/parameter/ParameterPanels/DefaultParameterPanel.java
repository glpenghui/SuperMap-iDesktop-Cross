package com.supermap.desktop.process.parameter.ParameterPanels;

import com.supermap.desktop.process.parameter.events.FieldConstraintChangedEvent;
import com.supermap.desktop.process.parameter.events.FieldConstraintChangedListener;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;

/**
 * @author XiaJT
 */
public abstract class DefaultParameterPanel implements IParameterPanel, FieldConstraintChangedListener {
	DefaultParameterPanel(IParameter parameter) {
		parameter.addFieldConstraintChangedListener(this);
	}

	@Override
	public void fieldConstraintChanged(FieldConstraintChangedEvent event) {

	}
}