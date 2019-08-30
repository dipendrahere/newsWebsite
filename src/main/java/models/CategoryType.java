package models;

public enum CategoryType {
    SPORTS(new CategoryFileMapping("/rssSports.txt", 1)),
    BUSINESS(new CategoryFileMapping("/rssBusiness.txt", 2)),
    WORLD(new CategoryFileMapping("/rssWorld.txt", 3)),
    SCITECH(new CategoryFileMapping("/rssSciTech.txt", 4));

    public final CategoryFileMapping value;
    private CategoryType(CategoryFileMapping obj) {
        this.value = obj;
    }
}
