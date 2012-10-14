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

package org.peterbaldwin.vlcremote.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.peterbaldwin.client.android.vlcremote.R;
import org.peterbaldwin.vlcremote.loader.DirectoryLoader;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for reading and writing application preferences.
 */
public final class Preferences {

    private static final String PREFERENCES = "preferences";

    private static final String PREFERENCE_SERVER = "server";

    private static final String PREFERENCE_REMEMBERED_SERVERS = "remembered_servers";

    private static final String PREFERENCE_BROWSE_DIRECTORY = "browse_directory";

    private static final String PREFERENCE_HOME_DIRECTORY = "home_directory";

    private static final String PREFERENCE_RESUME_ON_IDLE = "resume_on_idle";

    private static final String PREFERENCE_DIRECTORY_FILTERING = "enable_filtering_directories";

    private static final String PREFERENCE_DIRECTORY_FILTERING_SMART = "enable_filtering_directories_smart";

    private static final String PREFERENCE_FILE_FILTERING = "enable_filtering";

    private static final String PREFERENCE_EXRTA_EXTENSIONS = "filtering_extra";

    private static final String PREFERENCE_SPLIT_FOLDERS_FILES = "split_folders_files";

    private SharedPreferences mPreferences;

    public Preferences(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public static Preferences get(Context context) {
        return new Preferences(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    public boolean setResumeOnIdle() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(PREFERENCE_RESUME_ON_IDLE, System.currentTimeMillis());
        return editor.commit();
    }

    /**
     * Returns {@code true} if {@link #setResumeOnIdle()} was called in the last
     * hour.
     */
    public boolean isResumeOnIdleSet() {
        long start = mPreferences.getLong(PREFERENCE_RESUME_ON_IDLE, 0L);
        long end = System.currentTimeMillis();
        return start < end && (end - start) < DateUtils.HOUR_IN_MILLIS;
    }
    
    public boolean clearResumeOnIdle() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREFERENCE_RESUME_ON_IDLE);
        return editor.commit();
    }

    public String getAuthority() {
        return mPreferences.getString(PREFERENCE_SERVER, null);
    }

    public String getHomeDirectory() {
        return mPreferences.getString(PREFERENCE_HOME_DIRECTORY, "~");
    }

    public String getBrowseDirectory() {
        return mPreferences.getString(PREFERENCE_BROWSE_DIRECTORY, "~");
    }
    
    public boolean getFileFiltering()
    {
        return mPreferences.getBoolean(PREFERENCE_FILE_FILTERING, true);
    }

    public boolean getDirectoryFiltering()
    {
        return mPreferences.getBoolean(PREFERENCE_DIRECTORY_FILTERING, true);
    }
    
    public boolean getSplitFoldersFiles()
    {
        return mPreferences.getBoolean(PREFERENCE_SPLIT_FOLDERS_FILES, true);
    }
    
    public boolean getSmartDirectoryFiltering()
    {
        return mPreferences.getBoolean(PREFERENCE_DIRECTORY_FILTERING_SMART, true);
    }
    
    public String getExtraFileExtensions()
    {
        return mPreferences.getString(PREFERENCE_EXRTA_EXTENSIONS, "mkv");
    }

    public boolean setAuthority(String authority) {
        return mPreferences.edit().putString(PREFERENCE_SERVER, authority).commit();
    }

    public boolean setHomeDirectory(String dir) {
        dir = DirectoryLoader.simplifyDirectory(dir);
        return mPreferences.edit().putString(PREFERENCE_HOME_DIRECTORY, dir).commit();
    }

    public boolean setBrowseDirectory(String dir) {
        return mPreferences.edit().putString(PREFERENCE_BROWSE_DIRECTORY, dir).commit();
    }
    
    public ArrayList<String> getRememberedServers() {
        return fromJSONArray(mPreferences.getString(PREFERENCE_REMEMBERED_SERVERS, "[]"));
    }

    public boolean setRemeberedServers(List<String> rememberedServers) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFERENCE_REMEMBERED_SERVERS, toJSONArray(rememberedServers));
        return editor.commit();
    }
    
    public boolean setFileFiltering(boolean enabled)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREFERENCE_FILE_FILTERING, enabled);
        return editor.commit();
    }
    
    public boolean setDirectoryFiltering(boolean enabled)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREFERENCE_DIRECTORY_FILTERING, enabled);
        return editor.commit();
    }
    
    public boolean setSmartDirectoryFiltering(boolean enabled)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREFERENCE_DIRECTORY_FILTERING_SMART, enabled);
        return editor.commit();
    }
    
    public boolean setSplitFoldersFiles(boolean enabled)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREFERENCE_SPLIT_FOLDERS_FILES, enabled);
        return editor.commit();
    }
    
    public boolean setExtraFileExtensions(String extensions) {
        return mPreferences.edit().putString(PREFERENCE_EXRTA_EXTENSIONS, extensions).commit();
    }

    private static String toJSONArray(List<String> list) {
        JSONArray array = new JSONArray(list);
        return array.toString();
    }

    private static ArrayList<String> fromJSONArray(String json) {
        try {
            JSONArray array = new JSONArray(json);
            int n = array.length();
            ArrayList<String> list = new ArrayList<String>(n);
            for (int i = 0; i < n; i++) {
                String element = array.getString(i);
                list.add(element);
            }
            return list;
        } catch (JSONException e) {
            return new ArrayList<String>(0);
        }
    }
}
