INSERT INTO registered_user (
  id, first_name, last_name, user_name, password, email
) VALUES
  (10001, 'Test', 'T', 'test_user', '$2a$10$amxuzGDvTfbrtwB0AY3B0uJKva7m0viMzb8dit5D71AUQTHrjS9OO', 'test@mail.ru'),
  (10002, 'Test', 'T', 'service_user', '$2a$10$amxuzGDvTfbrtwB0AY3B0uJKva7m0viMzb8dit5D71AUQTHrjS9OO', 'service@mail.ru'),
  (10003, 'admin', 'A', 'admin_user', '$2a$10$amxuzGDvTfbrtwB0AY3B0uJKva7m0viMzb8dit5D71AUQTHrjS9OO', 'admin@mail.ru');

INSERT INTO authorities (
  username, authority
) VALUES
  ('test_user', 'ROLE_USER'),
  ('admin_user', 'ROLE_ADMIN');