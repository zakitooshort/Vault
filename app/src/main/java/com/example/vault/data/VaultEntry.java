package com.example.vault.data;

public class VaultEntry {
    private String id;
    private String title;
    private String username;
    private String password;
    private String url;
    private String notes;
    private String categoryId;
    private boolean favorite;
    private long createdAt;
    private long updatedAt;

    public VaultEntry(String id, String title, String username, String password,
                      String url, String notes, String categoryId, boolean favorite,
                      long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
        this.categoryId = categoryId;
        this.favorite = favorite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getUrl() { return url; }
    public String getNotes() { return notes; }
    public String getCategoryId() { return categoryId; }
    public boolean isFavorite() { return favorite; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setUrl(String url) { this.url = url; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
