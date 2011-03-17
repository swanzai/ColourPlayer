/* Copyright (C) 2006 Michael Voong

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */

package helpers.sql;

public interface Query {
    public void addColumn(String column);
    public void addWhere(String column, String value, boolean strict, int connector);
    public void addWhere(String column, int value, boolean strict, int connector);
    public void addTable(String table);
    public String toString();
    public void startBracket();
    public void endBracket();
    
    public static final int CONNECTOR_OR = 23;
    public static final int CONNECTOR_AND = 24;
    
}
