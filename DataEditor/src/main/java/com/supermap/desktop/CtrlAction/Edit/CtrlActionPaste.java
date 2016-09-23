package com.supermap.desktop.CtrlAction.Edit;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.*;
import com.supermap.desktop.controls.utilities.MapViewUIUtilities;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.implement.SmStatusbar;
import com.supermap.desktop.ui.StatusbarManager;

import javax.swing.*;

public class CtrlActionPaste extends CtrlAction {

    public CtrlActionPaste(IBaseItem caller, IForm formClass) {
        super(caller, formClass);
    }

    @Override
    public void run() {
        try {
            IForm form = Application.getActiveApplication().getActiveForm();
            if (form != null) {
                if (form instanceof IFormMap) {
                    ((IFormMap) form).getMapControl().paste();
                    ((IFormMap) form).getMapControl().getMap().refresh();
                } else if (form instanceof IFormScene) {
                    // TODO 目前场景并不支持编辑，后续版本根据组件支持情况再行实现
                } else if (form instanceof IFormLayout) {
                    ((IFormLayout) form).getMapLayoutControl().paste();
                    ((IFormLayout) form).getMapLayoutControl().getMapLayout().refresh();
                }
                int count = MapViewUIUtilities.reverseSelection((IFormMap) form);
                SmStatusbar statusbar = ((StatusbarManager) Application.getActiveApplication().getMainFrame().getStatusbarManager()).getStatusbar("com.supermap.desktop.FormMap");
                ((JTextField) statusbar.getComponent(1)).setText(String.valueOf(count));
            }
        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        }
    }

    @Override
    public boolean enable() {
        boolean enable = false;
        try {
            IForm form = Application.getActiveApplication().getActiveForm();
            if (form != null) {
                if (form instanceof IFormMap) {
                    enable = ((IFormMap) form).getMapControl().canPaste();
                } else if (form instanceof IFormScene) {
                    // TODO 目前场景并不支持编辑，后续版本根据组件支持情况再行实现
                } else if (form instanceof IFormLayout) {
                    enable = ((IFormLayout) form).getMapLayoutControl().canPaste();
                }
            }

        } catch (Exception ex) {
            Application.getActiveApplication().getOutput().output(ex);
        }
        return enable;
    }
}
