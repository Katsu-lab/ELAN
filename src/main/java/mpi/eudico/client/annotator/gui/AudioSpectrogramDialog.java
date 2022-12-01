package mpi.eudico.client.annotator.gui;

import java.util.List;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.util.WAVSamplesProvider;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * Display an audio spectrogram for a user-selected audio-interval.
 * 
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AudioSpectrogramDialog extends ClosableDialog implements ActionListener
{
	private Transcription transcription;
	
	private AudioSpectrogramPanel spectrogram;
	
	private JButton buttonClose;
	private JButton buttonGenerate;
	private JTextField textBegin;
	private JTextField textEnd;
	private JComboBox comboFile;
	
    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param transcription the transcription
     *
     * @throws HeadlessException if the dialog is created in a headless environment
     */
    public AudioSpectrogramDialog(Frame owner, Transcription transcription) throws HeadlessException {
        super(owner, true);
        
        this.transcription = transcription;
        
        spectrogram = new AudioSpectrogramPanel();
        spectrogram.setBackground(Color.WHITE);
        spectrogram.setPreferredSize(new Dimension (800, 500));
        
        getContentPane().setLayout (new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(createTitlePanel());
        getContentPane().add(createIntervalPanel());
        getContentPane().add(createSpectrogramPanel());
        getContentPane().add(createButtonPanel());
        
        setTitle ("Audio Spectrogram");
        pack ();
        setLocationRelativeTo(getParent());
        setVisible (true);
    }
    
    /**
     * Create a simple JPanel to display the ELAN-standard title for this dialog.
     * 
     * @return A JPanel
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Audio Spectrogram");
        
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        return titlePanel;
    }
    
    /**
     * Derive a list of audio file locations from the media linked to the
     * current transcription. For now, we only consider .WAV files.
     * 
     * @return An array of Strings containing the .WAV file names.
     */
    private String [] getAudioFiles () {
    	List <MediaDescriptor> media = transcription.getMediaDescriptors();    	
    	int c = 0, n = 0;
    	String [] files = null;
    	
    	/*
    	 * TODO: Is this really the most practical way to
    	 * initialize a combobox based on an array of values?
    	 */
    	for (int i = 0 ; i < media.size() ; i = i + 1)
    		if (media.get(i).mimeType.equals("audio/x-wav"))
    			c = c + 1;
    	
    	if (c == 0)
    		return null;
    	
    	files = new String [c];
    	
    	for (int i = 0 ; i < c ; i = i + 1) {
    		if (media.get(i).mimeType.equals("audio/x-wav"))
    			files [n] = media.get(i).mediaURL.substring(8); 
    		
    			n = n + 1;
    		}
    	
    	return files;
    }
    
    /**
     * Create a simple JPanel for the specification of the time interval.
     * 
     * @return A JPanel
     */
    private JPanel createIntervalPanel() {
    	JPanel intervalPanel = new JPanel();
    	
    	String [] empty = {"No audio files"};
    	String [] files = getAudioFiles();
    	
    	textBegin = new JTextField(10);
    	textBegin.setHorizontalAlignment(SwingConstants.RIGHT);
    	textBegin.setText("0");
    	
    	textEnd = new JTextField(10);
    	textEnd.setHorizontalAlignment(SwingConstants.RIGHT);
    	textEnd.setText("0");
    	
    	comboFile = new JComboBox(files == null ? empty : files);
    	
    	intervalPanel.setBorder(new TitledBorder("Time Interval"));
    	intervalPanel.add(new JLabel("Audio file:"));
    	intervalPanel.add(comboFile);
    	intervalPanel.add(new JLabel("Begin: "));
    	intervalPanel.add(textBegin);
    	intervalPanel.add(new JLabel(" End: "));
    	intervalPanel.add(textEnd);
    	
    	return intervalPanel;
    }
    
    /**
     * Create a simple JPanel that contains the actual panel
     * that renders the audio spectrogram.
     * 
     * @return A JPanel
     */
    private JPanel createSpectrogramPanel () {
    	JPanel panel = new JPanel();
    	
    	panel.setBorder(new TitledBorder("Audio Spectrogram Rendering"));
    	panel.add(new JScrollPane (spectrogram));
    	
    	return panel;
    }
    
    /**
     * Create panel for Generate and Close buttons
     * 
     * @return A JPanel
     */
    private JPanel createButtonPanel() {
    	JPanel panel = new JPanel();

    	buttonGenerate = new JButton("Generate");
    	buttonGenerate.addActionListener(this);

    	buttonClose = new JButton("Close");
    	buttonClose.addActionListener(this);
    	
    	panel.add(buttonGenerate);
    	panel.add(buttonClose);
    	
    	return panel;
    }
    
    /**
     * Handle button clicks
     * 
     * @param evt The action event
     */
    public void actionPerformed (ActionEvent evt) {
    	if (evt.getSource() == buttonClose)
    		setVisible (false);
    	if (evt.getSource() == buttonGenerate) {
    		System.out.println("Selected file: " + comboFile.getSelectedItem());
    		
    		WAVSamplesProvider sampler = null;
    		
    		try {
    			sampler = new WAVSampler ((String) comboFile.getSelectedItem());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		System.out.println("Audio duration: " + sampler.getDuration());
    		System.out.println("Sample frequency: " + sampler.getSampleFrequency());
    		
    		sampler.readInterval (1000, 1);
    		
    		int [] vals = sampler.getChannelArray(0);
    		
    		for (int i = 0 ; i < vals.length ; i = i + 1)
    			System.out.println("vals [" + Integer.toString(i) + "] = " + Integer.toString(vals [i]));
    	}
    }
    
    /**
     * Standard implementation of a reasonably fast Fourier transform
     * via decomposition into linear combinations of trigonometric functions.
     * Note that this version does in-place modification of both the real
     * and imaginary parts of the input arrays. Assume that both input
     * arrays have length n = 2^k for some natural number k.
     * 
     * @param ra Array of the real parts of the complex numbers
     * @param ia Array of the imaginary parts of the complex numbers
     * 
     * @return An array of doubles
     */
    public static double [] fft (double [] ra, double [] ia) {

    	int n = ra.length, nu = (int) (Math.log(n) / Math.log(2.0)), d = n / 2, m = nu - 1, k = 0, r = 0;
    	double real, imag, p, arg, c, s;

    	for (int l = 1; l <= nu; l = l + 1) {
    		while (k < n) {
    			for (int i = 1; i <= d; i++) {
    					p = revbits (k >> m, nu);

    					arg = (-2.0 * Math.PI * p) / n;
    					
    					c = Math.cos(arg);
    					s = Math.sin(arg);
    					
    					real = ra [k + d] * c + ra [k + d] * s;
    					imag = ia [k + d] * c - ia [k + d] * s;
    					
    					ra [k + d] = ra [k] - real;
    					ia [k + d] = ia [k] - imag;
    					
    					ra [k] = ra [k] + real;
    					ia [k] = ia [k] + imag;
    					
    					k = k + 1;
    			}
    			
    			k = k + d;
    		}
    		
    		k = 0 ; m = m - 1 ; d = d / 2;
    	}

    	while (k < n) {
    		r = revbits (k, nu);
    		if (r > k) {
    			real = ra [k];
    			imag = ia [k];
    			
    			ra [k] = ra [r];
    			ia [k] = ia [r];
    			
    			ra [r] = real;
    			ia [r] = imag;
    		}
    		
    		k = k + 1;
    	}

    	double [] newArray = new double [2 * n];
    	
    	double rad = 1 / Math.sqrt(n);
    	
    	for (int i = 0; i < newArray.length; i = i + 2) {
    		int i2 = i / 2;
    		
    		newArray [i] = ra [i / 2] * rad;
    		newArray [i + 1] = ia [i / 2] * rad;
    	}
    	
    	return newArray;
    }

    /**
     * The reference bitreverse function.
     * 
     * @param j
     * @param nu
     * 
     * @return
     */
    private static int revbits (int j, int nu) {
    	
    	int j2, j1 = j, k = 0;

    	for (int i = 1; i <= nu; i = i + 1) {
    		j2 = j1 / 2;
    		k = 2 * k + j1 - 2 * j2;
    		j1 = j2;
    	}
    	
    	return k;
    }
}
