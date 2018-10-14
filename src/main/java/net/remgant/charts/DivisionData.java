/*
  Copyright (C) 2018 Jeffrey D. Remillard <jdr@remgant.net>

  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  later version.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program. If not,
  see <https://www.gnu.org/licenses/>.
 */

package net.remgant.charts;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class DivisionData {
    private String longName;
    private String fileName;
    private String shortName;
    private List<String> members;

    public DivisionData(String longName,  String shortName, String fileName, List<String> members) {
        this.longName = longName;
        this.fileName = fileName;
        this.shortName = shortName;
        this.members = members;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getMembers() {
        return members;
    }
}
