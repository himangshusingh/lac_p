package org.eggzampl;

public class UserData {
    private String path;
    private String creationTime;
    private String owner;
    private String snippet;

    public UserData(String path, String creationTime, String owner, String snippet) {
        this.path = path;
        this.creationTime = creationTime;
        this.owner = owner;
        this.snippet = snippet;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
