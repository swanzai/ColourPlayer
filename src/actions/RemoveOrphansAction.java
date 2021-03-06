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

package actions;

import org.eclipse.jface.action.Action;

import controllers.OrphanRemover;
import data.Dao;
import exceptions.DataAccessException;

public class RemoveOrphansAction extends Action {
    private final Dao dao;

    public RemoveOrphansAction(Dao dao) {
        this.dao = dao;
        
        setToolTipText("Remove files from the library which are non-existent");
        setText("Remove &Orphaned Files");        
    }

    @Override
    public void run() {
        try {
            new OrphanRemover(dao).removeOrphans();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
