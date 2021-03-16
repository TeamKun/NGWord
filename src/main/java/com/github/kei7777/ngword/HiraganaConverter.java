package com.github.kei7777.ngword;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class HiraganaConverter {
    private final List<Character> komoji = Arrays.asList(
            'ヵ',
            'ヶ',
            'ゎ',
            'ヵ',
            'ヶ',
            'ゎ',
            'ゃ',
            'ぃ',
            'ゅ',
            'ぇ',
            'ょ',
            'ゃ',
            'ぃ',
            'ゅ',
            'ぇ',
            'ょ'
    );

    private Map<String, List<String>> convertTable;

    HiraganaConverter() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResource("convertTable.csv"), "UTF-8"))) {
            String line;
            convertTable = new HashMap<>();
            while (((line = reader.readLine())) != null) {
                line = line.replaceAll(" ", "");
                String[] str = line.split(",");
                if (convertTable.containsKey(str[0])) {
                    convertTable.get(str[0]).add(str[1]);
                } else {
                    convertTable.put(str[0], new ArrayList<String>(Collections.singleton(str[1])));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO Refactor
    public List<String> toRomaji(String s) {
        //ひらがな一文字毎に対応するローマ字のリストを作成する
        List<List<String>> corr = new ArrayList<>();
        int totalCombs = 1;
        for (int i = 0; i < s.length(); i++) {
            corr.add(i, new ArrayList<String>() {{
                add("");
            }});
            String letter = String.format("%c", s.charAt(i));
            if (!convertTable.containsKey(letter)) {
                corr.add(i, Collections.singletonList(letter));
            } else {
                corr.add(i, convertTable.get(letter));
            }
            totalCombs *= corr.get(i).size();
        }
        List<String> res = comb(totalCombs, corr);

        //拗音を考慮したパターンを作成する
        List<List<String>> youon = new ArrayList<>();
        totalCombs = 1;
        for (int i = 0; i < s.length(); i++) {
            youon.add(i, new ArrayList<String>() {{
                add("");
            }});
            String letter = String.format("%c", s.charAt(i));
            if (!convertTable.containsKey(letter)) {
                youon.add(i, Collections.singletonList(letter));
            } else {
                if (i < s.length() - 1 && komoji.contains((s.charAt(i + 1)))) {
                    youon.add(i, convertTable.get(String.format("%c%c", s.charAt(i), s.charAt(i + 1))));
                    totalCombs *= youon.get(i).size();
                    i++;
                    continue;
                }
                youon.add(i, convertTable.get(String.format("%c", s.charAt(i))));
            }
            totalCombs *= youon.get(i).size();
        }
        res.addAll(comb(totalCombs, youon));

        //促音を考慮したパターンを作成する
        List<List<String>> sokuon = new ArrayList<>();
        totalCombs = 1;
        for (int i = 0; i < s.length(); i++) {
            sokuon.add(i, new ArrayList<String>() {{
                add("");
            }});
            String letter = String.format("%c", s.charAt(i));
            if (!convertTable.containsKey(letter)) {
                sokuon.add(i, Collections.singletonList(letter));
            } else {
                if (s.charAt(i) == 'っ') {
                    List<String> tmp = convertTable.get(String.format("%c", s.charAt(i + 1)));
                    sokuon.add(i, tmp.stream().map(x -> String.format("%c%s", x.charAt(0), x)).collect(Collectors.toList()));
                    totalCombs *= sokuon.get(i).size();
                    i++;
                } else {
                    sokuon.add(i, convertTable.get(String.format("%c", s.charAt(i))));
                    totalCombs *= sokuon.get(i).size();
                }
            }
        }
        res.addAll(comb(totalCombs, sokuon));

        //促音と拗音を考慮したパターンを作成する
        List<List<String>> yousoku = new ArrayList<>();
        totalCombs = 1;
        for (int i = 0; i < s.length(); i++) {
            yousoku.add(i, new ArrayList<String>() {{
                add("");
            }});
            String letter = String.format("%c", s.charAt(i));
            if (!convertTable.containsKey(letter)) {
                yousoku.add(i, Collections.singletonList(letter));
            } else {
                if (s.charAt(i) == 'っ') {
                    List<String> tmp;
                    if (i < s.length() - 2 && komoji.contains(s.charAt(i + 2))) {
                        tmp = convertTable.get(String.format("%c%c", s.charAt(i + 1), s.charAt(i + 2)));
                        tmp = tmp.stream().map(x -> String.format("%c%s", x.charAt(0), x)).collect(Collectors.toList());
                        yousoku.add(i, tmp);
                        totalCombs *= yousoku.get(i).size();
                        i += 2;
                    } else {
                        tmp = convertTable.get(String.format("%c", s.charAt(i + 1)));
                        tmp = tmp.stream().map(x -> String.format("%c%s", x.charAt(0), x)).collect(Collectors.toList());
                        yousoku.add(i, tmp);
                        totalCombs *= yousoku.get(i).size();
                        i++;
                    }
                } else if (i < s.length() - 1 && komoji.contains((s.charAt(i + 1)))) {
                    yousoku.add(i, convertTable.get(String.format("%c%c", s.charAt(i), s.charAt(i + 1))));
                    totalCombs *= yousoku.get(i).size();
                    i++;
                } else {
                    yousoku.add(i, convertTable.get(String.format("%c", s.charAt(i))));
                    totalCombs *= yousoku.get(i).size();
                }
            }
        }
        res.addAll(comb(totalCombs, yousoku));
        return res.stream().distinct().collect(Collectors.toList());
    }

    public String toKatakana(String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char code = s.charAt(i);
            if ((code >= 0x3041) && (code <= 0x3093)) {
                buf.append((char) (code + 0x60));
            } else {
                buf.append(code);
            }
        }
        return buf.toString();
    }

    public String toHiragana(String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char code = s.charAt(i);
            if ((code >= 0x30a1) && (code <= 0x30f3)) {
                buf.append((char) (code - 0x60));
            } else {
                buf.append(code);
            }
        }
        return buf.toString();
    }

    //TODO Refactor
    private List<String> comb(int total, List<List<String>> src) {
        List<String> comb = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            comb.add("");
        }

        int rep = total;
        for (List<String> romajis : src) {
            rep /= romajis.size();
            for (int i = 0; i < comb.size(); i += romajis.size() * rep) {
                for (int j = 0; j < romajis.size(); j++) {
                    for (int k = i; k < rep + i; k++) {
                        comb.set(k + j * rep, comb.get(k + j * rep) + romajis.get(j));
                    }
                }
            }
        }
        return comb;
    }

    private InputStream getResource(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }
}
