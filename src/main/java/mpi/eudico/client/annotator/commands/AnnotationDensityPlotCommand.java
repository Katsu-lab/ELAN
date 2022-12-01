package mpi.eudico.client.annotator.commands;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AnnotationDensityPlotDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command to display a dialog to display annotations by means of a density plot 
 * 
 * @author Allan van Hulst
 *
 */
public class AnnotationDensityPlotCommand implements Command {
	private String name;
	private Transcription transcription;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name
	 */
	public AnnotationDensityPlotCommand(String name) {
		this.name = name;
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
		
		if (transcription == null)
			return;

		new AnnotationDensityPlotDialog (ELANCommandFactory.getRootFrame (transcription), 
				transcription, (Selection) arguments [1]);
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
