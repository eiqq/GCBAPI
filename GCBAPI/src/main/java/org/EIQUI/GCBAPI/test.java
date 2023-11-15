package org.EIQUI.GCBAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;

public class test {

    public void test(String font) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            TextComponent nameComponent = new TextComponent("Sword");
            nameComponent.setFont(font);
            online.sendMessage(nameComponent.toLegacyText());
            nameComponent = new TextComponent("Sword");
            nameComponent.setFont(font);
            online.sendMessage(nameComponent.toString());
            nameComponent = new TextComponent("Sword");
            nameComponent.setFont(font);
            online.sendMessage(nameComponent.toPlainText());
            nameComponent = new TextComponent("Sword");
            nameComponent.setFont("minecraft:uniform");
            online.sendMessage(nameComponent.toPlainText());
            nameComponent = new TextComponent("Sword");
            nameComponent.setFont("minecraft:Uniform");
            online.sendMessage(nameComponent.toLegacyText());
        }
    }
}
