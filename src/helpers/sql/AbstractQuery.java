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

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractQuery implements Query {
    protected boolean startBracket = false;

    protected StringBuffer sql = new StringBuffer();

    protected ArrayList<String> columns = new ArrayList<String>();

    protected ArrayList<String> tables = new ArrayList<String>();

    protected ArrayList<WhereItem> wheres = new ArrayList<WhereItem>();

    protected int limit = 0;

    protected int offset = 0;
    
    protected boolean useLimitOffset = true;

    public void addColumn(String column) {
        columns.add(column);
    }

    public void endBracket() {
        if (wheres.size() == 0)
            return;

        wheres.get(wheres.size() - 1).suffix = ")";
    }

    public void startBracket() {
        startBracket = true;
    }

    public void addWhere(String column, String value, boolean strict,
            int connector) {
        WhereItem item = new WhereItem(column, value, strict, connector);
        wheres.add(item);

        if (startBracket) {
            startBracket = false;
            item.prefix = "(";
        }
    }

    public void addWhere(String column, int value, boolean strict, int connector) {
        wheres.add(new WhereItem(column, value, strict, connector));
    }

    public void removeWhere(String column) {
        Iterator it = wheres.iterator();
        WhereItem item;
        
        while (it.hasNext()) {
            item = ((WhereItem) it.next());
            if (item.column.equals(column)) {
                it.remove();
            }
        }

    }

    public void addTable(String table) {
        tables.add(table);
    }

    public void addLimit(int limit) {
        this.limit = limit;
    }

    public void addOffset(int offset) {
        this.offset = offset;
    }

    public abstract String toString();

    class WhereItem {
        String column;

        String valueString;

        int valueInt;

        boolean strict;

        int connector;

        String prefix;

        String suffix;

        public WhereItem() {
        }

        public WhereItem(String column, String value, boolean strict,
                int connector) {
            this();
            this.column = column;
            valueString = value;
            this.strict = strict;
            this.connector = connector;
        }

        public WhereItem(String column, int value, boolean strict, int connector) {
            this();
            this.column = column;
            valueInt = value;
        }
    }
}
