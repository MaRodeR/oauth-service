INSERT INTO registered_user (
  id, first_name, last_name, user_name, password, email
) VALUES
  (10001, 'Test', 'T', 'test_user', '0eeb5d87a2e39c70e3d22f8154650f8c', 'test@mail.ru'),
  (10002, 'Test', 'T', 'jodform_user', '0eeb5d87a2e39c70e3d22f8154650f8c', 'jodform@mail.ru'),
  (10003, 'admin', 'A', 'admin_user', '0eeb5d87a2e39c70e3d22f8154650f8c', 'admin@mail.ru');

INSERT INTO authorities (
  username, authority
) VALUES
  ('test_user', 'ROLE_USER'),
  ('admin_user', 'ROLE_ADMIN');