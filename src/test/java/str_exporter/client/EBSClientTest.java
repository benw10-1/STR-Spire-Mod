package str_exporter.client;

import java.net.URL;

import org.junit.jupiter.api.Test;

// for example card add
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.WraithForm;
import com.megacrit.cardcrawl.cards.purple.WheelKick;
import com.megacrit.cardcrawl.cards.purple.WreathOfFlame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;

import basemod.BaseMod;
import basemod.patches.whatmod.WhatMod;
import str_exporter.builders.DeckJSONBuilder;
import str_exporter.testutil.TestUtil;

public class EBSClientTest extends TestUtil {
  @Test
  public void test() throws Exception {
    // clear deck before adding cards
    // AbstractDungeon.player.masterDeck.clear();

    // example way to add cards to the deck via the spire API
    // import the card you want to add
    // AbstractCard card = new WheelKick();

    // upgrade the card
    // card.upgrade();

    // add the card to the deck
    // AbstractDungeon.player.masterDeck.addToTop(card);

    loadDeckJSONFile("breaking.json");

    spireConfig.setString("api_url", "http://localhost:8080");

    DeckJSONBuilder deckJsonBuilder = new DeckJSONBuilder(strConfig, "test");

    String msg = strConfig.gson.toJson(deckJsonBuilder.buildMessage());

    System.out.println(msg);

    // ebsClient.broadcastMessage(msg);
  }
}
