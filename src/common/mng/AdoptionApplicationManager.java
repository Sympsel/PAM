package common.mng;

import common.entity.AdoptionApplication;

import java.util.LinkedList;
import java.util.Queue;

public class AdoptionApplicationManager {
    /**
     * 申请队列
     */
    private final Queue<AdoptionApplication> applyingQueue;
    /**
     * 已通过队列
     */
    private final Queue<AdoptionApplication> acceptedQueue;
    /**
     * 已拒绝队列
     */
    private final Queue<AdoptionApplication> rejectedQueue;

    private AdoptionApplicationManager() {
        applyingQueue = new LinkedList<>();
        acceptedQueue = new LinkedList<>();
        rejectedQueue = new LinkedList<>();
    }

    private static volatile AdoptionApplicationManager instance = null;

    public static AdoptionApplicationManager getInstance() {
        if (instance == null) {
            instance = new AdoptionApplicationManager();
        }
        return instance;
    }

    public boolean hasNext() {
        return !applyingQueue.isEmpty();
    }

    private void add(Queue<AdoptionApplication> queue, AdoptionApplication adoptionApplication) {
        queue.add(adoptionApplication);
    }

    public void addToApplyingQueue(AdoptionApplication adoptionApplication) {
        this.add(applyingQueue, adoptionApplication);
    }

    public void addToAcceptedQueue(AdoptionApplication adoptionApplication) {
        this.add(acceptedQueue, adoptionApplication);
    }

    public void addToRejectedQueue(AdoptionApplication adoptionApplication) {
        this.add(rejectedQueue, adoptionApplication);
    }

    public boolean passOne(String adminId, String comment) {
        return handleOne(acceptedQueue, adminId, comment);
    }

    public boolean rejectOne(String adminId, String comment) {
        return handleOne(rejectedQueue, adminId, comment);
    }

    public boolean handleLater() {
        if (applyingQueue.isEmpty()) {
            return false;
        }
        applyingQueue.add(applyingQueue.poll());
        return true;
    }

    private boolean handleOne(Queue<AdoptionApplication> resultQueue, String adminId, String comment) {
        if (applyingQueue.isEmpty()) {
            return false;
        }
        AdoptionApplication peek = applyingQueue.peek();
        peek.setReview(new AdoptionApplication.Review(adminId, comment));
        add(resultQueue, applyingQueue.poll());
        return true;

    }
}
