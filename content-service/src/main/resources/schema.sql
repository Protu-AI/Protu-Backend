--DROP TABLE IF EXISTS courses_lessons;
--DROP TABLE IF EXISTS tracks_courses;
--DROP TABLE IF EXISTS users_lessons;
--DROP TABLE IF EXISTS users_courses;
--DROP TABLE IF EXISTS lessons;
--DROP TABLE IF EXISTS courses;
--DROP TABLE IF EXISTS tracks;
--DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS tracks (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    description VARCHAR
);

ALTER TABLE tracks ADD CONSTRAINT tracks_name_unique UNIQUE(name);

CREATE TABLE IF NOT EXISTS courses (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    description VARCHAR,
    pic_url TEXT
);

ALTER TABLE courses ADD CONSTRAINT courses_name_unique UNIQUE(name);

CREATE TABLE IF NOT EXISTS tracks_courses (
    track_id     INT NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    course_id    INT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    PRIMARY KEY (track_id, course_id)
);

CREATE TABLE IF NOT EXISTS lessons (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    content     TEXT,
    lesson_order INT
);

ALTER TABLE lessons ADD CONSTRAINT lessons_name_unique UNIQUE(name);

CREATE TABLE IF NOT EXISTS courses_lessons (
    course_id    INT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id    INT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    PRIMARY KEY (course_id, lesson_id)
);

CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    public_id   CHAR(26) NOT NULL UNIQUE,
    roles       TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users_courses (
    user_id            INT REFERENCES users(id) ON DELETE CASCADE,
    course_id          INT REFERENCES courses(id) ON DELETE CASCADE,
    completed_lessons  INT DEFAULT 0,
    PRIMARY KEY (user_id, course_id)
);

CREATE TABLE IF NOT EXISTS users_lessons (
    user_id     INT REFERENCES users(id) ON DELETE CASCADE,
    lesson_id   INT REFERENCES lessons(id) ON DELETE CASCADE,
    is_completed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, lesson_id)
);