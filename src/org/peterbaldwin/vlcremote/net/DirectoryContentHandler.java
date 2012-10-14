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

package org.peterbaldwin.vlcremote.net;

import org.peterbaldwin.client.android.vlcremote.R;
import org.peterbaldwin.vlcremote.model.Directory;
import org.peterbaldwin.vlcremote.model.File;
import org.peterbaldwin.vlcremote.model.Preferences;
import org.xml.sax.Attributes;

import android.content.Context;
import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.text.GetChars;
import android.util.Log;

import java.io.IOException;
import java.net.URLConnection;
import java.util.regex.Pattern;

final class DirectoryContentHandler extends XmlContentHandler<Directory> {

    private final Preferences mPreferences;
    private String additional_extensions;

    private Pattern hex;

    public DirectoryContentHandler(Preferences prefs) {
        super();
        mPreferences = prefs;
        additional_extensions = mPreferences.getExtraFileExtensions();

        hex = Pattern.compile("[0-9a-fA-F]+");
    }

    private File createFile(Attributes attributes) {
        String type = attributes.getValue("", "type");
        String sizeString = attributes.getValue("", "size");
        Long size = null;
        try {
            if (sizeString != null && !sizeString.equals("unknown")) {
                size = Long.parseLong(sizeString);
            }
        } catch (NumberFormatException e) {
            // Ignore unexpected value
        }
        String date = attributes.getValue("", "date");
        String path = attributes.getValue("", "path");
        String name = attributes.getValue("", "name");
        String extension = attributes.getValue("", "extension");
        if (path != null && !path.startsWith("/")) { // Windows path
            // Work-around: Replace front-slash
            // appended by server with back-slash.
            path = path.replace('/', '\\');
        }

        return new File(type, size, date, path, name, extension);
    }

    private boolean isGibberish(String name) {
        if (mPreferences.getSmartDirectoryFiltering() && name.length() >= 16) {
            if (hex.matcher(name).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object getContent(URLConnection connection) throws IOException {
        final Directory directory = new Directory();
        final Directory folders = new Directory();
        final Directory files = new Directory();
        additional_extensions = mPreferences.getExtraFileExtensions();
        RootElement root = new RootElement("", "root");
        Element element = root.getChild("", "element");
        element.setStartElementListener(new StartElementListener() {
            /** {@inheritDoc} */
            public void start(Attributes attributes) {
                File file = createFile(attributes);

                String mime = file.getMimeType();
                String ext = file.getExtension();
                if (mPreferences.getFileFiltering()) {
                    if (mime == null && ext != null && !file.getType().equals("dir")) {
                        // Manual Override of known extensions
                        if (additional_extensions.indexOf(ext) >= 0) {
                            if (mPreferences.getSplitFoldersFiles()) {
                                files.add(file);
                            } else {
                                directory.add(file);
                            }
                        }
                    } else if (mime != null) {
                        if (mime.contains("audio") || mime.contains("video")) {
                            if (mPreferences.getSplitFoldersFiles()) {
                                files.add(file);
                            } else {
                                directory.add(file);
                            }
                        }
                    } else if (file.isDirectory()) {
                        String name = file.getName();
                        if (!mPreferences.getDirectoryFiltering()
                                || ((!name.startsWith(".") || name.equals(".."))
                                        && !name.startsWith("~") && !name.startsWith("$")
                                        && !name.startsWith("found.") && !name.endsWith(".tmp")
                                        && !name.equals("All Users") && !name.equals("AppData")
                                        && !name.equals("Application Data")
                                        && !name.equals("Contacts") && !name.equals("Cookies")
                                        && !name.equals("Default User") && !name.equals("Links")
                                        && !name.equals("MSOCache") && !name.equals("NetHood")
                                        && !name.equals("PerfLogs") && !name.equals("PrintHood")
                                        && !name.equals("ProgramData") && !name.equals("Recovery")
                                        && !name.equals("Saved Games") && !name.equals("Searches")
                                        && !name.equals("SendTo") && !name.equals("Start Menu")
                                        && !name.equals("System Volume Information")
                                        && !name.equals("Templates") && !name.equals("Windows")
                                        && !name.equals("WindowsImageBackup")
                                        && !name.contains("Program Files")
                                        && !name.contains("Recent") && !name.contains("Settings") && !isGibberish(name))) {

                            if (mPreferences.getSplitFoldersFiles()) {
                                folders.add(file);
                            } else {
                                directory.add(file);
                            }
                        }
                    }
                } else {
                    if (mPreferences.getSplitFoldersFiles()) {
                        files.add(file);
                    } else {
                        directory.add(file);
                    }
                }

            }
        });
        parse(connection, root.getContentHandler());

        if (!mPreferences.getSplitFoldersFiles()) {
            return directory;
        } else {
            folders.addAll(files);
            return folders;
        }
    }
}
