package str_exporter;

import basemod.*;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.MTSClassLoader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

@SpireInitializer
public class SlayTheRelicsExporter implements
        RelicGetSubscriber,
        PotionGetSubscriber,
        StartGameSubscriber,
        PostCreateStartingRelicsSubscriber,
        PostInitializeSubscriber,
        OnPowersModifiedSubscriber,
        PostPowerApplySubscriber,
        PostRenderSubscriber
{

    public static final Logger logger = LogManager.getLogger(SlayTheRelicsExporter.class.getName());
    public static final String MODID = "SlayTheRelicsExporter";

    private static String login = null;
    private static String secret = null;
    private static String version = "";

    private long lastBroadcast = System.currentTimeMillis();
    private long lastCheck = System.currentTimeMillis();
    private String lastBroadcastJson = "";
    private boolean checkNextUpdate = false;
    private JSONMessageBuilder json_builder;

    public static final String CUSTOM_TIP_HITBOX_NAME = "slayTheRelicsHitboxes";
    public static final String CUSTOM_TIP_POWERTIPS_NAME = "slayTheRelicsPowerTips";
    public ArrayList<Class<?>> customTipImplementingClasses;

//    private static final long MAX_BROADCAST_PERIOD_MILLIS = 250;
    private static final long MAX_CHECK_PERIOD_MILLIS = 2500;
    public static SlayTheRelicsExporter instance = null;

    public static Properties strDefaultSettings = new Properties();
    public static final String DELAY_SETTINGS = "delay";
    public static long delay = 0; // The boolean we'll be setting on/off (true/false)


    public SlayTheRelicsExporter()
    {
        json_builder = new JSONMessageBuilder(login, secret, version);
        BackendBroadcaster.start();

        logger.info("Slay The Relics Exporter initialized!");
        BaseMod.subscribe(this);

        strDefaultSettings.setProperty(DELAY_SETTINGS, "150");
        try {
            SpireConfig config = new SpireConfig("slayTheRelics", "slayTheRelicsExporterConfig", strDefaultSettings); // ...right here
            config.load();
            delay = config.getInt(DELAY_SETTINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean areCredentialsValid() {
        return login != null && secret != null;
    }

    public static String getVersion() {
        for (ModInfo info : Loader.MODINFOS) {
            if (info.ID.equals(MODID)) {
                return info.ModVersion.toString();
            }
        }

        return "unkwnown";
    }

    public static ArrayList<Class<?>> findCustomTipImplementingClasses() {
        logger.info("CHECKING FOR OTHER MODS");

        ArrayList<Class<?>> implementingClasses = new ArrayList<>();

        ClassLoader loader = SlayTheRelicsExporter.class.getClassLoader();

        for (ModInfo info : Loader.MODINFOS) {
            if (Patcher.annotationDBMap.containsKey(info.jarURL)) {
                Set<String> initializers = Patcher.annotationDBMap.get(info.jarURL).getAnnotationIndex().get(SpireInitializer.class.getName());
                if (initializers != null) {
                    System.out.println(" - " + info.Name);
                    for (String initializer : initializers) {
                        System.out.println("   - " + initializer);
                        try {
                            long startTime = System.nanoTime();

                            Class<?> c = loader.loadClass(initializer);

                            try {
                                Field hitboxes = c.getField(CUSTOM_TIP_HITBOX_NAME);
                                Field powerTips = c.getField(CUSTOM_TIP_POWERTIPS_NAME);

                                if (hitboxes.getType() == ArrayList.class && powerTips.getType() == ArrayList.class) {
                                    implementingClasses.add(c);
                                    logger.info("Fields found for class " + c.getCanonicalName());
                                }
                            } catch (NoSuchFieldException e) {
                                logger.info("No fields found for class " + c.getCanonicalName());
//                                e.printStackTrace();
                            }

                            long endTime = System.nanoTime();
                            long duration = endTime - startTime;
                            System.out.println("   - " + (duration / 1000000) + "ms");
                        } catch (ClassNotFoundException e) {
                            System.out.println("WARNING: Unable to find method initialize() on class marked @SpireInitializer: " + initializer);
                        }
                    }
                }
            } else {
                System.err.println(info.jarURL + " Not in DB map. Something is very wrong");
            }
        }

        return implementingClasses;
    }

    public static void initialize()
    {
        logger.info("initialize() called!");
        version = getVersion();
        try {

            String data = new String(Files.readAllBytes(Paths.get("slaytherelics_config.txt")));

            String[] lines = data.split("\r\n");

            login = lines[0].split(":")[1];
            secret = lines[1].split(":")[1];

//            logger.info("loaded login " + login + " and secret " + secret);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!areCredentialsValid()) {
            logger.info("slaytherelics_config.txt wasn't loaded, check if it exists.");
        }

        instance = new SlayTheRelicsExporter();
    }

    private void queue_check() {
        checkNextUpdate = true;
    }

    private void check() {
        if (areCredentialsValid()) {
            broadcast();
        } else {
            logger.info("Either your secret or your login are null. The config file has probably not loaded properly");
        }
    }

    private void broadcast() {

        long start = System.nanoTime();
        String json = json_builder.buildJson();
        long end = System.nanoTime();

        lastBroadcast = System.currentTimeMillis();
        lastBroadcastJson = json;

        long start_broadcast = System.nanoTime();
        BackendBroadcaster.queueMessage(json);
        long end_broadcast = System.nanoTime();
        logger.info("broadcasting relics");
        logger.info("json builder took " + (end - start) / 1e6 + " milliseconds");
        logger.info("adding to broadcast queue took " + (end_broadcast - start_broadcast) / 1e6 + " milliseconds");
//        logger.info(removeSecret(json));

    }

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();

        ModLabel label1 = new ModLabel("Use the slider below to set encoding delay of your PC.", 400.0f, 700.0f, settingsPanel, (me) -> {});
        ModLabel label2 = new ModLabel("With this set to 0, the extension will be ahead of what the stream displays.", 400.0f, 650.0f, settingsPanel, (me) -> {});
        ModSlider slider = new ModSlider("Delay", 500f, 600, 3000f, "ms", settingsPanel, (me) -> {
            logger.info("slider value: " + me.value);
            delay = (long) (me.value * me.multiplier);
        });
        ModLabelButton btn = new ModLabelButton("Save", 400f, 480f, settingsPanel, (me) -> {
            try {
                SpireConfig config = new SpireConfig("slayTheRelics", "slayTheRelicsExporterConfig", strDefaultSettings);
                config.setInt(DELAY_SETTINGS, (int) delay);
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        settingsPanel.addUIElement(label1);
        settingsPanel.addUIElement(slider);
        settingsPanel.addUIElement(label2);
        settingsPanel.addUIElement(btn);

        slider.setValue(delay * 1.0f / slider.multiplier);

        BaseMod.registerModBadge(ImageMaster.loadImage(
                "SlayTheRelicsExporterResources/img/ink_bottle.png"),
                "Slay the Relics Exporter",
                "LordAddy",
                "This mod exports data to Slay the Relics Twitch extension. See the extension config on Twitch for setup instructions.",
                settingsPanel);

        customTipImplementingClasses = findCustomTipImplementingClasses();
    }

    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        if (checkNextUpdate || System.currentTimeMillis() - lastCheck > MAX_CHECK_PERIOD_MILLIS) {

            lastCheck = System.currentTimeMillis();
            check();

            checkNextUpdate = false;
        }
    }

    public void relicPageChanged() {
        logger.info("Relic Page Changed");
        queue_check();
    }

    @Override
    public void receiveRelicGet(AbstractRelic abstractRelic) {
        logger.info("Relic Acquired");
        queue_check();
    }

    @Override
    public void receivePotionGet(AbstractPotion abstractPotion) {
        logger.info("Potion Acquired");
        queue_check();
    }

    @Override
    public void receiveStartGame() {
        logger.info("StartGame received");
        queue_check();
    }

    @Override
    public void receivePostCreateStartingRelics(AbstractPlayer.PlayerClass playerClass, ArrayList<String> arrayList) {
        logger.info("PostCreateStartingRelics received");
        queue_check();
    }

    @Override
    public void receivePowersModified() {
        logger.info("Powers modified");
        queue_check();
    }

    @Override
    public void receivePostPowerApplySubscriber(AbstractPower abstractPower, AbstractCreature abstractCreature, AbstractCreature abstractCreature1) {
        logger.info("PostPowerApply received");
        queue_check();
    }

    private static String removeSecret(String str) {
        String pattern = "\"secret\": \"[a-z0-9]*\"";
        return str.replaceAll(pattern, "\"secret\": \"********************\"");
    }
}