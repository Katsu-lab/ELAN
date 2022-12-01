package mpi.eudico.server.corpora.clomimpl.html;

/**
 * Data structure for the export TimeAlignedInterlinear. Contains a tier and their preferences.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
public class TAITierSetting {

    private String tierName;
    private boolean underlined;
    private boolean bold;
    private boolean italic;
    private boolean reference = false;

    public TAITierSetting (String tierName, boolean underlined, boolean bold, boolean italic) {
        this.tierName = tierName;
        this.underlined = underlined;
        this.italic = italic;
        this.bold = bold;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

}
