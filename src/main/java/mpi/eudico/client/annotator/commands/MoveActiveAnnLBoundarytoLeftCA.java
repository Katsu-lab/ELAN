package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;


/**
 * A command action for moving the left boundary of the active annotation to the left.
 * 
 * @author Aarthy Somasundaram
 * @version Jan 2010
 */
@SuppressWarnings("serial")
public class MoveActiveAnnLBoundarytoLeftCA extends CommandAction {

	/**
     * Creates a new MoveActiveAnnLBoundarytoLeftCA instance
     *
     * @param theVM the viewer manager
     */
	public MoveActiveAnnLBoundarytoLeftCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.MOVE_ANNOTATION_LBOUNDARY_LEFT);		
	}

    /**
     * Creates a new {@code ModifyAnnotationTimeCommand}.
     */
    @Override
	protected void newCommand() { 
        
        if (vm.getActiveAnnotation().getAnnotation() instanceof AlignableAnnotation) {
            AlignableAnnotation aa = (AlignableAnnotation) vm.getActiveAnnotation()
                                                             .getAnnotation();

            if (aa !=null) {
            	command = ELANCommandFactory.createCommand(vm.getTranscription(),
                        ELANCommandFactory.MODIFY_ANNOTATION_TIME);
            } else {
                command = null;
            }
        } else {
            command = null;
        }
    }

    /**
     * @return the active annotation
     */
    @Override
	protected Object getReceiver() {
        return vm.getActiveAnnotation().getAnnotation();
    }

    /**
     *
     * @return an array of size 2, containing the new calculated start time and
     * the existing end time of the annotation 
     */
    @Override
	protected Object[] getArguments() { 
        Object[] args = new Object[2];
        args[0] = Long.valueOf( vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary() - (long)vm.getTimeScale().getMsPerPixel());
        args[1] = Long.valueOf(vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary());            
        
        return args;
    } 

}
