package common.entity;

public class Announcement {
    private final String id;
    private String title;
    private String content;
    private final long createTime;

    public Announcement(String title, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String toString() {
        return String.format("{id=%s,title=%s,content=%s,createTime=%s}",
                id, title, content, createTime);
    }
}
