package local.autohotkey.data.macro.poe;

import local.autohotkey.data.Key;
import local.autohotkey.data.macro.Macro;
import local.autohotkey.sender.Sender;
import local.autohotkey.service.KeyManager;
import local.autohotkey.utils.Overlay;
import local.autohotkey.utils.ScreenPicker;
import local.autohotkey.utils.eso.Locks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Scope("prototype")
@Slf4j
public class Heal implements Macro {
    private final Sender sender;
    private final KeyManager keys;
    private final Overlay overlay;
    private Key key1;
    private Key key3;
    private Key key9;
    private static AtomicBoolean isInterrupted = new AtomicBoolean(false);
    private static AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void setParams(List<String> params) {
        key1 = keys.findKeyByText("1");
        key3 = keys.findKeyByText("3");
        key9 = keys.findKeyByText("9");
    }

    @Override
    public void run() {
        if (!isStarted.get()) {
            Executors.newSingleThreadExecutor().execute(new HealDaemon());
            isStarted.set(true);
            isInterrupted.set(false);
            toggleOverlay();
        } else {
            isStarted.set(false);
            isInterrupted.set(true);
            toggleOverlay();
        }
    }

    private void toggleOverlay(){
        overlay.draw(UUID.randomUUID().toString(), graphics -> {
            try {
                if(isStarted.get()) {
                    InputStream image = new ClassPathResource("poe/heal_icon.png").getInputStream();
                    graphics.setColor(new Color(1f,0f,0f,.5f));
                    graphics.drawImage(ImageIO.read(image), 0, 80, null);
                } else {
                    overlay.clean();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    class HealDaemon implements Runnable {

        @Override
        public void run() {
            while (!isInterrupted.get()) {
                int sleep = 200;
                try {
                    //hp
                    int color = ScreenPicker.pickDwordColor(140, 1247);
                    if (color != 2693048) {
                        log.debug("Sending heal {} expected 2693048", color);
                        sender.sendKey(key1, 30);
                        sender.sendKey(key3, 30);
                        sleep = 1500;
                    }
                    //mp
                    color = ScreenPicker.pickDwordColor(2404, 1286); //x: 2404.0 y: 1286.0 Dword: 12088087 Color: java.awt.Color[r=184,g=115,b=23
                    if (color != 12088087) {
                        log.debug("Sending mana {} expected 12088087", color);
                        sender.sendKey(key9, 30);
                        sleep = 1500;
                    }
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isInterrupted.set(false);
        }
    }
}
