package common.entity;

import common.enums.PetSpecies;
import common.enums.PetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class Pet {

    /**
     * 宠物 UUID
     */
    private String id;

    /**
     * 宠物名称
     */
    private String name;

    /**
     * 物种
     */
    private PetSpecies specie;

    /**
     * 年龄
     */
    private int age;

    /**
     * 描述
     */
    private String description;

    /**
     * 领养状态
     */
    private PetStatus adoptionStatus;

    /**
     * 创建时间戳
     */
    private long createTime;

    /**
     * 构造函数（id 和 createTime 由系统生成）
     */
    public Pet(String name, PetSpecies specie, int age, String description, PetStatus adoptionStatus) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.specie = specie;
        this.age = age;
        this.description = description;
        this.adoptionStatus = adoptionStatus != null ? adoptionStatus : PetStatus.AVAILABLE;
        this.createTime = System.currentTimeMillis();
    }

    /**
     * 从数据库记录创建 Pet 对象
     *
     * @warn 仅供 DAO 层使用
     */
    public static Pet createFromDatabase(String id, String name, PetSpecies specie, int age,
                                         String description, PetStatus adoptionStatus, long createTime) {
        return Pet.builder()
                .id(id)
                .name(name)
                .specie(specie)
                .age(age)
                .description(description)
                .adoptionStatus(adoptionStatus)
                .createTime(createTime)
                .build();
    }

    /**
     *
     * @return 格式化字符串
     */
    public String getDisplayString() {
        return name + " (" + specie + ")\n" +
                "\tID: " + id + "\n" +
                "\t年龄: " + age + "岁\n" +
                "\t状态: " + adoptionStatus + "\n" +
                "\t描述: " + (description != null ? description : "无");
    }
}
