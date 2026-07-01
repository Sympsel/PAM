package server.service;

import common.entity.Pet;
import common.entity.User;
import common.enums.Permission;
import common.enums.PetSpecies;
import common.enums.PetStatus;
import common.manager.PetManager;
import common.dto.response.Result;
import common.exception.BusinessException;
import common.manager.UserManager;

import java.util.List;

/**
 * 宠物服务类 - 服务层
 * 提供宠物相关的业务服务，封装 Manager 层
 */
public class PetService {

    private final PetManager petManager;

    public PetService() {
        this.petManager = PetManager.getInstance();
    }

    /**
     * 添加宠物（管理员）
     *
     * @param adminId     管理员ID
     * @param name        宠物名称
     * @param specie      物种
     * @param age         年龄
     * @param description 描述
     * @param status      领养状态
     * @return 操作结果
     */
    public Result<String> addPet(String adminId, String name, PetSpecies specie,
                                 int age, String description, PetStatus status) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层添加宠物
            String petId = petManager.addPet(name, specie, age, description, status);

            if (petId != null) {
                return Result.success(petId);
            } else {
                return Result.error("宠物添加失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 添加宠物（简化版，默认状态为 AVAILABLE）
     */
    public Result<String> addPet(String adminId, String name, PetSpecies specie,
                                 int age, String description) {
        return addPet(adminId, name, specie, age, description, PetStatus.AVAILABLE);
    }

    /**
     * 更新宠物信息（管理员）
     *
     * @param adminId     管理员ID
     * @param petId       宠物ID
     * @param name        新名称
     * @param specie      新物种
     * @param age         新年龄
     * @param description 新描述
     * @return 操作结果
     */
    public Result<String> updatePet(String adminId, String petId, String name,
                                    PetSpecies specie, int age, String description) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层更新宠物
            boolean success = petManager.updatePet(petId, name, specie, age, description);

            if (success) {
                return Result.success("宠物信息更新成功");
            } else {
                return Result.error("宠物信息更新失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除宠物（管理员）
     *
     * @param adminId 管理员ID
     * @param petId   宠物ID
     * @return 操作结果
     */
    public Result<String> deletePet(String adminId, String petId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层删除宠物
            boolean success = petManager.removePet(petId);

            if (success) {
                return Result.success("宠物删除成功");
            } else {
                return Result.error("宠物删除失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 查询所有宠物
     *
     * @return 宠物列表
     */
    public Result<List<Pet>> getAllPets() {
        try {
            List<Pet> pets = petManager.getAll();
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    /**
     * 根据 ID 查询宠物详情
     *
     * @param petId 宠物ID
     * @return 宠物对象
     */
    public Result<Pet> getPetById(String petId) {
        try {
            Pet pet = petManager.getById(petId);
            if (pet == null) {
                return Result.error("宠物不存在");
            }
            return Result.success(pet);
        } catch (Exception e) {
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    /**
     * 根据物种查询宠物
     *
     * @param species 物种
     * @return 宠物列表
     */
    public Result<List<Pet>> getPetsBySpecies(PetSpecies species) {
        try {
            List<Pet> pets = petManager.getBySpecies(species);
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    /**
     * 根据领养状态查询宠物
     *
     * @param status 领养状态
     * @return 宠物列表
     */
    public Result<List<Pet>> getPetsByStatus(PetStatus status) {
        try {
            List<Pet> pets = petManager.getByStatus(status);
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    /**
     * 搜索可领养的宠物
     *
     * @return 宠物列表
     */
    public Result<List<Pet>> getAvailablePets() {
        try {
            List<Pet> pets = petManager.getAvailablePets();
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    /**
     * 根据名称搜索宠物
     *
     * @param name 名称关键词
     * @return 宠物列表
     */
    public Result<List<Pet>> searchPetsByName(String name) {
        try {
            List<Pet> pets = petManager.searchByName(name);
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("搜索宠物失败: " + e.getMessage());
        }
    }

    /**
     * 更新宠物领养状态（管理员）
     *
     * @param adminId 管理员ID
     * @param petId   宠物ID
     * @param status  新状态
     * @return 操作结果
     */
    public Result<String> updatePetStatus(String adminId, String petId, PetStatus status) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层更新状态
            boolean success = petManager.updateStatus(petId, status);

            if (success) {
                return Result.success("宠物状态更新成功");
            } else {
                return Result.error("宠物状态更新失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 标记宠物为已领养（管理员）
     */
    public Result<String> markPetAsAdopted(String adminId, String petId) {
        return updatePetStatus(adminId, petId, PetStatus.ADOPTED);
    }

    /**
     * 标记宠物为待审核（管理员）
     */
    public Result<String> markPetAsPending(String adminId, String petId) {
        return updatePetStatus(adminId, petId, PetStatus.PENDING);
    }

    /**
     * 标记宠物为可领养（管理员）
     */
    public Result<String> markPetAsAvailable(String adminId, String petId) {
        return updatePetStatus(adminId, petId, PetStatus.AVAILABLE);
    }

    /**
     * 统计宠物数量
     *
     * @return 宠物总数
     */
    public Result<Integer> getPetCount() {
        try {
            int count = petManager.count();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("统计宠物失败: " + e.getMessage());
        }
    }

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(String userId) throws BusinessException {
        User user = UserManager.getInstance().getUserById(userId);
        if (user == null || user.getPermission() != Permission.ADMIN) {
            throw new BusinessException(403, "无权限执行此操作");
        }
    }
}
