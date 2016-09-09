/*
 * CAL.
 *  A P2P chat program that lets you communicate without any infrastructure.
 *
 *   Copyright (C) 2015  Foo-Manroot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package files;

/**
 * Entry for the list of written bytes per file, on {@link FileObserver}.
 */
public class Entry {

    private final String filePath;
    private long writtenBytes;

    /**
     * Constructor.
     * 
     * @param filePath 
     *              String with the file path.
     * 
     * @param writtenBytes 
     *              Long number with the written bytes on th file.
     */
    public Entry (String filePath, long writtenBytes) {
        
        this.filePath = filePath;
        this.writtenBytes = writtenBytes;
    }

    public String getFilePath () {
        
        return filePath;
    }

    public long getWrittenBytes () {
        
        return writtenBytes;
    }
    
    /**
     * Sets a new value for {@code writtenBytes}.
     * 
     * @param newValue 
     *              The new value for {@code writtenBytes}.
     */
    public void setWrittenBytes (long newValue) {
     
        writtenBytes = newValue;
    }
}
