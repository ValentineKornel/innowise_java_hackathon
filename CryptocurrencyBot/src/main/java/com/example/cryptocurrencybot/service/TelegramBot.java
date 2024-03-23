package com.example.cryptocurrencybot.service;

import com.example.cryptocurrencybot.config.BotConfig;
import com.example.cryptocurrencybot.model.User;
import com.example.cryptocurrencybot.repositories.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final PriceUpdater priceUpdater;

    @Autowired
    UserRepository userRepository;

    static final String HELP_TEXT = "используйте команду /currentrate с параметром например" +
            " [/currentrate TON] чтобы узнать текущую цену критовалюты\n" +
            "используйте команду /changepr с параметром например\"" +
            "            \" [/changepr 20] чтобы изменть процент изменения цены для уведомления\\n";



    public TelegramBot(BotConfig botConfig, PriceUpdater priceUpdater) throws SQLException {
        this.config = botConfig;
        this.priceUpdater = priceUpdater;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/currentrate", "get current rate [command param]"));
        listofCommands.add(new BotCommand("/changepr", "get current rate [command param]"));

        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update){

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            System.out.println(update.getMessage().getText());


            String[] parts = messageText.split("\\s+", 2); // Разделяем сообщение по первому пробелу
            String command = parts[0]; // Получаем команду
            String parameter = parts.length > 1 ? parts[1] : null;


            if (command.contains("/send") && config.getOwnerId() == chatId) {
                // Ваш код для отправки сообщений пользователям
            } else {
                switch (command) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        break;
                    case "/changepr":
                            int param = Integer.parseInt(parameter);
                            updateRate(chatId, param);
                        break;
                    case "/currentrate":
                        Map<String, Double> prices = priceUpdater.getPrices();

                        Double price = priceUpdater.getPrices().get(parameter);
                        prepareAndSendMessage(chatId, parameter + " price is " + price);
                        break;
                    default:
                        prepareAndSendMessage(chatId, "Sorry, command was not recognized");
                }
            }
        }
    }


    @EventListener
    private void handlePriceEncrease(PriceIncreaseEvent event) throws SQLException {
        /*prepareAndSendMessage(1115193769, "Цена для криптовалюты "
                + event.getSymbol() + " увеличилась на 10% или более!");*/
        int rate = event.getPercent();
        List<User> users = userRepository.findUserByRate(rate);

        String message = "Цена для криптовалюты " + event.getSymbol() + " изменилась на " + event.getPercent() + "% или более!";

        for (User user : users) {
            prepareAndSendMessage(user.getId(), message);
        }
    }

    private void registerUser(Message msg){
        if(userRepository.findUserById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setId(chatId);
            user.setUsername(chat.getUserName());
            user.setRate(10);

            userRepository.saveUser(user);
            log.info("user saved: " + user);
        }
    }

    private void updateRate(Long chatId, int newRate){
        userRepository.updateUserRate(chatId, newRate);
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startCommandReceived(long chatId, String name) {

        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        log.info("Replied to user " + name);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        executeMessage(message);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
