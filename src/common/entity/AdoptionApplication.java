package common.entity;

import common.enums.ApplicationStatus;
import lombok.*;

/**
 * @brief 领养申请
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdoptionApplication {
    /**
     * 领养申请 uuid
     */
    private final String id;
    /**
     * 申请者 uuid
     */
    private String applicatorId;
    /**
     * 申请领养的宠物 uuid
     */
    private String petId;
    /**
     * 申请状态
     */
    private ApplicationStatus status;

    /**
     * 审核信息，为空表示还未审核
     */
    private Review review;

    AdoptionApplication(String applicatorId, String petId, ApplicationStatus status, Review review) {
        this.id = java.util.UUID.randomUUID().toString();
        this.applicatorId = applicatorId;
        this.petId = petId;
        this.status = status;
        this.review = review;
    }

    public AdoptionApplication(String applicatorId, String petId) {
        this(applicatorId, petId, ApplicationStatus.PENDING, null);
    }

    public static AdoptionApplication createFromDatabase(String id, String applicatorId, String petId, ApplicationStatus status, Review review) {
        return AdoptionApplication.builder()
                .id(id)
                .applicatorId(applicatorId)
                .petId(petId)
                .status(status)
                .review(review)
                .build();
    }


    public String toString() {
        return String.format("{id=%s,applicatorId=%s,petId=%s,status=%s,review={%s}}",
                id, applicatorId, petId, status, review);
    }

    /**
     * 审核信息
     */
    @Data
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Review {
        /**
         * 审核员 id
         */
        private final String adminId;
        /**
         * 审核意见
         */
        private final String comment;
        /**
         * 审核时间戳
         */
        private final long time;

        public Review(String adminId, String comment) {
            this.adminId = adminId;
            this.comment = comment;
            this.time = System.currentTimeMillis();
        }

        public static Review createFromDatabase(String adminId, String comment, long time) {
            return Review.builder()
                    .adminId(adminId)
                    .comment(comment)
                    .time(time)
                    .build();
        }

    }
}
