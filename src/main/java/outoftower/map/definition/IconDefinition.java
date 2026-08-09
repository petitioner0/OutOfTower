package outoftower.map.definition;

public final class IconDefinition {
    private final String imagePath;
    private final String outlinePath;

    public IconDefinition(String imagePath, String outlinePath) {
        this.imagePath = imagePath;
        this.outlinePath = outlinePath;
    }

    public String getImagePath() { return imagePath; }
    public String getOutlinePath() { return outlinePath; }
}
