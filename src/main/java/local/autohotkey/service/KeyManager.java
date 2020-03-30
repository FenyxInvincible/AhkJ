package local.autohotkey.service;

import local.autohotkey.data.Key;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyManager {

    @Value("${app.keys.all.file}")
    private String keyConfig;

    private Map<Integer, Key> keys;
    private Map<Integer, Key> macroKeys;

    @PostConstruct
    private void init() throws IOException {
        keys = new JsonReader(keyConfig).parse().entrySet()
            .stream().map(s -> Pair.of(Integer.valueOf(s.getKey()), Key.create(s.getValue().getAsJsonObject())))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        /*Map<Integer, Key> map = keys.entrySet().stream()
                .map(e -> Pair.of(e.getValue().getRawCode(), e.getValue()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        log.info(new Gson().toJson(map));*/
    }

    public boolean hasKey(int keyCode) {
        return keys.containsKey(keyCode);
    }

    public Integer findKeyCodeByText(String key) {
        return keys.entrySet().stream()
                .filter(e -> e.getValue().getKeyText().equals(key))
                .map(Map.Entry::getValue)
                .findAny().orElseThrow(() -> new IllegalArgumentException("Could not find key " + key))
                .getKeyCode();
    }

    public Key findKeyByText(String key) {
        return keys.get(findKeyCodeByText(key));
    }

    public Key findKeyByKeyCode(int keyCode) {
        return keys.getOrDefault(keyCode, keys.get(0));
    }
}
