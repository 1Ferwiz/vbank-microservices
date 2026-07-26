-- Database-per-service: each microservice gets its own isolated database
-- inside this single local Postgres instance.

CREATE DATABASE users_db;
CREATE DATABASE accounts_db;
CREATE DATABASE transactions_db;
CREATE DATABASE logs_db;

-- Enable UUID generation (gen_random_uuid()) in the databases that need it.
-- logs_db uses a plain BIGSERIAL id, so it doesn't need this extension.

\connect users_db
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\connect accounts_db
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\connect transactions_db
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
