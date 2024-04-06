package org.EIQUI.GCBAPI.fabric;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;

import static org.EIQUI.GCBAPI.main.that;

public class Fabric {

    public static void sendPacketToFabric(Player p,String c, String name){
        ByteBuf buf = Unpooled.buffer();
        writeString(buf, name);
        p.sendPluginMessage(that, c, buf.array());
    }

    private static void writeString(ByteBuf buf, String string) {
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        int i = 98301;
        writeVarInt(buf, bs.length);
        buf.writeBytes(bs);
    }
    private static void writeVarInt(ByteBuf buf, int value) {
        while((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        buf.writeByte(value);
    }
    public static void writeIntArray(ByteBuf buf, int[] array) {
        writeVarInt(buf, array.length);
        int[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            writeVarInt(buf, i);
        }

    }
}
