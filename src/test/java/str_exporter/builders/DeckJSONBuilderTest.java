package str_exporter.builders;

import org.junit.jupiter.api.Test;

import str_exporter.testutil.TestUtil;

public class DeckJSONBuilderTest extends TestUtil {

  // @Test
  public void buildStarterDeck() throws Exception {
    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    System.out.println(strConfig.gson.toJson(deckJsonBuilder.buildMessage()));
  }
}
