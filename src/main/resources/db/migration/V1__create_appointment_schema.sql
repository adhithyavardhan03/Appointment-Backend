CREATE TABLE IF NOT EXISTS providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_providers_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS services (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    duration INT NOT NULL,
    price DOUBLE NOT NULL,
    provider_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_services_provider (provider_id),
    CONSTRAINT fk_services_provider FOREIGN KEY (provider_id) REFERENCES providers (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS slots (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_slots_provider_status (provider_id, status),
    KEY idx_slots_service_date (service_id, date),
    CONSTRAINT fk_slots_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT fk_slots_provider FOREIGN KEY (provider_id) REFERENCES providers (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    slot_id BIGINT,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_appointments_customer (customer_id),
    KEY idx_appointments_provider (provider_id),
    KEY idx_appointments_slot (slot_id),
    CONSTRAINT fk_appointments_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT fk_appointments_provider FOREIGN KEY (provider_id) REFERENCES providers (id),
    CONSTRAINT fk_appointments_slot FOREIGN KEY (slot_id) REFERENCES slots (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS waitlist (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    preferred_date DATE NOT NULL,
    preferred_start_time TIME NOT NULL,
    preferred_end_time TIME NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_waitlist_service_status (service_id, status),
    KEY idx_waitlist_customer (customer_id),
    CONSTRAINT fk_waitlist_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_waitlist_service FOREIGN KEY (service_id) REFERENCES services (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    waitlist_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    service_score INT NOT NULL,
    date_score INT NOT NULL,
    time_score INT NOT NULL,
    total_score INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_matches_slot_status (slot_id, status),
    KEY idx_matches_waitlist (waitlist_id),
    CONSTRAINT fk_matches_waitlist FOREIGN KEY (waitlist_id) REFERENCES waitlist (id),
    CONSTRAINT fk_matches_slot FOREIGN KEY (slot_id) REFERENCES slots (id)
) ENGINE=InnoDB;
