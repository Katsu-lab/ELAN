package mpi.eudico.client.annotator.gui;

import java.util.List;
import java.util.Map;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.AnnotationDensityPlotDialog;

/**
 * A panel to render a density plot for annotation information.
 * 
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AnnotationDensityPanel extends JPanel {
	private AnnotationDensityPlotDialog parent = null;
	
    /**
     * Constructor
     * 
     * @param parent the parent dialog
     */	
    public AnnotationDensityPanel(AnnotationDensityPlotDialog parent) {
    	this.parent = parent;
    	
    	setBorder(BorderFactory.createLineBorder(Color.black));
    	setBackground(Color.GRAY);
    }

    /**
     * Display a (relatively simple) spread of the annotation distribution
     */
    protected void paintComponent (Graphics g) {
    	super.paintComponent (g);
    	
    	parent.drawPlot (g, false);
   }
}
