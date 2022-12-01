package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.UrlOpener;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;


/**
 * Attempts to open a URL in the default browser.
 * 
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 Dec 2012 Updated to use the Java 1.6 Desktop integration
  */
@SuppressWarnings("serial")
public class WebMA extends FrameMenuAction {
    private String url;

    /**
     * Creates a new WebMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     * @param webpageURL the URL to jump to
     */
    public WebMA(String name, ElanFrame2 frame, String webpageURL) {
        super(name, frame);
        url = webpageURL;
    }

    /**
     * Opens the web page in the default web browser of the system.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	try {
    		// run in separate thread
			UrlOpener.openUrl(url, true);
		} catch(Exception exc) {
			errorMessage(exc.getMessage());
		}
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(frame,
            (ElanLocale.getString("Message.Web.NoConnection") + ": " + message),
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

}
