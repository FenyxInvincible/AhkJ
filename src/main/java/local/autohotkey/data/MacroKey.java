package local.autohotkey.data;

import lombok.Data;

@Data
public class MacroKey {
    private final Key key;
    private final Key modifier;
}
