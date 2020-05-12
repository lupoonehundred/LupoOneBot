package BotBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

public class LupoOneBot {
    private static DiscordApi api;
    private static final Logger logger = LogManager.getLogger(LupoOneBot.class.getName());
    //TODO Possibly Fix all the Loggers to get them to have important information.
    public static void main(String[] args) {
        api = new DiscordApiBuilder().setToken(args[0]).login().join();
        //Builds the bot and logs in to the account.

        FallbackLoggerConfiguration.setDebug(true);
        //Sets the logger to look for bugs with full details on messages and users.
        logger.info("Bot Started Up!");
        new BotListeners().mainListener();
    }
    //Getters for Private Variables
    public DiscordApi apiGetter() { return api; } //Always gets the api
    public Logger loggerGetter() { return logger; } //Always gets a logger.
}