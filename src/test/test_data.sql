-- test/test_data.sql
-- 测试数据生成脚本

USE pam_db;

-- ========== 清空现有数据 ==========
DELETE FROM adoption_applications;
DELETE FROM announcements;
DELETE FROM pets;
DELETE FROM users;

-- ========== 创建管理员用户 ==========
-- 密码: admin123 (实际应该使用加密后的密码)
INSERT INTO users (
    id,
    username,
    password_hash,
    permission,
    real_name,
    phone,
    address,
    is_online,
    create_time
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin',
    'admin123',
    'admin',
    '管理员',
    '13800138000',
    '管理中心',
    FALSE,
    UNIX_TIMESTAMP() * 1000
);

-- ========== 创建普通用户 ==========
-- 密码: user123
INSERT INTO users (
    id,
    username,
    password_hash,
    permission,
    real_name,
    phone,
    address,
    is_online,
    create_time
) VALUES (
    'u0000000-0000-0000-0000-000000000001',
    'user',
    'user123',
    'normal',
    '张三',
    '13900139000',
    '北京市朝阳区',
    FALSE,
    UNIX_TIMESTAMP() * 1000
);

-- ========== 创建宠物 ==========
-- 宠物 1: 小白
INSERT INTO pets (
    id,
    name,
    specie,
    age,
    description,
    adoption_status,
    create_time
) VALUES (
    'p0000000-0000-0000-0000-000000000001',
    '小白',
    'dog',
    2,
    '可爱的小狗，很温顺',
    'available',
    UNIX_TIMESTAMP() * 1000
);

-- 宠物 2: 咪咪
INSERT INTO pets (
    id,
    name,
    specie,
    age,
    description,
    adoption_status,
    create_time
) VALUES (
    'p0000000-0000-0000-0000-000000000002',
    '咪咪',
    'cat',
    1,
    '活泼的小猫，喜欢玩耍',
    'available',
    UNIX_TIMESTAMP() * 1000
);

-- 宠物 3: 旺财
INSERT INTO pets (
    id,
    name,
    specie,
    age,
    description,
    adoption_status,
    create_time
) VALUES (
    'p0000000-0000-0000-0000-000000000003',
    '旺财',
    'dog',
    3,
    '忠诚的看门狗',
    'available',
    UNIX_TIMESTAMP() * 1000
);

-- ========== 创建领养申请 ==========
-- 申请 1: 用户申请领养小白
INSERT INTO adoption_applications (
    id,
    applicator_id,
    pet_id,
    status
) VALUES (
    'app00000-0000-0000-0000-000000000001',
    'u0000000-0000-0000-0000-000000000001',
    'p0000000-0000-0000-0000-000000000001',
    'pending'
);

-- 申请 2: 用户申请领养咪咪
INSERT INTO adoption_applications (
    id,
    applicator_id,
    pet_id,
    status
) VALUES (
    'app00000-0000-0000-0000-000000000002',
    'u0000000-0000-0000-0000-000000000001',
    'p0000000-0000-0000-0000-000000000002',
    'pending'
);

-- ========== 创建公告 ==========
INSERT INTO announcements (
    id,
    sender_id,
    title,
    content,
    create_time
) VALUES (
    'ann00000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    '欢迎使用宠物领养系统',
    '亲爱的用户们：\n\n\t欢迎使用我们的宠物领养系统！我们致力于为流浪动物找到温暖的家。

系统功能：
1. 浏览可领养的宠物
2. 提交领养申请
3. 查看申请状态
4. 接收最新公告

如果您有任何问题，请联系管理员。

祝好！
宠物领养管理团队',
    UNIX_TIMESTAMP() * 1000
);

-- ========== 验证数据 ==========
SELECT '========== 测试数据生成完成 ==========' AS message;

SELECT CONCAT('用户总数: ', COUNT(*)) AS result FROM users;
SELECT CONCAT('宠物总数: ', COUNT(*)) AS result FROM pets;
SELECT CONCAT('申请总数: ', COUNT(*)) AS result FROM adoption_applications;
SELECT CONCAT('公告总数: ', COUNT(*)) AS result FROM announcements;

SELECT '' AS empty_line;
SELECT '========== 账号信息 ==========' AS message;
SELECT '管理员账号: admin / admin123' AS account;
SELECT '普通用户: user / user123' AS account;
SELECT '=====================================' AS message;

-- 查看所有数据
SELECT * FROM users;
SELECT * FROM pets;
SELECT * FROM adoption_applications;
SELECT * FROM announcements;
