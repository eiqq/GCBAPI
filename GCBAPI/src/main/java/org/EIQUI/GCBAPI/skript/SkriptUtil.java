package org.EIQUI.GCBAPI.skript;

import ch.njol.skript.events.bukkit.SkriptParseEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.EIQUI.GCBAPI.main.that;


public class SkriptUtil implements Listener {

    private static Map<String,String> options = new HashMap<String,String>();

    public SkriptUtil(){
    }

    public static String getOption(String skriptname,String option) {
        for(String s: options.keySet()){
            that.getLogger().log(Level.INFO, s+": "+options.get(s));
        }
        return options.get(skriptname+":"+option);
    }

    @EventHandler
    public void SkriptParseEvent(SkriptParseEvent e){
        that.getLogger().log(Level.INFO, "SkriptLoad");
        HashMap<String,String> op = ParserInstance.get().getCurrentOptions();
        String filename = ParserInstance.get().getCurrentScript().getFileName();
        for(String s: op.keySet()){
            options.put(filename+":"+s,op.get(s));
        }
    }

}
