package common.manager;

import common.entity.Pet;
import common.enums.PetSpecies;
import common.enums.PetStatus;
import server.dao.PetDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 宠物管理器 - 业务逻辑层
 * 负责宠物相关的业务逻辑处理
 */
public class PetManager {

    private static final Logger log = LoggerFactory.getLogger(PetManager.class);

    private static volatile PetManager instance = null;

    // DAO 用于数据持久化
    private final PetDAO petDAO;

    private PetManager() {
        this.petDAO = new PetDAO();
    }

    /**
     * 获取单例实例（线程安全）
     */
    public static PetManager getInstance() {
        if (instance == null) {
            synchronized (PetManager.class) {
                if (instance == null) {
                    instance = new PetManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加宠物
     *
     * @param name              宠物名称
     * @param specie            物种
     * @param age               年龄
     * @param description       描述
     * @param adoptionStatus    领养状态
     * @return 宠物ID，失败返回null
     */
    public String addPet(String name, PetSpecies specie, int age, String description, PetStatus adoptionStatus) {
        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            log.warn("添加失败：宠物名称为空");
            return null;
        }

        if (specie == null) {
            log.warn("添加失败：物种为空");
            return null;
        }

        if (age < 0) {
            log.warn("添加失败：年龄不能为负数: {}", age);
            return null;
        }

        // 创建宠物对象
        Pet pet = new Pet(name.trim(), specie, age, description != null ? description : "",
                         adoptionStatus != null ? adoptionStatus : PetStatus.AVAILABLE);

        // 保存到数据库
        boolean success = petDAO.save(pet);
        if (success) {
            log.info("宠物添加成功: {} ({})", pet.getName(), pet.getId());
            return pet.getId();
        } else {
            log.error("宠物添加失败: {}", name);
            return null;
        }
    }

    /**
     * 添加宠物，默认状态为 AVAILABLE
     *
     * @param name        宠物名称
     * @param specie      物种
     * @param age         年龄
     * @param description 描述
     * @return 宠物ID，失败返回null
     */
    public String addPet(String name, PetSpecies specie, int age, String description) {
        return addPet(name, specie, age, description, PetStatus.AVAILABLE);
    }

    /**
     * 更新宠物信息
     *
     * @param id          宠物ID
     * @param name        新名称
     * @param specie      新物种
     * @param age         新年龄
     * @param description 新描述
     * @return 是否更新成功
     */
    public boolean updatePet(String id, String name, PetSpecies specie, int age, String description) {
        // 查询宠物是否存在
        Pet pet = petDAO.findById(id);
        if (pet == null) {
            log.warn("更新失败：宠物不存在: {}", id);
            return false;
        }

        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            log.warn("更新失败：宠物名称为空");
            return false;
        }

        if (specie == null) {
            log.warn("更新失败：物种为空");
            return false;
        }

        if (age < 0) {
            log.warn("更新失败：年龄不能为负数: {}", age);
            return false;
        }

        // 更新字段
        pet.setName(name.trim());
        pet.setSpecie(specie);
        pet.setAge(age);
        pet.setDescription(description != null ? description : "");

        // 保存到数据库
        boolean success = petDAO.update(pet);
        if (success) {
            log.info("宠物更新成功: {}", id);
        } else {
            log.error("宠物更新失败: {}", id);
        }
        return success;
    }

    /**
     * 删除宠物
     *
     * @param id 宠物ID
     * @return 是否删除成功
     */
    public boolean removePet(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("删除失败：ID为空");
            return false;
        }

        // 检查宠物是否存在
        Pet pet = petDAO.findById(id);
        if (pet == null) {
            log.warn("删除失败：宠物不存在: {}", id);
            return false;
        }

        // 从数据库删除
        boolean success = petDAO.delete(id);
        if (success) {
            log.info("宠物删除成功: {}", id);
        } else {
            log.error("宠物删除失败: {}", id);
        }
        return success;
    }

    /**
     * 根据ID查询宠物
     *
     * @param id 宠物ID
     * @return 宠物对象，不存在返回null
     */
    public Pet getById(String id) {
        return petDAO.findById(id);
    }

    /**
     * 查询所有宠物
     *
     * @return 宠物列表
     */
    public List<Pet> getAll() {
        return petDAO.findAll();
    }

    /**
     * 根据物种查询宠物
     *
     * @param species 物种
     * @return 宠物列表
     */
    public List<Pet> getBySpecies(PetSpecies species) {
        if (species == null) {
            log.warn("查询失败：物种为空");
            return List.of();
        }
        return petDAO.findBySpecies(species);
    }

    /**
     * 根据领养状态查询宠物
     *
     * @param status 领养状态
     * @return 宠物列表
     */
    public List<Pet> getByStatus(PetStatus status) {
        if (status == null) {
            log.warn("查询失败：状态为空");
            return List.of();
        }
        return petDAO.findByStatus(status);
    }

    /**
     * 根据名称模糊查询宠物
     *
     * @param name 名称关键词
     * @return 宠物列表
     */
    public List<Pet> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("查询失败：名称为空");
            return List.of();
        }
        return petDAO.findByName(name.trim());
    }

    /**
     * 查询可领养的宠物
     *
     * @return 宠物列表
     */
    public List<Pet> getAvailablePets() {
        return petDAO.findByStatus(PetStatus.AVAILABLE);
    }

    /**
     * 更新宠物领养状态
     *
     * @param id     宠物ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateStatus(String id, PetStatus status) {
        if (id == null || id.isEmpty()) {
            log.warn("更新状态失败：ID为空");
            return false;
        }

        if (status == null) {
            log.warn("更新状态失败：状态为空");
            return false;
        }

        // 查询宠物是否存在
        Pet pet = petDAO.findById(id);
        if (pet == null) {
            log.warn("更新状态失败：宠物不存在: {}", id);
            return false;
        }

        // 更新状态
        pet.setAdoptionStatus(status);

        // 保存到数据库
        boolean success = petDAO.update(pet);
        if (success) {
            log.info("宠物状态更新成功: {} -> {}", id, status);
        } else {
            log.error("宠物状态更新失败: {}", id);
        }
        return success;
    }

    /**
     * 标记宠物为已领养
     *
     * @param id 宠物ID
     * @return 是否更新成功
     */
    public boolean markAsAdopted(String id) {
        return updateStatus(id, PetStatus.ADOPTED);
    }

    /**
     * 标记宠物为待审核
     *
     * @param id 宠物ID
     * @return 是否更新成功
     */
    public boolean markAsPending(String id) {
        return updateStatus(id, PetStatus.PENDING);
    }

    /**
     * 标记宠物为可领养
     *
     * @param id 宠物ID
     * @return 是否更新成功
     */
    public boolean markAsAvailable(String id) {
        return updateStatus(id, PetStatus.AVAILABLE);
    }

    /**
     * 统计宠物总数
     *
     * @return 宠物数量
     */
    public int count() {
        return petDAO.count();
    }

    /**
     * 显示所有宠物
     */
    public void showAll() {
        List<Pet> pets = getAll();
        System.out.println("========== 宠物列表 ==========");
        if (pets.isEmpty()) {
            System.out.println("暂无宠物");
        } else {
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = pets.get(i);
                System.out.printf("[%d] %s (%s)\n", i + 1, pet.getName(), pet.getSpecie());
                System.out.printf("\tID: %s\n", pet.getId());
                System.out.printf("\t年龄: %d岁\n", pet.getAge());
                System.out.printf("\t状态: %s\n", pet.getAdoptionStatus());
                System.out.printf("\t描述: %s\n", pet.getDescription());
                System.out.println();
            }
        }
        System.out.println("总计: " + pets.size() + " 只");
        System.out.println("==============================");
    }

    /**
     * 显示宠物详情
     *
     * @param id 宠物ID
     */
    public void showDetail(String id) {
        Pet pet = getById(id);
        if (pet == null) {
            System.out.println("宠物不存在: " + id);
            return;
        }

        System.out.println("========== 宠物详情 ==========");
        System.out.println("ID: " + pet.getId());
        System.out.println("名称: " + pet.getName());
        System.out.println("物种: " + pet.getSpecie());
        System.out.println("年龄: " + pet.getAge() + "岁");
        System.out.println("状态: " + pet.getAdoptionStatus());
        System.out.println("描述: " + pet.getDescription());
        System.out.println("创建时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(pet.getCreateTime())));
        System.out.println("==============================");
    }

    /**
     * 显示可领养的宠物
     */
    public void showAvailablePets() {
        List<Pet> pets = getAvailablePets();
        System.out.println("========== 可领养宠物列表 ==========");
        if (pets.isEmpty()) {
            System.out.println("暂无可领养宠物");
        } else {
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = pets.get(i);
                System.out.printf("[%d] %s (%s)\n", i + 1, pet.getName(), pet.getSpecie());
                System.out.printf("\tID: %s\n", pet.getId());
                System.out.printf("\t年龄: %d岁\n", pet.getAge());
                System.out.printf("\t描述: %s\n", pet.getDescription());
                System.out.println();
            }
        }
        System.out.println("总计: " + pets.size() + " 只");
        System.out.println("==================================");
    }
}
