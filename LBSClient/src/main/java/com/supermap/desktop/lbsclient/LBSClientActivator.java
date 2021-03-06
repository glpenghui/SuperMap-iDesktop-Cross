package com.supermap.desktop.lbsclient;

import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormLBSControl;
import com.supermap.desktop.Interface.IFormManager;
import com.supermap.desktop.dialog.FormLBSControl;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.event.NewWindowEvent;
import com.supermap.desktop.event.NewWindowListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.swing.*;
import java.awt.*;

public class LBSClientActivator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		LBSClientActivator.context = bundleContext;
		
		System.out.println("Hello SuperMap === LBSClient!!");
		Application.getActiveApplication().getPluginManager().addPlugin("SuperMap.Desktop.LBSClient", bundleContext.getBundle());
		CommonToolkit.FormWrap.addNewWindowListener(new NewWindowListener() {
			@Override
			public void newWindow(NewWindowEvent evt) {
				newWindowEvent(evt);
			}
		});
	}
	private void newWindowEvent(NewWindowEvent evt) {
		try {
			WindowType type = evt.getNewWindowType();

			if (type == WindowType.LBSCONTROL) {
				IFormLBSControl formMap = showLBSControl(evt.getNewWindowName());
				formMap .setText("FormHDFSManager");
				evt.setNewWindow(formMap);
			}

		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public IFormLBSControl showLBSControl(String name) {
		String nameTemp = name;
		JFrame mainFrame = (JFrame) Application.getActiveApplication().getMainFrame();
		mainFrame.setCursor(Cursor.WAIT_CURSOR);
		IFormLBSControl formMap = null;
		try {
			IForm form = CommonToolkit.FormWrap.getForm(nameTemp, WindowType.LBSCONTROL);
			IFormManager formManager = Application.getActiveApplication().getMainFrame().getFormManager();

			if (form == null) {
				formMap = new FormLBSControl(nameTemp);
				formManager.showChildForm(formMap);
			} else {
				formMap = (IFormLBSControl) form;
				formManager.setActiveForm(formMap);
			}

		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		mainFrame.setCursor(Cursor.DEFAULT_CURSOR);

		return formMap;
	}
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		LBSClientActivator.context = null;
		System.out.println("Goodbye SuperMap === LBSClient!!");
	}

}
