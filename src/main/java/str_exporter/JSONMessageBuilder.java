package str_exporter;

import com.megacrit.cardcrawl.core.CardCrawlGame;

public class JSONMessageBuilder {

    private String login, secret, version;
    private int msg_type;

    public JSONMessageBuilder(String login, String secret, String version, int msg_type) {
        this.login = login;
        this.secret = secret;
        this.version = version;
        this.msg_type = msg_type;
    }

    public String buildJson() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"msg_type\":");
        sb.append(msg_type);
        sb.append(",");
        sb.append("\"streamer\":{\"login\":\"" + login + "\",\"secret\":\"" + secret + "\"},");
        sb.append("\"meta\":{\"version\": \"" + version + "\"},");
        sb.append("\"delay\":" + BackendBroadcaster.DELAY_PLACEHOLDER + ",");
        sb.append("\"message\":");

        buildMessage(sb);

        sb.append("}");

        return sb.toString();
    }

    protected void buildMessage(StringBuilder sb) {
        sb.append("\"\"");
    }

}
