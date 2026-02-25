CREATE TABLE IF NOT EXISTS food (
                                    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    dtype       VARCHAR(31),
                                    name        VARCHAR(255),
                                    remarks     VARCHAR(255),
                                    created_at  TIMESTAMP,
                                    updated_at  TIMESTAMP,
                                    deleted_at  TIMESTAMP,
                                    best_before_end DATE,
                                    original_ml_g   DOUBLE,
                                    remaining_ml_g  DOUBLE,
                                    use_by      DATE
);

CREATE TABLE IF NOT EXISTS storage (
                                       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name       VARCHAR(255),
                                       remarks    VARCHAR(255)
);