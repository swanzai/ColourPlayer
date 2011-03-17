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

import java.io.File;

/**
 * An abstract file scanner. Classes must extend this one in order to set
 * behaviours for when files are found.
 * 
 * @author Mike
 */
public abstract class FileScanner {
    /**
     * When this is set to true, the file scanning stops
     */
    boolean cancel = false;

    public void cancel() {
        cancel = true;
    }

    public void scan(String path) {
        final File file = new File(path);

        recurseFolders(file);
        finished();
    }

    public void scanFiles(String[] paths) {
        for (String fileName : paths) {
            recurseFolders(new File(fileName));
        }

        finished();
    }

    /**
     * This method is recursive. That is, it calls itself every time it examines
     * a new directory
     * 
     * @param file
     *            The file or directory to start searching from
     */
    private void recurseFolders(File file) {
        if (cancel || file == null) {
            return;
        }

        if (file.isDirectory()) {
            File[] children = file.listFiles();

            if (children == null)
                return;

            for (File child : children) {
                recurseFolders(child);
            }
        } else if (file.isFile()) {
            fileFound(file);
        }
    }

    protected abstract void fileFound(File file);

    protected void finished() {
        System.out.println("Finished scan");
    }
}
