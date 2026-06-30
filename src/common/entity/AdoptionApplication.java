package common.entity;

/**
 * @brief 领养申请
 */
public class AdoptionApplication {
    public enum Status {
        // 待审核
        PENDING,
        // 已通过
        ACCEPTED,
        // 已拒绝
        REJECTED
    }

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
    private Status status;

    /**
     * 审核信息，为空表示还未审核
     */
    private Review review;

    AdoptionApplication(String applicatorId, String petId, Status status, Review review) {
        this.id = java.util.UUID.randomUUID().toString();
        this.applicatorId = applicatorId;
        this.petId = petId;
        this.status = status;
        this.review = review;
    }

    AdoptionApplication(String applicatorId, String petId) {
        this(applicatorId, petId, Status.PENDING, null);
    }

    public String getId() {
        return id;
    }

    public String getApplicatorId() {
        return applicatorId;
    }

    public void setApplicatorId(String applicatorId) {
        this.applicatorId = applicatorId;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public String toString() {
        return String.format("{id=%s,applicatorId=%s,petId=%s,status=%s,review={%s}}",
                id, applicatorId, petId, status, review);
    }

    /**
     * 审核信息
     */
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

        public String getAdminId() {
            return adminId;
        }

        public String getComment() {
            return comment;
        }

        public long getTime() {
            return time;
        }
    }
}
