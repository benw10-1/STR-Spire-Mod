package str_exporter.client;

import org.junit.jupiter.api.Test;

// for example card add
// import com.megacrit.cardcrawl.cards.AbstractCard;
// import com.megacrit.cardcrawl.cards.blue.EchoForm;
// import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import str_exporter.builders.DeckJSONBuilder;
import str_exporter.testutil.TestUtil;

public class EBSClientTest extends TestUtil {
  @Test
  public void test() throws Exception {
    // example way to add cards to the deck via the spire API
    // import the card you want to add
    // AbstractCard card = new EchoForm();

    // upgrade the card
    // card.upgrade();

    // add the card to the deck
    // AbstractDungeon.player.masterDeck.addToTop(card);

    loadDeckJSONFile("run-1728675795.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
  }
}
