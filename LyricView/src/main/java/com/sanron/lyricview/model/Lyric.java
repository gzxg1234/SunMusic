package com.sanron.lyricview.model;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanron on 16-5-13.
 */
public class Lyric {

    /**
     * 歌名
     */
    public String title;
    /**
     * 歌手
     */
    public String artist;
    /**
     * 专辑
     */
    public String album;
    /**
     * 歌词作者
     */
    public String by;

    public int offset;

    public List<Sentence> sentences;

    private Lyric() {
        sentences = new ArrayList<>();
    }

    @Override
    public String toString() {
        String str = "title:" + title + "\n"
                + "artist:" + artist + "\n"
                + "album:" + album + "\n"
                + "by:" + by + "\n"
                + "offset:" + offset + "\n";
        for (Sentence sentence : sentences) {
            str += (sentence.startTime + ":" + sentence.content + "\n");
        }
        return str;
    }

    public static Lyric read(InputStream is, String encoding) {
        Lyric lyric = new Lyric();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
            String line;
            while (((line = br.readLine()) != null)) {
                parseLine(lyric, line);
            }
            br.close();
            is.close();
            Collections.sort(lyric.sentences, Sentence.sComparator);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return lyric;
    }

    public static Lyric read(String string) {
        return read(new ByteArrayInputStream(string.getBytes()), "utf-8");
    }

    public static Lyric read(File file, String encoding) {
        try {
            InputStream is = new FileInputStream(file);
            return read(is, encoding);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void parseLine(Lyric lyric, String line) {
        if (line.isEmpty()) {
            return;
        }

        //匹配标签
        Matcher matcher = Pattern.compile("\\[([a-zA-Z]+):(.*)\\]").matcher(line);
        if (matcher.find()) {
            String tag = matcher.group(1);
            String tagContent = matcher.group(2);
            if (tag.equalsIgnoreCase("ti")) {
                lyric.title = tagContent;
            } else if (tag.equalsIgnoreCase("ar")) {
                lyric.artist = tagContent;
            } else if (tag.equalsIgnoreCase("al")) {
                lyric.album = tagContent;
            } else if (tag.equalsIgnoreCase("by")) {
                lyric.by = tagContent;
            } else if (tag.equalsIgnoreCase("offset")) {
                lyric.offset = Integer.valueOf(tagContent);
            }
        } else {
            //不是标签则匹配歌词
            Matcher m = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2})\\.?(\\d{0,2})\\]").matcher(line);
            List<Integer> times = new ArrayList<>();
            int lastCloseBracketIndex = -1;
            while (m.find()) {
                int min = 0;
                int sec = 0;
                int mill = 0;
                try {
                    min = Integer.valueOf(m.group(1));
                    sec = Integer.valueOf(m.group(2));
                    String strMill = m.group(3);
                    if (!TextUtils.isEmpty(strMill)) {
                        mill = Integer.valueOf(m.group(3));
                    }
                } catch (NumberFormatException e) {
                    break;
                }
                if (min < 0 || sec < 0 || mill < 0 || sec >= 60) {
                    break;
                }
                times.add((min * 60 + sec) * 1000 + mill * 10);
                lastCloseBracketIndex = m.end(0);
            }
            if (lastCloseBracketIndex != -1) {
                String sentenceContent = line.substring(lastCloseBracketIndex, line.length());
                for (int i = 0; i < times.size(); i++) {
                    Sentence sentence = new Sentence();
                    sentence.content = sentenceContent;
                    sentence.startTime = times.get(i) + lyric.offset;
                    lyric.sentences.add(sentence);
                }
            }
        }
    }

    public static class Sentence {
        public String content;
        public int startTime;
        public static Comparator<Sentence> sComparator = new Comparator<Sentence>() {
            @Override
            public int compare(Sentence lhs, Sentence rhs) {
                return lhs.startTime - rhs.startTime;
            }
        };
    }
}
