// Added by Katsuya Fukuoka 2022/12/20
package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;

@SuppressWarnings("serial")
public class DataEditorMA extends FrameMenuAction {

    public DataEditorMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        try {
            Process process = Runtime.getRuntime().exec("/Users/FukuokaKatsuya/Documents/Systems/ELAN/test.sh");
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}