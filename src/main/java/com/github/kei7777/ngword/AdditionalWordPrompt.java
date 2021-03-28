package com.github.kei7777.ngword;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

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
    List<String> ngwords;
    List<String> prons;
    NGWord plugin;

    AdditionalWordPrompt(NGWord plugin) {
        this.converter = new HiraganaConverter();
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.LIGHT_PURPLE + "NGワードと対応する読み方を半角スペース区切りで入力してください.\nそれぞれ複数入力する必要がある場合はカンマ(,)で区切ってください.\n中断する場合は「--quit」と入力してください.\n記述例: 50人クラフト 50にんくらふと,ごじゅうにんくらふと";
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
        String[] inputs = input.split(" ");
        ngwords = Arrays.asList(inputs[0].split(",").clone());
        prons = Arrays.asList(inputs[1].split(",").clone());
        saveAddWordsFile(ngwords, prons);

        List<String> list = new ArrayList<>();
        list.addAll(ngwords);
        for (String ng : ngwords) {
            list.add(plugin.normalize(ng));
        }

        list.addAll(prons);
        for (String pron : prons) {
            list.addAll(converter.toRomaji(plugin.normalize(pron)));
        }
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
