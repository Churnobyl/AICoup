CREATE TABLE IF NOT EXISTS action
(
    action_id                INTEGER,
    name                     VARCHAR(255),
    english_name             VARCHAR(255),
    description              VARCHAR(255),
    specific_character       INTEGER,
    counteraction_character1 INTEGER,
    counteraction_character2 INTEGER,
    can_be_blocked           BOOLEAN,
    can_be_challenged        BOOLEAN,
    CONSTRAINT PRIMARY KEY (action_id)
);

CREATE TABLE IF NOT EXISTS card_info
(
    card_info_id INTEGER,
    name         VARCHAR(100),
    english_name VARCHAR(100),
    image_url    VARCHAR(255),
    CONSTRAINT PRIMARY KEY (card_info_id)
);

CREATE TABLE IF NOT EXISTS game
(
    game_id       INTEGER AUTO_INCREMENT,
    name          VARCHAR(255),
    turn          INTEGER DEFAULT 0,
    created_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modified_by VARCHAR(255),
    CONSTRAINT PRIMARY KEY (game_id)
);

CREATE TABLE IF NOT EXISTS game_member
(
    game_member_id INTEGER AUTO_INCREMENT,
    game_id        INTEGER,
    name           VARCHAR(255),
    left_card      INTEGER,
    right_card     INTEGER,
    coin           INTEGER,
    is_player      BOOLEAN,
    created_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modified_by VARCHAR(255),
    CONSTRAINT PRIMARY KEY (game_member_id),
    CONSTRAINT fk_game_id FOREIGN KEY (game_id) REFERENCES game (game_id)
);

CREATE TABLE IF NOT EXISTS game_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_num INT NOT NULL,
    current_player INT NOT NULL,
    playerinfo JSON NOT NULL,
    history JSON NOT NULL,
    current_action VARCHAR(10),
    target VARCHAR(10),
    counter_actioner VARCHAR(10),
    counter_action VARCHAR(10)
);
