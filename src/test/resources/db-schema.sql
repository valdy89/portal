CREATE TABLE portal_config
(
  name  VARCHAR(50) NOT NULL PRIMARY KEY,
  value VARCHAR(255)
);

CREATE TABLE users (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  password VARCHAR(500) NOT NULL,
  uid varchar(36),
  firstname varchar(100),
  surname varchar(100),
  vmCount TINYINT(2),
  quota INTEGER,
  enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE authorities (
  username VARCHAR(50) NOT NULL,
  authority VARCHAR(50) NOT NULL,
  CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);

CREATE UNIQUE INDEX ix_auth_username
  ON authorities (username, authority);