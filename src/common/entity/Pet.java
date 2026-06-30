package common.entity;

public class Pet {
    public enum Specie {
        Cat,
        Dog,
        Other
    }

    public enum AdoptionStatus {
        Waiting,
        Adopted,
        UnAdoptable
    }

    public enum Gender {

    }

    private final String id;
    private String name;
    private Specie specie;
    private int age;
    // 健康状态描述
    private String healthStatus;
    private AdoptionStatus adoptionStatus;
    private String description;
    // 创建时间戳
    private final long createTime;

    public Pet(String name, Specie specie, int age, String healthStatus, AdoptionStatus adoptionStatus, String description) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.specie = specie;
        this.age = age;
        this.healthStatus = healthStatus;
        this.adoptionStatus = adoptionStatus;
        this.description = description;
        this.createTime = System.currentTimeMillis();
    }

    public Pet(String name, Specie specie, int age, String healthStatus, AdoptionStatus adoptionStatus) {
        this(name, specie, age, healthStatus, adoptionStatus, "");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Specie getSpecie() {
        return specie;
    }

    public void setSpecie(Specie specie) {
        this.specie = specie;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public AdoptionStatus getAdoptionStatus() {
        return adoptionStatus;
    }

    public void setAdoptionStatus(AdoptionStatus adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreateTime() {
        return createTime;
    }
}
