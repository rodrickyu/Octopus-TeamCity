package octopus.teamcity.agent.commands;

public class ListEnvironmentsCommandBuilder implements OctopusCommandBuilder {
    private String apiKey;
    private String serverUrl;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setServer(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String buildCommand() {
        return "list-environments --server=\"" + serverUrl + "\" --apikey=\"" + apiKey + "\"";
    }
}
