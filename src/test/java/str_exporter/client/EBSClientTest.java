package str_exporter.client;

import org.junit.jupiter.api.Test;

import str_exporter.builders.DeckJSONBuilder;
import str_exporter.testutil.TestUtil;

public class EBSClientTest extends TestUtil {
  @Test
  public void test() throws Exception {
    loadDeckJSONFile("basic.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
  }
}
