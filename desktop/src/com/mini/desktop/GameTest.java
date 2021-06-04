package com.mini.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.custom.game.CustomGame;

public class GameTest {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title="月影传说-V1.32";
        config.addIcon("D:/icon.png", Files.FileType.Internal);
        config.resizable=false;
        config.fullscreen=false;
        config.width=1524;
        config.height=768;
        LwjglApplication lwjglApplication = new LwjglApplication(new GameApplication(), config);
    }
}
