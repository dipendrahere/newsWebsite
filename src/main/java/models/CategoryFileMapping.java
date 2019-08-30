package models;

public class CategoryFileMapping {
    private String filepath;
    private int key;

    public CategoryFileMapping(String filepath, int key) {
        this.filepath = filepath;
        this.key = key;
    }

    public String getFilepath() {
        return filepath;
    }

    public int getKey() {
        return key;
    }
}
