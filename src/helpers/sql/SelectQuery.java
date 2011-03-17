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

import helpers.StringUtilities;

import java.util.ArrayList;

import data.OrderByItem;

public class SelectQuery extends AbstractQuery {
    ArrayList<LeftJoinTable> leftJoinTables = new ArrayList<LeftJoinTable>();

    private ArrayList<OrderByItem> orderByItems;

    private boolean isAggregateQuery;

    private ArrayList<String> groupBys;

    public SelectQuery(boolean isAggregateQuery) {
        super();
        this.isAggregateQuery = isAggregateQuery;
    }

    @Override
    public String toString() {
        sql.setLength(0);

        if (columns.size() == 0
                || (tables.size() == 0 && leftJoinTables.size() == 0)) {
            return "";
        }

        sql.append("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            sql.append(StringUtilities.escapeQuotes(columns.get(i)) + " ");

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append("FROM ");

        // normal joins
        for (int i = 0; i < tables.size(); i++) {
            sql.append(StringUtilities.escapeQuotes(tables.get(i)));

            if (i < tables.size() - 1) {
                sql.append(", ");
            } else {
                sql.append(" ");
            }
        }

        // left joins
        if (leftJoinTables.size() > 0) {
            if (tables.size() > 0) {
                sql.append(", ");
            }

            LeftJoinTable joinTable;
            for (int i = 0; i < leftJoinTables.size(); i++) {
                joinTable = leftJoinTables.get(i);

                sql.append(joinTable.table1 + " LEFT JOIN " + joinTable.table2
                        + " ON " + joinTable.column1 + " = "
                        + joinTable.column2 + " ");
            }
        }

        if (wheres.size() > 0) {
            sql.append("WHERE ");

            WhereItem item;
            for (int i = 0; i < wheres.size(); i++) {
                item = wheres.get(i);

                if (item.prefix != null && !item.prefix.equals("")) {
                    sql.append(item.prefix);
                }

                if (item.strict && item.valueString != null) {
                    // strict string
                    sql.append(item.column + " = ");

                    if (item.valueString != null) {
                        sql.append("'"
                                + StringUtilities
                                        .escapeQuotes(item.valueString) + "' ");
                    } else {

                    }
                } else if (!item.strict && item.valueString != null) {
                    // not strict, and a string
                    sql.append("LOCATE(LCASE('"
                            + StringUtilities.escapeQuotes(item.valueString)
                            + "'), LCASE(" + item.column + "))!=0 ");

                } else {
                    // integer.. doesnt matter if strict or not
                    sql.append(item.column + " = ");
                    sql.append(item.valueInt + " ");
                }

                if (i < wheres.size() - 1) {
                    sql.append(" "
                            + ((item.connector == Query.CONNECTOR_AND) ? "AND"
                                    : "OR") + " ");
                } else if (item.suffix != null && !item.suffix.equals("")) {
                    sql.append(item.suffix + " ");
                }
            }
        }

        // TODO: THIS IS A HACK! There must be another wayt
        // the problem arises when a query such as SELECT * FROM tracks_view
        // LIMIT 100 OFFSET 0; is executed.
        // HSQLDB must have a WHERE item if we use limit and/or offset
        if (wheres.size() == 0) {
            sql.append("WHERE 1 = 1 ");
        }

        // group bys
        if (groupBys != null) {
            sql.append("GROUP BY ");
            int i = 0;
            for (String groupBy : groupBys) {
                sql.append(groupBy);

                if (i < groupBys.size() - 1) {
                    sql.append(", ");
                } else {
                    sql.append(" ");
                }
                i++;
            }
        }

        // ordering
        if (!isAggregateQuery && orderByItems != null
                && orderByItems.size() > 0) {
            sql.append("ORDER BY ");
            int i = 0;
            for (OrderByItem item : orderByItems) {
                sql
                        .append(item.column
                                + " "
                                + ((item.direction == OrderByItem.DIRECTION_ASC) ? "ASC"
                                        : "DESC"));
                if (i < orderByItems.size() - 1) {
                    sql.append(", ");
                } else {
                    sql.append(" ");
                }
                i++;
            }
        }

        if (useLimitOffset) {

            sql.append("LIMIT " + limit + " ");
            sql.append("OFFSET " + offset);
        }

        // sql.append(";");

        return sql.toString();
    }

    public void addOrderBy(OrderByItem item) {
        if (orderByItems == null) {
            orderByItems = new ArrayList<OrderByItem>();
        }
        orderByItems.add(item);
    }

    public void addTableLeftJoin(String table1, String table2, String column1,
            String column2) {
        leftJoinTables.add(new LeftJoinTable(table1, table2, column1, column2));
    }

    public void clearColumns() {
        columns.clear();
    }

    class LeftJoinTable {
        String table1;

        String table2;

        String column1;

        String column2;

        public LeftJoinTable(String table1, String table2, String column1,
                String column2) {
            this.table1 = table1;
            this.table2 = table2;
            this.column1 = column1;
            this.column2 = column2;
        }
    }

    public static void main(String[] args) {
        SelectQuery query = new SelectQuery(false);
        query.addTable("tracks");
        query.addColumn("title");
        query.addWhere("title", "Showbiz", false, 0);
        System.out.println(query.toString());
    }

    public int getOrderBys() {
        if (orderByItems == null)
            return 0;
        return orderByItems.size();
    }

    public void setUseLimitOffset(boolean use) {
        useLimitOffset = use;
    }

    public boolean isUseLimitOffset() {
        return useLimitOffset;
    }

    public void clearOrderBys() {
        if (orderByItems != null)
            orderByItems.clear();
    }

    public void addGroupBy(String groupBy) {
        if (groupBys == null) {
            groupBys = new ArrayList<String>();
        }
        groupBys.add(groupBy);
    }

}
