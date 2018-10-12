-- create table teams (
--   id int primary key identity,
--   code char(3),
--   city varchar(32),
--   nickname varchar(24),
--   league char(2),
--   division char(3),
--   firstYear int,
--   lastYear int,
--   r int,
--   g int,
--   b int
-- );
-- ANA,LAA,AL,,Los Angeles,Angels,,4/11/1961,9/1/1965,Los Angeles,CA
create table teams (
  abbrev char(3),
  abbrev2 char(3),
  league char(2),
  division char(1),
  city varchar(32),
  nickname varchar(32),
  alt_nickname varchar(32),
  start_date date,
  end_date date
);

create table game_results (
  game_date date,
  team char(3),
  wins int,
  losses int
) ;

create table team_colors (
  abbrev char(3),
  r int,
  g int,
  b int
);

create table leagues (
  short_name varchar(3),
  long_name varchar(32)
)