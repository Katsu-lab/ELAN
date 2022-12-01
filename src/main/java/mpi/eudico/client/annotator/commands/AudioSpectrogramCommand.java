package mpi.eudico.client.annotator.commands;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.AudioSpectrogramDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Displays a dialog window containing an audio spectrogram rendering. 
 * 
 * @author Allan van Hulst
 */
public class AudioSpectrogramCommand implements UndoableCommand {
	private String name;
	private Transcription transcription;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name
	 */
	public AudioSpectrogramCommand(String name) {
		this.name = name;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public void redo() {
		
	}

	/**
	 * Not implemented.
	 */
	@Override
	public void undo() {
		
	}
 
	/**
	 * @param receiver the transcription
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (receiver instanceof Transcription) {
			transcription = (Transcription) receiver;
		}
		
		if (transcription == null) {
			return;
		}

		new AudioSpectrogramDialog (ELANCommandFactory.getRootFrame (transcription), transcription);
	}

	/**
	 * Returns the name of the command.
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

}
