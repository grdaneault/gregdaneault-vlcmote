/*-
 *  Copyright (C) 2011 Peter Baldwin   
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.peterbaldwin.vlcremote.loader;

import org.peterbaldwin.vlcremote.model.Directory;
import org.peterbaldwin.vlcremote.model.Preferences;
import org.peterbaldwin.vlcremote.model.Remote;
import org.peterbaldwin.vlcremote.net.MediaServer;

import android.content.Context;
import android.util.Log;

import java.util.regex.Pattern;

public class DirectoryLoader extends ModelLoader<Remote<Directory>> {

    private final MediaServer mMediaServer;

    private final Preferences mPreferences;
    
    private final String mDir;

    public static String simplifyDirectory(String dir)
    {
        dir = dir.concat("\\");
        String n = dir.replaceAll("([^\\\\\\.]*)(?:\\\\[^\\\\\\.]+\\\\\\.\\.\\\\)([^\\\\]*)", "$1\\\\$2");

        while (!dir.equals(n))
        {
            dir = n;
            n = dir.replaceAll("([^\\\\\\.]*)(?:\\\\[^\\\\\\.]+\\\\\\.\\.\\\\)([^\\\\]*)", "$1\\\\$2");
        }
    
        return dir.substring(0,dir.length()-1);
        
    }
    public DirectoryLoader(Context context, MediaServer mediaServer, String dir, Preferences prefs) {
        super(context);
        mMediaServer = mediaServer;
        if (!dir.equals(""))
        {
            mDir = DirectoryLoader.simplifyDirectory(dir);
        }
        else
        {
            mDir = dir;
        }
        mPreferences = prefs;
        
    }

    @Override
    public Remote<Directory> loadInBackground() {
        return mMediaServer.browse(mDir).load(mPreferences);
    }
}
