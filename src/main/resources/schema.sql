--   Copyright (C) 2018 Jeffrey D. Remillard <jdr@remgant.net>
--
--   This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
--   License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
--   later version.
--
--   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
--   warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
--
--   You should have received a copy of the GNU General Public License along with this program. If not,
--   see <https://www.gnu.org/licenses/>.
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
  long_name varchar(32),
  start int not null,
  end int
)