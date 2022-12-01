package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Opens a dialog window to display an audio spectrogram
 * 
 * @author Allan van Hulst
 */
public class AudioSpectrogramCA extends CommandAction {

	/**
	 * Constructor.
	 * 
	 * @param theVM the viewer manager
	 */
	public AudioSpectrogramCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.AUDIO_SPECTROGRAM);
	}

	/**
	 * Creates a new command.
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.AUDIO_SPECTROGRAM);
	}

	/**
	 * Returns the transcription.
	 * 
	 * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
	 */
	@Override
	protected Object getReceiver() {
		return vm.getTranscription();
	}

	
}
