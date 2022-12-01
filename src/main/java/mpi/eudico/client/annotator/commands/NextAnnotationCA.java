package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * An action to make the next annotation active.
 */
@SuppressWarnings("serial")
public class NextAnnotationCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new NextAnnotationCA instance
     *
     * @param theVM the viewer manager
     */
    public NextAnnotationCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.NEXT_ANNOTATION);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToNextAnnotation.gif"));
        putValue(SMALL_ICON, icon);

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code ActiveAnnotationCommand}.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
    }

    /**
     * @return the viewer manager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     *
     * @return an array containing only the new active annotation or containing
     *  {@code null}
     */
    @Override
	protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationAfter(currentActiveAnnot);

            if (newActiveAnnot == null) {
                newActiveAnnot = currentActiveAnnot;
            }
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = ((TierImpl) activeTier).getAnnotationAfter(vm.getMasterMediaPlayer()
                                                                              .getMediaTime());
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }
}
