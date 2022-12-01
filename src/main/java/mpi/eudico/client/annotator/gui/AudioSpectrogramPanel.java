package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.server.corpora.clom.Transcription;
import nl.mpi.media.spectrogram.SpectrogramSettings;

/**
 * The actual JPanel for rendering the audio spectrogram
 *
 * @author Allan van Hulst
 * @version 1.0
 */
public class AudioSpectrogramPanel extends JPanel {
	
	
    /**
	 * 
	 */
	public AudioSpectrogramPanel() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public AudioSpectrogramPanel(int[] audioSamples, double[][] freqData, 
			SpectrogramSettings settings, Selection selection,
			long intervalStart, long intervalEnd) {
		super(null);// null layout
		// store/set local references
		// wait for setVisible and/or setSize before generating the image
	}

	/**
	 * @param isDoubleBuffered
	 */
	public AudioSpectrogramPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public AudioSpectrogramPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 */
	public AudioSpectrogramPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	/**
     * Re-draw the spectrogram.
     * 
     * @param g The graphics context
     */
    public void paintComponent (Graphics g) {
    	super.paintComponent(g);
    	
    	g.drawString("Please specify an interval in the text fields above", 20, 20);
    	
    	// paint background
    	// paint image, creating a border around it
    }
}
