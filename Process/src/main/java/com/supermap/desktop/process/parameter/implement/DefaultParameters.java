package com.supermap.desktop.process.parameter.implement;

import com.supermap.desktop.process.parameter.ParameterPanels.EmptyParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.util.ParameterUtil;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * @author XiaJT
 */
public class DefaultParameters implements IParameters {
	private IParameter[] parameters;
	private JPanel panel;
	private ArrayList<ParameterClassBundleNode> packages = new ArrayList<>();
	private EmptyParameterPanel parameterPanel = new EmptyParameterPanel();

	public DefaultParameters() {
		packages.add(new ParameterClassBundleNode("com.supermap.desktop.process.parameter.ParameterPanels", "SuperMap.Desktop.Process"));
	}

	@Override
	public void setParameters(IParameter... iParameters) {
		if (this.parameters != null && this.parameters.length > 0) {
			for (IParameter parameter : parameters) {
				parameter.dispose();
				parameter.setParameters(null);
			}
		}
		if (panel != null) {
			panel.removeAll();
		}
		panel = null;
		this.parameters = iParameters;
		if (this.parameters != null && this.parameters.length > 0) {
			for (IParameter parameter : parameters) {
				parameter.setParameters(this);
			}
		}
	}


	@Override
	public IParameter[] getParameters() {
		return parameters;
	}

	@Override
	public IParameter getParameter(String key) {
		for (IParameter parameter : parameters) {
			if (parameter.getType().equals(key)) {
				return parameter;
			}
		}
		return null;
	}

	@Override
	public IParameter getParameter(int index) {
		if (index >= 0 && index < parameters.length) {
			return parameters[index];
		}
		return null;
	}

	@Override
	public int size() {
		return parameters.length;
	}

	@Override
	public IParameterPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			for (int i = 0; i < parameters.length; i++) {
				panel.add((JPanel) parameters[i].getParameterPanel().getPanel(), new GridBagConstraintsHelper(0, i, 1, 1).setWeight(1, 0).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 10, 0, 10));
			}
			panel.add(new JPanel(), new GridBagConstraintsHelper(0, parameters.length, 1, 1).setWeight(1, 1).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.BOTH));
		}
		parameterPanel.setPanel(panel);
		return parameterPanel;
	}

	@Override
	public IParameterPanel createPanel(IParameter parameter) {
		Class clazz = ParameterUtil.getParameterPanel(parameter.getType(), packages);
		if (clazz != null) {
			try {
				Constructor constructor = clazz.getConstructor(IParameter.class);
				return (IParameterPanel) constructor.newInstance(parameter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
