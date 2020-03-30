package local.autohotkey.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import local.autohotkey.data.Key;
import local.autohotkey.data.macro.DummyMacro;
import local.autohotkey.data.macro.Macro;
import local.autohotkey.macro.MacroThreads;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.coley.simplejna.hook.key.KeyEventReceiver;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MacroFactory {

    public static final int KEY_PRESS = 2401;
    public static final int KEY_RELEASE = 2402;

    private final String macroConfigFile;
    private final ApplicationContext context;
    private final KeyManager keyManager;
    private final String macroPackage;
    private Map<Integer, MacroPair> macros;
    private Map<Integer, MacroThreads> macroThreadMap = new ConcurrentHashMap<>();

    public MacroFactory(
            @Value("${app.macro.mapping.file}") String path,
            @Value("${app.macro.package}") String macroPackage,
            KeyManager keyManager,
            ApplicationContext context
    ) {
        this.macroConfigFile = path;
        this.macroPackage = macroPackage;
        this.keyManager = keyManager;
        this.context = context;
    }

    @PostConstruct
    private void init() throws IOException {
        macros = new JsonReader(macroConfigFile).parse()
                .entrySet().stream()
                .map(e -> Pair.of(keyManager.findKeyCodeByText(e.getKey()), createMacroPair(e.getValue())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private MacroPair createMacroPair(JsonElement value) {
        JsonObject json = value.getAsJsonObject();
        JsonElement onPress = json.get("onPress");
        JsonElement onRelease = json.get("onRelease");
        return new MacroPair(findMacroClass(onPress), findMacroClass(onRelease));
    }


    public void execute(Key key, KeyEventReceiver.PressState pressState) {
        Macro macro = macros.get(key.getKeyCode()).getMacroByEventType(pressState);

        if (!macroThreadMap.containsKey(key.getKeyCode())) {
            macroThreadMap.put(key.getKeyCode(), new MacroThreads());
        }

        log.debug("Macro found {}", macro.getClass());

        MacroThreads macroThreads = macroThreadMap.get(key.getKeyCode());

        macroThreads.run(macro, pressState);
    }

    private Macro findMacroClass(JsonElement value) {
        Macro bean = null;

        if (value == null) {
            bean = new DummyMacro();
        } else {
            try {
                String className = value.getAsJsonObject().get("class").getAsString();
                Class<?> clazz = Class.forName(macroPackage + "." + className);
                bean = (Macro) context.getBean(clazz);

                ArrayList<String> params = new ArrayList<>();
                value.getAsJsonObject().get("params").getAsJsonArray()
                        .iterator().forEachRemaining(jsonElement -> {
                    params.add(jsonElement.getAsString());
                });

                bean.setParams(params);

            } catch (ClassCastException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Can not find macro class" + value);
            }
        }

        return bean;
    }

    public boolean hasMacro(Key key) {
        return macros.containsKey(key.getKeyCode());
    }

    @Data
    private static class MacroPair {
        private final Macro onPressMacro;
        private final Macro onReleaseMacro;

        public Macro getMacroByEventType(KeyEventReceiver.PressState eventType) {
            switch(eventType) {
                case DOWN:
                    return getOnPressMacro();
                case UP:
                    return getOnReleaseMacro();
                default:
                    throw new IllegalArgumentException("Unknown event type " + eventType);
            }
        }
    }
}
