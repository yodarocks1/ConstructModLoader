/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.beans;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author benne
 */
public class Item {

    private final String name;
    private final String uuid;

    public Item(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public static List<Item> fromJson(String json) {
        List<Item> items = new ArrayList();
        String jsonShort = json.replaceAll("\t", "").replaceAll(" ", "");
        for (String object : jsonShort.split("\\{")) {
            int uuidIndex = object.indexOf("uuid");
            int nameIndex = object.indexOf("name");
            if (uuidIndex != -1 && nameIndex != -1) {
                String u = object.substring(uuidIndex);
                String n = object.substring(nameIndex);
                String uuid = u.substring(u.indexOf(":") + 1, u.indexOf(",")).replaceAll("\"", "");
                String name = n.substring(n.indexOf(":") + 1, n.indexOf(",")).replaceAll("\"", "");
                String type = (name.startsWith("obj_")) ? "[Part] " : (name.startsWith("blk_")) ? "[Block] " : "[Item] ";
                items.add(new Item(name, uuid));
            }
        }
        return items;
    }

}
