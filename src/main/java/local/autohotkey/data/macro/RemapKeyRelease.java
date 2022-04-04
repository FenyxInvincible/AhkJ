package local.autohotkey.data.macro;

import local.autohotkey.data.Key;
import local.autohotkey.sender.Sender;
import local.autohotkey.service.KeyManager;
import local.autohotkey.utils.eso.Locks;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Scope("prototype")
public class RemapKeyRelease implements Macro {
    private final Sender sender;
    private final KeyManager keys;
    private final Locks locks;
    private Key overridableKey;

    @Override
    public void setParams(Object param, Key self) {
        List<String> params = (List<String>) param;
        overridableKey = keys.findKeyByText(params.get(0));
    }

    @Override
    public void run() {
        sender.releaseKey(overridableKey);
    }
}
