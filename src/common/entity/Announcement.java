package common.entity;


import common.manager.UserManager;
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

    /**
     * toString 显示转义后的内容（用于日志和调试）
     */
    @Override
    public String toString() {
        return String.format("Announcement{id=%s,title=%s,content=%s,createTime=%s}",
                id, title, StringFormatter.escapeAndTruncate(content, 100), createTime);
    }

    /**
     *
     * @return 转义后的内容，有显示长度限制
     */
    public String getShortContent() {
        return StringFormatter.escapeAndTruncate(content, 100);
    }

    private String getSenderName() {
        if (senderId == null || senderId.isEmpty()) {
            return "未知";
        }
        User sender = UserManager.getInstance().getUserById(senderId);
        return sender != null ? sender.getUsername() : "未知用户";
    }

    /**
     *
     * @return 格式化字符串
     */
    public String getDisplayString() {
        return String.format("%s\n\tID：%s\n\t发布者：{%s\tID：%s}\n\t发布时间：%s\n\t内容：\n%s\n",
                title, id, getSenderName(), senderId, StringFormatter.timeStampToString(createTime), getShortContent()
        );
    }

    /**
     *
     * @return 格式化字符串
     */
    public String getShortDisplayString() {
        return String.format("%s\n\tID：%s\n\t发布者：{%s\tID：%s}\n\t发布时间：%s\n\t内容：%s\n",
                title, id, getSenderName(), senderId, StringFormatter.timeStampToString(createTime), getContent()
        );
    }
}
