package common.entity;


import common.utils.StringFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Announcement {
    private final String id;
    private final String senderId;
    private String title;
    private String content;
    private final long createTime;

    public Announcement(String senderId, String title, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.senderId = senderId;
        this.title = title;
        this.content = content;
        this.createTime = System.currentTimeMillis();
    }

    public static Announcement createFromDatabase(String id, String senderId, String title, String content, long createTime) {
        return Announcement.builder()
                .id(id)
                .senderId(senderId)
                .title(title)
                .content(content)
                .createTime(createTime)
                .build();
    }

//    public String toString() {
//        return String.format("{id=%s,title=%s,content=%s,createTime=%s}",
//                id, title, content, createTime);
//    }

    /**
     * toString 显示转义后的内容（用于日志和调试）
     */
    @Override
    public String toString() {
        return String.format("Announcement{id=%s,title=%s,content=%s,createTime=%s}",
                id, title, StringFormatter.escapeAndTruncate(content, 100), createTime);
    }
}
