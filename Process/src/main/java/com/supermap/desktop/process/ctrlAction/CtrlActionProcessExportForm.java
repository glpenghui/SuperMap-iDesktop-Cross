package com.supermap.desktop.process.ctrlAction;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IWorkFlow;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.process.FormProcess;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.SmFileChoose;
import com.supermap.desktop.utilities.StringUtilities;
import com.supermap.desktop.utilities.XmlUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.File;

/**
 * @author XiaJT
 */
public class CtrlActionProcessExportForm extends CtrlAction {
	public CtrlActionProcessExportForm(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		IWorkFlow workflow;
		workflow = ((FormProcess) Application.getActiveApplication().getActiveForm()).getWorkFlow();
		if (workflow == null) {
			return;
		}
		String moduleName = "CtrlActionProcessExport";
		if (!SmFileChoose.isModuleExist(moduleName)) {
			SmFileChoose.addNewNode(SmFileChoose.createFileFilter(ProcessProperties.getString("String_ProcessFile"), "xml"),
					CommonProperties.getString("String_DefaultFilePath"), ProcessProperties.getString("String_ImportWorkFLowFile"),
					moduleName, "SaveOne");
		}
		SmFileChoose fileChoose = new SmFileChoose(moduleName);
		fileChoose.setSelectedFile(new File("ProcessTemplate.xml"));
		if (fileChoose.showDefaultDialog() == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChoose.getFilePath();
			if (!StringUtilities.isNullOrEmpty(filePath)) {
				Document emptyDocument = XmlUtilities.getEmptyDocument();
				Element workFlowNode = emptyDocument.createElement("WorkFlow");
				workFlowNode.setAttribute("name", workflow.getName());
				workFlowNode.setAttribute("value", workflow.getMatrixXml());
				emptyDocument.appendChild(workFlowNode);
				XmlUtilities.saveXml(filePath, emptyDocument, "UTF-8");
			}
		}
	}

	@Override
	public boolean enable() {
		return true;
	}
}