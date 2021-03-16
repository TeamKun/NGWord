package com.github.kei7777.ngword;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdditionalWordPrompt extends StringPrompt {
    HiraganaConverter converter;
    File dataFolder;
    String firstMessage = ChatColor.LIGHT_PURPLE + "NGワードを入力してください.\n複数入力する必要がある場合はカンマ(,)で区切ってください.\n中断する場合は「--quit」と入力してください.";
    String secondMessage = ChatColor.LIGHT_PURPLE + "NGワードに対応する読み方を平仮名で入力してください\n複数入力する必要がある場合はカンマ(,)で区切ってください.\n中断する場合は「--quit」と入力してください.";
    int count = 0;
    List<String> ngwords;
    List<String> prons;

    AdditionalWordPrompt(JavaPlugin plugin) {
        this.converter = new HiraganaConverter();
        dataFolder = plugin.getDataFolder();
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (count == 0) {
            return firstMessage;
        } else {
            return secondMessage;
        }
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equals("--quit")) {
            return new MessagePrompt() {
                @Override
                protected Prompt getNextPrompt(ConversationContext context) {
                    return Prompt.END_OF_CONVERSATION;
                }

                @Override
                public String getPromptText(ConversationContext context) {
                    return Message.FailureMsg("NGワードの追加登録を中断しました.");
                }
            };
        }
        List<String> s = Arrays.asList(input.split(",").clone());
        if (count == 0) {
            ngwords = s;
            count++;
            return this;
        } else {
            prons = s;
            List<String> list = new ArrayList<>();
            list.addAll(ngwords);
            list.addAll(prons);
            for (String pron : prons) {
                list.addAll(converter.toRomaji(pron));
            }
            saveAddWordsFile(ngwords, prons);
            NGWord.additionalNGWords.put(list.get(0), list);
            return new MessagePrompt() {
                @Override
                protected Prompt getNextPrompt(ConversationContext context) {
                    return Prompt.END_OF_CONVERSATION;
                }

                @Override
                public String getPromptText(ConversationContext context) {
                    return Message.SuccessMsg("NGワードを追加登録しました.");
                }
            };
        }
    }

    private void saveAddWordsFile(List<String> ngwords, List<String> prons) {
        HashMap<String, List<String>> map = new HashMap<>();
        map.put("Pron", prons);
        map.put("NGWord", ngwords);
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(new File(dataFolder, NGWord.addWordsFilename)), "UTF-8")) {
            FileConfiguration addwordsyml = YamlConfiguration.loadConfiguration(in);
            List<HashMap<String, List<String>>> aw = ((List<HashMap<String, List<String>>>) addwordsyml.getList("Words"));
            aw.add(map);
            addwordsyml.set("Words", aw);
            addwordsyml.save(new File(dataFolder, NGWord.addWordsFilename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
