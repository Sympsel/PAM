package server.dao;

import common.entity.Pet;
import common.enums.PetSpecies;
import common.enums.PetStatus;

import java.sql.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static common.utils.DatabaseConnect.getConnection;

/**
 * 宠物数据访问对象
 */
public class PetDAO implements BaseDAO<Pet, String> {

    private static final Logger logger = LoggerFactory.getLogger(PetDAO.class);

    /**
     * 保存宠物
     */
    @Override
    public boolean save(Pet pet) {
        if (pet == null) {
            logger.warn("尝试保存空宠物");
            return false;
        }

        String sql = "insert into pets (id, name, specie, age, description, adoption_status, create_time) " +
                "values (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pet.getId());
            pstmt.setString(2, pet.getName());
            pstmt.setString(3, pet.getSpecie().name().toLowerCase());
            pstmt.setInt(4, pet.getAge());
            pstmt.setString(5, pet.getDescription());
            pstmt.setString(6, pet.getAdoptionStatus().name().toLowerCase());
            pstmt.setLong(7, pet.getCreateTime());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("宠物保存成功: {}", pet.getName());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("宠物已存在: {}", pet.getId());
        } catch (SQLException e) {
            logger.warn("保存宠物失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 根据 ID 查询宠物
     */
    @Override
    public Pet findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String sql = "select * from pets where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getPetByResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.warn("查询宠物失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 查询所有宠物
     */
    @Override
    public List<Pet> findAll() {
        List<Pet> pets = new ArrayList<>();
        String sql = "select * from pets order by create_time desc";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pets.add(getPetByResultSet(rs));
            }
        } catch (SQLException e) {
            logger.warn("查询所有宠物失败: " + e.getMessage());
        }
        return pets;
    }

    /**
     * 更新宠物信息
     */
    @Override
    public boolean update(Pet pet) {
        if (pet == null) {
            logger.warn("尝试更新空宠物");
            return false;
        }

        String sql = "update pets set name = ?, specie = ?, age = ?, " +
                "description = ?, adoption_status = ? where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pet.getName());
            pstmt.setString(2, pet.getSpecie().name().toLowerCase());
            pstmt.setInt(3, pet.getAge());
            pstmt.setString(4, pet.getDescription());
            pstmt.setString(5, pet.getAdoptionStatus().name().toLowerCase());
            pstmt.setString(6, pet.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("宠物更新成功: {}", pet.getName());
                return true;
            }
        } catch (SQLException e) {
            logger.warn("更新宠物失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 删除宠物
     */
    @Override
    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        String sql = "delete from pets where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("宠物删除成功: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.warn("删除宠物失败: {}", e.getMessage());
        }

        logger.warn("尝试删除不存在的宠物: {}", id);
        return false;
    }

    /**
     * 统计宠物数量
     */
    @Override
    public int count() {
        String sql = "select count(*) from pets";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("统计宠物失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 根据物种查询宠物
     */
    public List<Pet> findBySpecies(PetSpecies species) {
        List<Pet> pets = new ArrayList<>();
        String sql = "select * from pets where specie = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, species.name().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(getPetByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("按物种查询宠物失败: {}", e.getMessage());
        }
        return pets;
    }

    /**
     * 根据领养状态查询宠物
     */
    public List<Pet> findByStatus(PetStatus status) {
        List<Pet> pets = new ArrayList<>();
        String sql = "select * from pets where adoption_status = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(getPetByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("按状态查询宠物失败: {}", e.getMessage());
        }
        return pets;
    }

    /**
     * 根据名字模糊查询宠物
     */
    public List<Pet> findByName(String name) {
        List<Pet> pets = new ArrayList<>();
        String sql = "select * from pets where name like ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(getPetByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("按名称查询宠物失败: {}", e.getMessage());
        }
        return pets;
    }

    /**
     * 将 ResultSet 转换为 Pet 对象
     */
    private Pet getPetByResultSet(ResultSet rs) throws SQLException {
        return Pet.createFromDatabase(
                rs.getString("id"),
                rs.getString("name"),
                PetSpecies.valueOf(rs.getString("specie").toUpperCase()),
                rs.getInt("age"),
                rs.getString("description"),
                PetStatus.valueOf(rs.getString("adoption_status").toUpperCase()),
                rs.getLong("create_time")
        );
    }
}
