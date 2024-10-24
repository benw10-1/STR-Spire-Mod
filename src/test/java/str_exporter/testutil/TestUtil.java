package str_exporter.testutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.Defect;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.characters.TheSilent;
import com.megacrit.cardcrawl.characters.Watcher;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.Prefs;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.integrations.PublisherIntegration;
import com.megacrit.cardcrawl.integrations.DistributorFactory.Distributor;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.screens.DisplayOption;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import basemod.BaseMod;
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

    Loader.MODINFOS = new ModInfo[0];

    field = BaseMod.class.getDeclaredField("keywordProperNames");
    field.setAccessible(true);

    // Set the field to the mock object
    field.set(null, new HashMap<>());

    field = BaseMod.class.getDeclaredField("keywordUniqueNames");
    field.setAccessible(true);

    field.set(null, new HashMap<>());

    field = BaseMod.class.getDeclaredField("keywordUniquePrefixes");
    field.setAccessible(true);

    field.set(null, new HashMap<>());

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
  }

  @BeforeAll
  public static void setUpClass() throws Exception {
    setupAppMocks();
    setupGameMocks();

    game = new CardCrawlGame("");

    Settings.language = Settings.GameLanguage.ENG;
    Settings.scale = 1.0f;

    CardCrawlGame.languagePack = new LocalizedStrings();

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
    setupGame(PlayerClass.IRONCLAD);
  }

  public void setupGame(PlayerClass cPlayerClass) throws Exception {
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

    // init calls at start of game minus the rendering
    AbstractDungeon.player.initializeStarterDeck();

    game.getDungeon("EXORDIUM", AbstractDungeon.player);
    game.mode = CardCrawlGame.GameMode.GAMEPLAY;
  }

  public void loadDeckJSONFile(String filename) throws Exception {
    FileHandle file = Gdx.files.classpath("str_exporter_testdata/" + filename);

    String jsonStr = file.readString();

    @SuppressWarnings("unchecked")
    Map<String, Double> cardMap = strConfig.gson.fromJson(jsonStr, Map.class);

    AbstractDungeon.player.masterDeck.clear();

    cardMap.forEach((cardName, count) -> {
      AbstractCard c = CardLibrary.getCard(cardName);
      AbstractCard cCpy3 = null;
      if (c == null) {
        // for some cases the ID wont match the name of the card (like Recursion, which
        // has the ID "Redo", or "Strike" with ID
        // "Strike_R" for Ironclad), so check all cards by name
        for (AbstractCard card : CardLibrary.getAllCards()) {
          String name = card.name.replaceFirst("_(R|G|B|P)", ""); // because of this 
          if (!name.equals(cardName)) {
            continue;
          }

          // insert starter cards from the correct class
          if (card.isStarterDefend() || card.isStarterStrike()) {
            if (card.color != AbstractDungeon.player.getCardColor()) {
              continue;
            }
          }

          cCpy3 = card.makeCopy();
          break;
        }

        if (cCpy3 == null) {
          throw new RuntimeException("Card not found: " + cardName);
        }
      } else {
        cCpy3 = c.makeCopy();
      }

      String[] cardNameSplit = cardName.split("\\+");
      if (cardNameSplit.length == 2) {
        int upgradeCount = 1;
        if (cardNameSplit[1].length() > 0) {
          upgradeCount = Integer.parseInt(cardNameSplit[1]);
        }

        for (int i = 0; i < upgradeCount; i++) {
          cCpy3.upgrade();
        }
      }

      for (int i = 0; i < count; i++) {
        AbstractCard card = cCpy3.makeCopy();

        AbstractDungeon.player.masterDeck.addToTop(card);
      }
    });
  }
}
