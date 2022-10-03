package local.macroj;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import local.macroj.data.Key;
import local.macroj.data.MacroKey;
import local.macroj.data.UseKeyData;
import local.macroj.jna.hook.key.KeyHookManager;
import local.macroj.jna.hook.mouse.MouseHookManager;
import local.macroj.sender.JnaSender;
import local.macroj.sender.Sender;
import local.macroj.serialization.ColorDeserializer;
import local.macroj.serialization.KeyDeserializer;
import local.macroj.serialization.MacroKeyDeserializer;
import local.macroj.serialization.UseKeyDataDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

@Configuration
public class ApplicationConfig {
    /**
     * Send action is press + delay + release. This is delay
     */
    public static final int DEFAULT_SEND_DELAY = 16;

    @Bean
    public Robot robot() throws AWTException {
        return new Robot();
    }
    @Bean
    public KeyHookManager keyHookManager() {
        return new KeyHookManager();
    }

    @Bean
    public MouseHookManager mouseHookManager() {
        return new MouseHookManager();
    }

    @Bean
    public Sender sender(){
        return new JnaSender();
    }

    @Bean
    public Gson gson(
            KeyDeserializer keyDeserializer,
            MacroKeyDeserializer macroKeyDeserializer,
            ColorDeserializer colorDeserializer,
            UseKeyDataDeserializer useKeyDataDeserializer
    ){
        return new GsonBuilder()
                .registerTypeAdapter(Key.class, keyDeserializer)
                .registerTypeAdapter(MacroKey.class, macroKeyDeserializer)
                .registerTypeAdapter(Color.class, colorDeserializer)
                .registerTypeAdapter(UseKeyData.class, useKeyDataDeserializer)
                .create();
    }
}
