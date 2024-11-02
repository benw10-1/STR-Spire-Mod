package str_exporter.testutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.audio.MusicMaster;
import com.megacrit.cardcrawl.audio.SoundMaster;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.EchoForm;
import com.megacrit.cardcrawl.characters.Defect;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.characters.TheSilent;
import com.megacrit.cardcrawl.characters.Watcher;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardHelper;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.Prefs;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.integrations.PublisherIntegration;
import com.megacrit.cardcrawl.integrations.DistributorFactory.Distributor;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.screens.DisplayOption;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import basemod.BaseMod;
import basemod.helpers.dynamicvariables.BlockVariable;
import basemod.helpers.dynamicvariables.DamageVariable;
import basemod.helpers.dynamicvariables.MagicNumberVariable;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import str_exporter.SlayTheRelicsExporter;
import str_exporter.client.EBSClient;
import str_exporter.config.AuthManager;

public class TestUtil {
  private static HeadlessApplication application;
  private static AssetManager assetManager;
  public static CardCrawlGame game;
  public static str_exporter.config.Config strConfig;
  public static EBSClient ebsClient;
  public static SpireConfig spireConfig;

  private static void setupAppMocks() {
    // Initialize the headless application
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

    application = new HeadlessApplication(new ApplicationListener() {
      @Override
      public void create() {
      }

      @Override
      public void resize(int width, int height) {
      }

      @Override
      public void render() {
      }

      @Override
      public void pause() {
      }

      @Override
      public void resume() {
      }

      @Override
      public void dispose() {
      }
    }, config);

    Gdx.graphics = application.getGraphics();

    if (Gdx.files == null) {
      Gdx.files = application.getFiles();
    }
    // Initialize AssetManager and load assets if necessary
    assetManager = new AssetManager();

    Gdx.gl = Mockito.mock(GL20.class);

    Locale.setDefault(Locale.ENGLISH);
  }

  // all stubs/mocks in here were implemented as the errors came up, only supports
  // the minimum functionality of the
  // DeckJSONBuilder as of now
  private static void setupGameMocks() throws Exception {
    // Create a mock PublisherIntegration instance
    PublisherIntegration mockIntegration = Mockito.mock(PublisherIntegration.class);

    // Mock the getType() method
    Mockito.when(mockIntegration.getType()).thenReturn(Distributor.STEAM);

    // Access the private field using reflection
    Field field = CardCrawlGame.class.getDeclaredField("publisherIntegration");
    field.setAccessible(true);

    // Set the field to the mock object
    field.set(null, mockIntegration);

    Settings.gamePref = new Prefs();
    Settings.displayOptions = new ArrayList<DisplayOption>();
    Settings.displayOptions.add(new DisplayOption(0, 0));

    Settings.soundPref = new Prefs();

    Settings.scale = 1.0f;
    Settings.language = Settings.GameLanguage.ENG;
    Settings.setLanguage(Settings.language, true);

    CardCrawlGame.languagePack = new LocalizedStrings();

    strConfig = new str_exporter.config.Config();
    strConfig.setUser("test");
    strConfig.setOathToken("test");

    spireConfig = new SpireConfig("slayTheRelics", "slayTheRelicsExporterConfig");
    spireConfig.load();

    field = str_exporter.config.Config.class.getDeclaredField("config");
    field.setAccessible(true);

    field.set(strConfig, spireConfig);

    field = SlayTheRelicsExporter.class.getDeclaredField("config");
    field.setAccessible(true);

    field.set(null, strConfig);

    ebsClient = new EBSClient(strConfig);

    field = SlayTheRelicsExporter.class.getDeclaredField("ebsClient");
    field.setAccessible(true);

    field.set(null, ebsClient);

    field = SlayTheRelicsExporter.class.getDeclaredField("authManager");
    field.setAccessible(true);

    field.set(null, new AuthManager(ebsClient, strConfig));

    CardCrawlGame.music = new MusicMaster();
    CardCrawlGame.sound = new SoundMaster();
  }

  private static void setupLoaderClassPath() throws Exception {
    Loader.MODINFOS = new ModInfo[0];
    URL stsLoc = EchoForm.class.getProtectionDomain().getCodeSource().getLocation();
    Loader.STS_JAR = stsLoc.getPath(); // refed by misc. BaseMod calls to see what mod objects came from

    ClassPool pool = new ClassPool();
    URLClassLoader loader = new URLClassLoader(new URL[] { stsLoc });
    pool.appendClassPath(new LoaderClassPath(loader));
  
    Field field = Loader.class.getDeclaredField("POOL");
    field.setAccessible(true);

    field.set(null, pool);
  }

  private static void setupBaseMod() throws Exception {
    // populate loader pool with sts classes without needing the patch jar which is coupled in the loader entrypoint
    setupLoaderClassPath();

    BaseMod.initialize();

    BaseMod.addKeyword(new String[] { "[E]" }, GameDictionary.TEXT[0]); // from annoyingly tucked basemod hook - energy keyword

    BaseMod.addDynamicVariable(new DamageVariable());
    BaseMod.addDynamicVariable(new BlockVariable());
    BaseMod.addDynamicVariable(new MagicNumberVariable());
  }

  @BeforeAll
  public static void setUpClass() throws Exception {
    setupAppMocks();
    setupGameMocks();

    game = new CardCrawlGame("");

    AbstractCreature.initialize();
    AbstractCard.initialize();
    GameDictionary.initialize();

    ImageMaster.initialize();
    AbstractPower.initialize();
    FontHelper.initialize();
    // AbstractCard.initializeDynamicFrameWidths();

    UnlockTracker.initialize();
    CardLibrary.initialize();
    RelicLibrary.initialize();
    // InputHelper.initialize();
    TipTracker.initialize();
    // ModHelper.initialize();
    // ShaderHelper.initializeShaders();
    // UnlockTracker.retroactiveUnlock();
    // CInputHelper.loadSettings();

    AbstractDungeon.topPanel = Mockito.mock(TopPanel.class);
    setupBaseMod();
  }

  @AfterAll
  public static void tearDownClass() {
    // Dispose AssetManager
    if (assetManager != null) {
      assetManager.dispose();
    }

    // Clean up headless application
    if (application != null) {
      application.exit();
      application = null;
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    setupRun(PlayerClass.IRONCLAD);
  }

  public void setupRun(PlayerClass cPlayerClass) throws Exception {
    switch (cPlayerClass) {
      case IRONCLAD:
        Constructor<Ironclad> ironcladConstructor = Ironclad.class.getDeclaredConstructor(String.class);
        ironcladConstructor.setAccessible(true); // Bypass access control checks
        AbstractDungeon.player = ironcladConstructor.newInstance("test");
        break;

      case THE_SILENT:
        Constructor<TheSilent> silentConstructor = TheSilent.class.getDeclaredConstructor(String.class);
        silentConstructor.setAccessible(true); // Bypass access control checks
        AbstractDungeon.player = silentConstructor.newInstance("test");
        break;

      case DEFECT:
        Constructor<Defect> defectConstructor = Defect.class.getDeclaredConstructor(String.class);
        defectConstructor.setAccessible(true); // Bypass access control checks
        AbstractDungeon.player = defectConstructor.newInstance("test");
        break;

      case WATCHER:
        Constructor<Watcher> watcherConstructor = Watcher.class.getDeclaredConstructor(String.class);
        watcherConstructor.setAccessible(true); // Bypass access control checks
        AbstractDungeon.player = watcherConstructor.newInstance("test");
        break;

      default:
        throw new Exception("Invalid player class");
    }

    setRandomSeed();
    // init calls at start of game minus the rendering
    AbstractDungeon.generateSeeds();

    CardCrawlGame.dungeon = game.getDungeon("Exordium", AbstractDungeon.player);
    game.mode = CardCrawlGame.GameMode.GAMEPLAY;
  }

  private void setRandomSeed() throws Exception {
    long sourceTime = System.nanoTime();
    Random rng = new Random(Long.valueOf(sourceTime));
    Settings.seedSourceTimestamp = sourceTime;
    Settings.seed = Long.valueOf(SeedHelper.generateUnoffensiveSeed(rng));
    Settings.seedSet = false;
  }

  public void loadDeckJSONFile(String filename) throws Exception {
    FileHandle file = Gdx.files.classpath("str_exporter_testdata/" + filename);

    String jsonStr = file.readString();

    @SuppressWarnings("unchecked")
    Map<String, Double> cardMap = strConfig.gson.fromJson(jsonStr, Map.class);

    Character playerColorChar = Character
        .toLowerCase(AbstractDungeon.player.getCardColor().name().toLowerCase().charAt(0));

    // for some cases the ID wont match the name of the card (like Recursion, which
    // has the ID "Redo", or "Strike" with ID
    // "Strike_R" for Ironclad), so check all cards by name
    Map<String, AbstractCard> cardMapByName = new HashMap<>();
    for (AbstractCard card : CardLibrary.getAllCards()) {
      String name = card.name.toLowerCase();
      if (name == "strike" || name == "defend") {
        name = name + "_" + playerColorChar;
      }
      cardMapByName.put(name, card);
    }

    AbstractDungeon.player.masterDeck.clear();

    cardMap.forEach((cardName, count) -> {
      cardName = cardName.toLowerCase();
      int upgradeCount = 0;

      String[] cardNameUpgradeSplit = cardName.split("\\+");
      if (cardNameUpgradeSplit.length == 2) {
        upgradeCount = 1;
        if (cardNameUpgradeSplit[1].length() > 0) {
          upgradeCount = Integer.parseInt(cardNameUpgradeSplit[1]);
        }

        cardName = cardNameUpgradeSplit[0];
      }

      if (cardName == "strike" || cardName == "defend") {
        cardName = cardName + "_" + playerColorChar;
      }

      AbstractCard c = cardMapByName.getOrDefault(cardName.toLowerCase(), null);
      if (c == null) {
        throw new RuntimeException("Card not found: " + cardName);
      }

      CardHelper.obtain(c.cardID, c.rarity, c.color);

      for (int i = 0; i < count; i++) {
        AbstractCard cCpy = c.makeCopy();
        for (int j = 0; j < upgradeCount; j++) {
          cCpy.upgrade();
        }
        cCpy.displayUpgrades(); // updates metadata which affects the description

        cCpy.update();

        AbstractDungeon.player.masterDeck.addToTop(cCpy);
      }
    });
  }
}
