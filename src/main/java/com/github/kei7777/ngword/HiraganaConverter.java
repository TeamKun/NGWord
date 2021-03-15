package com.github.kei7777.ngword;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class HiraganaConverter {
    JavaPlugin plugin;
    private final List<String> komoji = Arrays.asList(
            "ヵ",
            "ヶ",
            "ゎ",
            "ヵ",
            "ヶ",
            "ゎ",
            "ゃ",
            "ぃ",
            "ゅ",
            "ぇ",
            "ょ",
            "ゃ",
            "ぃ",
            "ゅ",
            "ぇ",
            "ょ"
    );

    private Map<String, List<String>> convertTable;

    HiraganaConverter(JavaPlugin plugin) {
        this.plugin = plugin;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("convertTable.csv"), "UTF-8"))) {
            String line;
            convertTable = new HashMap<>();
            while (((line = reader.readLine())) != null) {
                String[] str = line.split(",");
                if (convertTable.containsKey(str[0])) {
                    convertTable.get(str[0]).add(str[1]);
                } else {

                    convertTable.put(str[0], new ArrayList<String>(Collections.singleton(str[1])));
                }
            }
        } catch (Exception ignore) {
            //TODO REMOVE
            ignore.printStackTrace();
        }
        plugin.getLogger().info(convertTable.toString());
    }

    public List<String> convert(String s) {
        List<List<String>> corr = new ArrayList<>();
        int total = 1;
        //ひらがな一文字毎に対応するローマ字のリストを作成する
        for (int i = 0; i < s.length(); i++) { //拗音
            corr.add(i, new ArrayList<String>() {{
                add("");
            }});
            if (i < s.length() - 1 && komoji.contains(s.charAt(i + 1))) {
                corr.add(i, convertTable.get(Character.toString(s.charAt(i)) + s.charAt(i + 1)));
            } else if (s.charAt(i) == 'っ') { //促音
                List<String> tmp = convertTable.get(Character.toString(s.charAt(i + 1)));
                corr.add(i, tmp.stream().map(x -> String.format("%c%s", x.charAt(0), x)).collect(Collectors.toList()));
            }
            corr.add(i, convertTable.get(Character.toString(s.charAt(i))));
            i *= corr.get(i).size();
        }

        List<String> conv = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            conv.add("");
        }

        int rep = total;
        for (int i = 0; i < corr.size(); i++) {
            rep /= corr.get(i).size();
            for (String romaji : corr.get(i)) {
                for (int j = rep * i; j < rep * (i + 1); j++) {
                    plugin.getLogger().info(corr.toString());
                    plugin.getLogger().info(String.format("%d %d", i, j));
                    conv.set(j, conv.get(j) + romaji);
                }
            }
        }

        return conv;
    }

}
