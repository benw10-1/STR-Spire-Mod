package str_exporter.devcommands.setspireblightdeck;

import basemod.DevConsole;
import basemod.devcommands.ConsoleCommand;
import basemod.helpers.ConvertHelper;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SetSpireblightDeck extends ConsoleCommand {

  public SetSpireblightDeck() {
    requiresPlayer = true;
    maxExtraTokens = 2;
    simpleCheck = true;
  }

  // TODO: cache results from this call
  private String[] getCardTextFromSpireblight(String runID) throws IOException {
    // TODO: URL from config
    final String fullPath = "https://baalorlord.tv/runs/" + runID;
    URL url = new URL(fullPath);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "text/html");
    con.setRequestProperty("Accept", "text/html");
    con.setDoOutput(true);

    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),
        StandardCharsets.UTF_8))) {
      if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
        // albeit innefficient, just read all the lines into memory while we are looking
        // for the cards
        String[] linesArr = br.lines().toArray(String[]::new);

        List<String> res = new ArrayList<String>();

        boolean readingCards = false;
        for (int i = 0; i < linesArr.length; i++) {
          String line = linesArr[i];

          // check if line contains character + loss
          if (line.contains("div class=\"cards\"")) {
            readingCards = true;
            continue;
          }

          if (!readingCards) {
            continue;
          }

          // hack, but we know that there are no other divs in the cards section
          if (line.contains("</div>")) {
            break;
          }

          final int startIdx = line.indexOf("<span>") + "<span>".length();
          if (startIdx < 0) {
            continue;
          }

          final int endIdx = line.indexOf("</span>", startIdx);
          if (endIdx < 0) {
            continue;
          }

          res.add(line.substring(startIdx, endIdx));
        }
        String[] resArr = new String[res.size()];
        res.toArray(resArr);
        return resArr;
      }
      throw new IOException("GET " + fullPath + " failed: HTTP error code: " + con.getResponseCode());
    }
  }

  public void execute(String[] tokens, int depth) {
    if (tokens.length < 1) {
      DevConsole.log("No ID specified");
      return;
    }

    final int runID = ConvertHelper.tryParseInt(tokens[1], 0);
    if (runID == 0) {
      DevConsole.log("Invalid ID specified");
      return;
    }

    try {
      String[] cardNameList = getCardTextFromSpireblight(tokens[1]);

      if (cardNameList.length == 0) {
        DevConsole.log("No cards found for run ID - " + tokens[1]);
        return;
      }

      // remove all cards
      for (String str : AbstractDungeon.player.masterDeck.getCardNames()) {
        AbstractDungeon.player.masterDeck.removeCard(str);
      }

      // add all cards from run
      for (String cardName : cardNameList) {
        int cardCount = 1;
        int endCountIdx = cardName.indexOf("x ");
        if (endCountIdx >= 0) {
          // parse \d+x part
          cardCount = ConvertHelper.tryParseInt(cardName.substring(0, endCountIdx), 1);
          if (cardCount <= 1) {
            throw new RuntimeException("Invalid card count - " + cardName + " - " + cardName.substring(0, endCountIdx));
          }

          cardName = cardName.substring(endCountIdx + 2); // + len("x ")
        }

        // get current character color

        int upgradeCount = 0;
        int upgradeIdx = cardName.indexOf("+");
        if (upgradeIdx >= 0) {
          if (upgradeIdx == cardName.length() - 1) {
            upgradeCount = 1;
          } else {
            upgradeCount = ConvertHelper.tryParseInt(cardName.substring(upgradeIdx + 1), 0);
            if (upgradeCount <= 1) { // not valid if has stuff after + but no number
              throw new RuntimeException("Invalid upgrade count after parse - " + cardName);
            }
          }

          cardName = cardName.substring(0, upgradeIdx);
        }
        
        AbstractCard card = CardLibrary.getCard(cardName);
        if (card == null) {
          // for these cases, the card ID might not be the same as what is displayed on
          // the card. (eg "Strike" vs "Strike_R", or "Recursion"'s ID is "Redo")
          // note that this approach will be locale mapped, so if your game is not in
          // english this wont work
          for (AbstractCard c : CardLibrary.getAllCards()) {
            // strip out the color suffix from strikes and defends
            String name = c.name.replaceFirst("_(R|G|B|P)", cardName);
            if (!name.equals(cardName)) {
              continue;
            }

            // if curse just add it
            // if starter defend or strike, add this card only if its the classes' defend or
            // strike
            if (c.type == AbstractCard.CardType.CURSE || (!c.isStarterDefend() && !c.isStarterStrike())
                || c.color == AbstractDungeon.player.getCardColor()) {
              card = c.makeCopy();
              break;
            }
          }

          if (card == null) {
            DevConsole.log("Card not found - " + cardName);

            continue;
          }
        }

        for (int i = 0; i < cardCount; i++) {
          AbstractCard copy = card.makeCopy();
          for (int j = 0; j < upgradeCount; j++) {
            copy.upgrade();
          }

          UnlockTracker.markCardAsSeen(copy.cardID);

          AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(copy, Settings.WIDTH / 2.0f,
              Settings.HEIGHT / 2.0f));
        }
      }
    } catch (Exception e) {
      DevConsole.log("Error running command - " + e);
    }
  }
}
