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

package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import exceptions.DatabaseInUseException;
import exceptions.DriverNotFoundException;

public class Database {

    public static Connection conn;

    public static Connection getConnection() throws DriverNotFoundException,
            DatabaseInUseException {
        if (conn == null) {
            try {
                Class.forName("org.hsqldb.jdbcDriver");
                conn = DriverManager.getConnection(
                        "jdbc:hsqldb:file:database/music;SHUTDOWN=true", "sa",
                        "");

                return conn;
            } catch (ClassNotFoundException e) {
                throw new DriverNotFoundException();
            } catch (SQLException sqle) {
                throw new DatabaseInUseException();
            }
        } else {
            return conn;
        }
    }

    public static void close() throws SQLException {
        if (conn != null) {
            conn.close();
            System.out.println("Closed db connection");
        }
    }

    public static void cleanUp() {
        try {
            Database.getConnection().createStatement()
                    .executeUpdate("SHUTDOWN");
        } catch (DatabaseInUseException e) {
            e.printStackTrace();
        } catch (DriverNotFoundException dnf) {
            dnf.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        System.out.println("Database shut down");
    }

    public static ResultSet getResult(String sql)
            throws DatabaseInUseException, SQLException {
        try {
            Connection conn = Database.getConnection();
            return conn.createStatement().executeQuery(sql);
        } catch (DriverNotFoundException e) {
            return null;
        }
    }

    public static int executeUpdate(String sql) throws DatabaseInUseException,
            SQLException {
        try {
            Connection conn = Database.getConnection();
            return conn.createStatement().executeUpdate(sql);
        } catch (DriverNotFoundException e) {
            return 0;
        }
    }
}
