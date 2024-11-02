package str_exporter.testutil;

import java.lang.reflect.Field;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import str_exporter.SlayTheRelicsExporter;
import str_exporter.client.EBSClient;
import str_exporter.config.AuthManager;

import mockthespire.MockTheSpire;

public class TestUtil extends MockTheSpire {
  public static str_exporter.config.Config strConfig;
  public static EBSClient ebsClient;
  public static SpireConfig spireConfig;

  // all stubs/mocks in here were implemented as the errors came up, only supports
  // the minimum functionality of the
  // DeckJSONBuilder as of now
  private static void initializeSTRMocks() throws Exception {
    strConfig = new str_exporter.config.Config();
    strConfig.setUser("test");
    strConfig.setOathToken("test");

    spireConfig = new SpireConfig("slayTheRelics", "slayTheRelicsExporterConfig");
    spireConfig.load();

    Field field = str_exporter.config.Config.class.getDeclaredField("config");
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
  public static void setup() throws Exception {
    initializeClass();
    initializeSTRMocks();
  }
  @AfterAll
  public static void tearDown() {
    tearDownClass();
  }
}
