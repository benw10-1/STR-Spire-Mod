package str_exporter.client;

import org.junit.jupiter.api.Test;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.WheelKick;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import str_exporter.builders.DeckJSONBuilder;
import str_exporter.testutil.TestUtil;

public class EBSClientTest extends TestUtil {
  @Test
  public void integrationTestBasicJSON() throws Exception {
    initializeRun();
    loadDeckJSONFile("basic.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
    Thread.sleep(1000); // let buffer time between messages a little
  }

  @Test
  public void integrationTestBreakingJSON() throws Exception {
    initializeRun();
    loadDeckJSONFile("breaking.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
    Thread.sleep(1000); // let buffer time between messages a little
  }

  @Test
  public void integrationTestBreakingRealJSON() throws Exception {
    initializeRun();
    loadDeckJSONFile("run-1728675795.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
    Thread.sleep(1000); // let buffer time between messages a little
  }

  @Test
  public void integrationTestFixed() throws Exception {
    initializeRun();
    // clear deck before adding cards
    AbstractDungeon.player.masterDeck.clear();

    // example way to add cards to the deck via the spire API
    // import the card you want to add
    AbstractCard card = new WheelKick();

    // upgrade the card
    card.upgrade();

    // add the card to the deck
    AbstractDungeon.player.masterDeck.addToTop(card);

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    ebsClient.broadcastMessage(msg);
    Thread.sleep(1000); // let buffer time between messages a little
  }
}
