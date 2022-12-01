package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.multiplefilesedit.create.CreateTranscriptionsDialog;

/**
 * A menu action to create a dialog for configuring the process of creation of 
 * multiple EAF files for collections of media files.
 */
@SuppressWarnings("serial")
public class MultiEAFCreationMA extends FrameMenuAction {

	public MultiEAFCreationMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new CreateTranscriptionsDialog(frame, true).setVisible(true);		
	}
}
