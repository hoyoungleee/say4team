-- ADMIN 계정
INSERT INTO tbl_users (name, password, email, address, role, status, phone, birth_date,
                       registered_at, social_id, profile_image, social_provider)
VALUES ('admin',
        '$2a$10$2zFEXENDdTCzjOijyyOQj.jdy/aPXg/33aonIQ9mBMnd2T50BeOK.',
        'admin@admin.com',
        '서울특별시 강남구',
        'ADMIN',
        'ACTIVE',
        '010-1111-2222',
        '2025-06-02',
        NOW(),
        NULL,
        NULL,
        NULL);

-- USER 계정
INSERT INTO tbl_users (name, password, email, address, role, status, phone, birth_date,
                       registered_at, social_id, profile_image, social_provider)
VALUES ('user',
        '$2a$10$DjkrliK9L/06dXqoix8JIeuIcExkVWbc1eswetti/bzFoxsLN51H2',
        'user@example.com',
        '부산광역시 해운대구',
        'USER',
        'ACTIVE',
        '010-2222-3333',
        '2025-06-03',
        NOW(),
        NULL,
        NULL,
        NULL);

