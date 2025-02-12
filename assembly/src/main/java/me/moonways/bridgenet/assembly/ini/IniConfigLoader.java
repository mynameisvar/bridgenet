package me.moonways.bridgenet.assembly.ini;

import me.moonways.bridgenet.assembly.BridgenetAssemblyException;
import me.moonways.bridgenet.assembly.ini.type.IniGroup;
import me.moonways.bridgenet.assembly.ini.type.IniProperty;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class IniConfigLoader {

    public IniConfig loadAbsolute(File file) {
        if (file == null) {
            return null;
        }
        try {
            return load(new FileInputStream(file));
        } catch (FileNotFoundException exception) {
            throw new BridgenetAssemblyException(exception);
        }
    }

    public IniConfig load(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        Set<IniGroup> properties = loadGroups(inputStream);
        return new IniConfig(properties);
    }

    private Set<IniGroup> loadGroups(InputStream inputStream) {
        try {
            final Ini ini = new Ini(inputStream);
            return ini.keySet()
                    .stream()
                    .map(groupName -> new IniGroup(groupName, loadProperties(ini, groupName)))
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            throw new BridgenetAssemblyException(exception);
        }
    }

    private IniProperty[] loadProperties(Ini ini, String groupName) {
        List<Profile.Section> sectionList = ini.getAll(groupName);
        return sectionList.stream()
                .flatMap(section -> section.keySet().stream())
                .map(propertyName -> new IniProperty(propertyName, ini.get(groupName, propertyName)))
                .toArray(IniProperty[]::new);
    }
}
